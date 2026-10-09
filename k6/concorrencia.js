import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const EMAIL = __ENV.EMAIL || 'ana@orbita.com';
const PIN = __ENV.PIN || '1234';
const TICKER = __ENV.TICKER || 'ORBT3';
const QTD_REQUISICOES = Number(__ENV.QTD_REQUISICOES || 20);

const POLL_TENTATIVAS = Number(__ENV.POLL_TENTATIVAS || 20);
const POLL_INTERVALO = Number(__ENV.POLL_INTERVALO || 0.5);

const executadas = new Counter('ordens_executadas');
const rejeitadas = new Counter('ordens_rejeitadas');
const timeoutPolling = new Counter('ordens_timeout_polling');
const erroInesperado = new Counter('erro_inesperado');
const estoqueInconsistente = new Counter('estoque_inconsistente');

export const options = {
    scenarios: {
        compra_simultanea: {
            executor: 'shared-iterations',
            vus: QTD_REQUISICOES,
            iterations: QTD_REQUISICOES,
            maxDuration: '60s',
        },
    },
    thresholds: {
        erro_inesperado: ['count==0'],
        ordens_timeout_polling: ['count==0'],
        estoque_inconsistente: ['count==0'],
    },
};

export function setup() {
    const params = { headers: { 'Content-Type': 'application/json' } };

    const resLogin = http.post(`${BASE_URL}/api/autenticacao/clientes`, JSON.stringify({ email: EMAIL, pin: PIN }), params);
    const token = JSON.parse(resLogin.body).token;

    const resAssinatura = http.post(`${BASE_URL}/api/autenticacao/assinaturas`, JSON.stringify({ pin: PIN }), {
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    });
    const assinatura = JSON.parse(resAssinatura.body).token;

    const resOferta = http.get(`${BASE_URL}/api/ofertas/${TICKER}`);
    const estoqueInicial = JSON.parse(resOferta.body).quantidadeDisponivel;

    return { token, assinatura, estoqueInicial };
}

export default function (dados) {
    const payload = JSON.stringify({
        ticker: TICKER,
        tipo: 'COMPRA',
        quantidade: 1,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${dados.token}`,
            'X-Assinatura': dados.assinatura,
        },
        responseCallback: http.expectedStatuses(202, 422),
    };

    const resOrdem = http.post(`${BASE_URL}/api/ordens`, payload, params);

    const ok = check(resOrdem, {
        'ordem aceita (202) ou recusada por falta de oferta (422)': (r) => r.status === 202 || r.status === 422,
    });

    if (!ok) {
        erroInesperado.add(1);
        return;
    }

    if (resOrdem.status === 422) {
        rejeitadas.add(1);
        return;
    }

    let ordemId;
    try {
        ordemId = JSON.parse(resOrdem.body).id;
    } catch (e) {
        erroInesperado.add(1);
        return;
    }

    let statusFinal = null;
    for (let i = 0; i < POLL_TENTATIVAS; i++) {
        sleep(POLL_INTERVALO);

        const resStatus = http.get(`${BASE_URL}/api/ordens/${ordemId}`, {
            headers: { Authorization: `Bearer ${dados.token}` },
        });
        if (resStatus.status !== 200) continue;

        try {
            const status = JSON.parse(resStatus.body).status;
            if (status === 'EXECUTADA' || status === 'REJEITADA') {
                statusFinal = status;
                break;
            }
        } catch (e) {
        }
    }

    if (statusFinal === 'EXECUTADA') {
        executadas.add(1);
    } else if (statusFinal === 'REJEITADA') {
        rejeitadas.add(1);
    } else {
        timeoutPolling.add(1);
    }
}

export function teardown(dados) {
    const res = http.get(`${BASE_URL}/api/ofertas/${TICKER}`);
    const oferta = JSON.parse(res.body);
    const esperadas = Math.min(dados.estoqueInicial, QTD_REQUISICOES);

    console.log('====================================');
    console.log(`Ativo:                    ${TICKER}`);
    console.log(`Estoque inicial:          ${dados.estoqueInicial}`);
    console.log(`Estoque final no banco:   ${oferta.quantidadeDisponivel}`);
    console.log(`Requisições enviadas:     ${QTD_REQUISICOES}`);
    console.log('====================================');
    console.log('Confira nas métricas finais do k6:');
    console.log(`  ordens_executadas      → deve ser ${esperadas} (estoque inicial, limitado às requisições)`);
    console.log(`  ordens_rejeitadas      → deve ser ${QTD_REQUISICOES - esperadas}`);
    console.log('  ordens_timeout_polling → deve ser 0 (se não for, aumente POLL_TENTATIVAS)');
    console.log('====================================');

    if (oferta.quantidadeDisponivel < 0) {
        estoqueInconsistente.add(1);
        console.error('FALHA: estoque ficou NEGATIVO — a trava pessimista não está protegendo contra concorrência.');
    } else if (oferta.quantidadeDisponivel !== dados.estoqueInicial - esperadas) {
        estoqueInconsistente.add(1);
        console.error(`ATENÇÃO: estoque final (${oferta.quantidadeDisponivel}) não bate com o esperado. Investigar.`);
    } else {
        console.log('Estoque final bateu com o esperado.');
    }
}
