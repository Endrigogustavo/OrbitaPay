import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';
import exec from 'k6/execution';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:9000';
const EMAIL = __ENV.EMAIL || 'bruno@orbita.com';
const PIN = __ENV.PIN || '4321';
const TICKER = __ENV.TICKER || 'PETR4';
const QTD_REQUISICOES = Number(__ENV.QTD_REQUISICOES || 50);
// Quanto o total pedido pelas ordens passa do saldo: garante que o saldo se esgote no meio do teste.
const VEZES_O_SALDO = Number(__ENV.VEZES_O_SALDO || 4);
// Se informado, ignora o cálculo automático e usa esta quantidade de ações por ordem.
const QUANTIDADE = __ENV.QUANTIDADE ? Number(__ENV.QUANTIDADE) : null;
// ESTRITO=1 também reprova ordens que não fecharam (pendentes ou recusadas por outro motivo).
const ESTRITO = __ENV.ESTRITO === '1';

const POLL_TENTATIVAS = Number(__ENV.POLL_TENTATIVAS || 40);
const POLL_INTERVALO = Number(__ENV.POLL_INTERVALO || 0.5);

const executadas = new Counter('ordens_executadas');
const rejeitadasPorSaldo = new Counter('ordens_rejeitadas_por_saldo');
const rejeitadasOutroMotivo = new Counter('ordens_rejeitadas_outro_motivo');
const timeoutPolling = new Counter('ordens_timeout_polling');
const erroInesperado = new Counter('erro_inesperado');
const saldoInconsistente = new Counter('saldo_inconsistente');

const thresholds = {
    erro_inesperado: ['count==0'],
    saldo_inconsistente: ['count==0'],
};
if (ESTRITO) {
    thresholds.ordens_timeout_polling = ['count==0'];
    thresholds.ordens_rejeitadas_outro_motivo = ['count==0'];
}

export const options = {
    scenarios: {
        compra_sem_saldo: {
            executor: 'shared-iterations',
            vus: QTD_REQUISICOES,
            iterations: QTD_REQUISICOES,
            maxDuration: '60s',
        },
    },
    thresholds,
};

const centavos = (valor) => Math.round(Number(valor) * 100);
const reais = (emCentavos) => (emCentavos / 100).toFixed(2);

function consultarConta(sessao) {
    const res = http.get(`${BASE_URL}/api/contas/me`, { headers: { Cookie: `orbita_sessao=${sessao}` } });
    return JSON.parse(res.body);
}

function listarOrdens(sessao) {
    const res = http.get(`${BASE_URL}/api/ordens`, { headers: { Cookie: `orbita_sessao=${sessao}` } });
    return JSON.parse(res.body);
}

export function setup() {
    const params = { headers: { 'Content-Type': 'application/json' } };

    const resLogin = http.post(`${BASE_URL}/api/autenticacao/sessoes`, JSON.stringify({ email: EMAIL, pin: PIN }), params);
    if (resLogin.status !== 200 || !resLogin.cookies.orbita_sessao) {
        exec.test.abort(`Não foi possível entrar como ${EMAIL} (HTTP ${resLogin.status}: ${resLogin.body}). Confira -e EMAIL e -e PIN.`);
    }
    const sessao = resLogin.cookies.orbita_sessao[0].value;

    const resAssinatura = http.post(`${BASE_URL}/api/autenticacao/assinaturas`, JSON.stringify({ pin: PIN }), params);
    if (resAssinatura.status !== 200) {
        exec.test.abort(`Não foi possível assinar com o PIN de ${EMAIL} (HTTP ${resAssinatura.status}: ${resAssinatura.body}).`);
    }
    const assinatura = JSON.parse(resAssinatura.body).token;

    const ativo = JSON.parse(http.get(`${BASE_URL}/api/ativos/${TICKER}`).body);
    const precoPorAcao = centavos(ativo.cotacao * ativo.cambio);
    const saldoInicial = centavos(consultarConta(sessao).saldo);

    // Cada ordem cabe no saldo sozinha, mas todas juntas passam dele.
    const quantidade = QUANTIDADE
        || Math.max(1, Math.ceil((saldoInicial * VEZES_O_SALDO) / (QTD_REQUISICOES * precoPorAcao)));

    if (saldoInicial < quantidade * precoPorAcao) {
        console.warn(`AVISO: saldo de ${EMAIL} (R$ ${reais(saldoInicial)}) não paga nem uma ordem. `
            + 'O teste só vai conferir que todas foram barradas. Use outro cliente (-e EMAIL=... -e PIN=...) para um teste completo.');
    }

    const ordensAnteriores = listarOrdens(sessao).map((o) => o.id);

    return {
        sessao,
        assinatura,
        quantidade,
        precoPorAcao,
        saldoInicial,
        estoqueInicial: ativo.quantidadeDisponivel,
        ordensAnteriores,
    };
}

export default function (dados) {
    const payload = JSON.stringify({
        ticker: TICKER,
        tipo: 'COMPRA',
        quantidade: dados.quantidade,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            Cookie: `orbita_sessao=${dados.sessao}`,
            'X-Assinatura': dados.assinatura,
        },
        responseCallback: http.expectedStatuses(202),
    };

    const resOrdem = http.post(`${BASE_URL}/api/ordens`, payload, params);

    // A ordem entra PENDENTE e vai para a fila ordem.solicitada; o polling espera ela virar EXECUTADA ou REJEITADA.
    const ok = check(resOrdem, {
        'ordem aceita na fila (202)': (r) => r.status === 202,
    });

    if (!ok) {
        erroInesperado.add(1);
        return;
    }

    let ordemId;
    try {
        ordemId = JSON.parse(resOrdem.body).id;
    } catch (e) {
        erroInesperado.add(1);
        return;
    }

    let ordemFinal = null;
    for (let i = 0; i < POLL_TENTATIVAS; i++) {
        sleep(POLL_INTERVALO);

        const resStatus = http.get(`${BASE_URL}/api/ordens/${ordemId}`, {
            headers: { Cookie: `orbita_sessao=${dados.sessao}` },
        });
        if (resStatus.status !== 200) continue;

        try {
            const ordem = JSON.parse(resStatus.body);
            if (ordem.status === 'EXECUTADA' || ordem.status === 'REJEITADA') {
                ordemFinal = ordem;
                break;
            }
        } catch (e) {
        }
    }

    if (ordemFinal === null) {
        timeoutPolling.add(1);
    } else if (ordemFinal.status === 'EXECUTADA') {
        executadas.add(1);
    } else if ((ordemFinal.motivoRejeicao || '').startsWith('Saldo insuficiente')) {
        rejeitadasPorSaldo.add(1);
    } else {
        rejeitadasOutroMotivo.add(1);
    }
}

export function teardown(dados) {
    let falhas = 0;
    const falhar = (mensagem) => {
        falhas++;
        saldoInconsistente.add(1);
        console.error(`FALHA: ${mensagem}`);
    };

    const anteriores = new Set(dados.ordensAnteriores);
    const doTeste = listarOrdens(dados.sessao).filter((o) => !anteriores.has(o.id) && o.ticker === TICKER);
    const idsDoTeste = new Set(doTeste.map((o) => o.id));
    const comStatus = (status) => doTeste.filter((o) => o.status === status);
    const somaAcoes = (ordens) => ordens.reduce((soma, o) => soma + o.quantidade, 0);

    // O extrato da conta é a fonte da verdade do dinheiro: cada débito aponta a ordem que o gerou.
    const conta = consultarConta(dados.sessao);
    const saldoFinal = centavos(conta.saldo);
    const debitos = conta.lancamentos.filter((l) => l.tipo === 'COMPRA_DE_ACOES' && idsDoTeste.has(l.referencia));
    const ordensDebitadas = new Set(debitos.map((l) => l.referencia));
    const gasto = debitos.reduce((soma, l) => soma - centavos(l.valor), 0);

    const pendentes = comStatus('PENDENTE');
    const rejeitadas = comStatus('REJEITADA');
    const porSaldo = rejeitadas.filter((o) => (o.motivoRejeicao || '').startsWith('Saldo insuficiente'));
    const outroMotivo = rejeitadas.filter((o) => !porSaldo.includes(o));
    const executadasNoTeste = comStatus('EXECUTADA');

    const valorPorOrdem = dados.quantidade * dados.precoPorAcao;
    const cabemNoSaldo = Math.min(QTD_REQUISICOES, Math.floor(dados.saldoInicial / valorPorOrdem));
    const estoqueFinal = JSON.parse(http.get(`${BASE_URL}/api/ativos/${TICKER}`).body).quantidadeDisponivel;
    // Ações saem do estoque do ativo quando a compra executa e voltam quando ela é recusada.
    const estoqueEsperado = dados.estoqueInicial - somaAcoes(executadasNoTeste) - somaAcoes(pendentes);

    console.log('====================================');
    console.log(`Cliente:                  ${EMAIL}`);
    console.log(`Ativo:                    ${TICKER} (R$ ${reais(dados.precoPorAcao)} por ação)`);
    console.log(`Ordens enviadas:          ${QTD_REQUISICOES} x ${dados.quantidade} ações (~R$ ${reais(valorPorOrdem)} cada)`);
    console.log(`Saldo inicial:            R$ ${reais(dados.saldoInicial)} (paga no máximo ${cabemNoSaldo} ordens)`);
    console.log(`Debitado no extrato:      R$ ${reais(gasto)} (${ordensDebitadas.size} ordens)`);
    console.log(`Saldo final:              R$ ${reais(saldoFinal)}`);
    console.log(`Estoque:                  ${dados.estoqueInicial} -> ${estoqueFinal}`);
    console.log('------------------------------------');
    console.log(`Executadas:               ${executadasNoTeste.length}`);
    console.log(`Recusadas por saldo:      ${porSaldo.length}`);
    console.log(`Recusadas outro motivo:   ${outroMotivo.length}`);
    console.log(`Ainda pendentes:          ${pendentes.length}`);
    console.log('====================================');

    if (saldoFinal < 0) {
        falhar('saldo ficou NEGATIVO — a trava da conta não está protegendo contra compras simultâneas.');
    }
    if (gasto > dados.saldoInicial) {
        falhar(`foram debitados R$ ${reais(gasto)}, mais que o saldo inicial.`);
    }
    if (ordensDebitadas.size !== debitos.length) {
        falhar(`${debitos.length - ordensDebitadas.size} ordem(ns) debitada(s) mais de uma vez.`);
    }
    if (saldoFinal !== dados.saldoInicial - gasto) {
        falhar(`saldo final (R$ ${reais(saldoFinal)}) deveria ser R$ ${reais(dados.saldoInicial - gasto)} pelo extrato.`);
    }
    if (estoqueFinal !== estoqueEsperado) {
        falhar(`estoque final (${estoqueFinal}) deveria ser ${estoqueEsperado}. Reserva de ordem recusada não foi devolvida.`);
    }
    if (falhas === 0) {
        console.log('OK: nenhuma compra passou do saldo; saldo e estoque batem com o extrato.');
    }

    if (saldoFinal >= valorPorOrdem && ordensDebitadas.size < QTD_REQUISICOES) {
        console.warn('AVISO: o saldo não chegou a se esgotar, então o limite não foi testado. Aumente VEZES_O_SALDO ou QUANTIDADE.');
    }

    const naoFecharam = pendentes.length + outroMotivo.length;
    if (naoFecharam > 0) {
        const motivos = [...new Set(outroMotivo.map((o) => o.motivoRejeicao))].join(' | ');
        console.warn(`AVISO: ${naoFecharam} ordem(ns) não fecharam (${pendentes.length} pendentes, ${outroMotivo.length} recusadas por outro motivo${motivos ? `: ${motivos}` : ''}).`);
        console.warn('  Isso acontece quando algum serviço ficou fora do ar durante o teste. Não afeta a conferência do saldo.');
        console.warn(`  ${ESTRITO ? 'Reprovado por estar em ESTRITO=1.' : 'Rode com -e ESTRITO=1 para reprovar nesse caso.'}`);
    }
}
