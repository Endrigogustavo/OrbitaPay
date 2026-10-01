// Jornada completa de investidores simultâneos, cada um com cadastro próprio:
// cadastro (gerente) → login → depósito Pix → compras e vendas assinadas com PIN → carteira e extrato.
//   k6 run k6/jornada-investidor.js
//   k6 run -e INVESTIDORES=30 -e DURACAO=3m k6/jornada-investidor.js
import { check, group, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';
import {
  aguardarConta, aguardarOrdem, aguardarPagamento, aceitando, assinar, cadastrarInvestidor, enviarOrdem,
  entrarComoCliente, entrarComoGerente, get, resumoEmTexto, solicitarDeposito,
} from './lib/orbita.js';

const INVESTIDORES = Number(__ENV.INVESTIDORES || 10);
const DURACAO = __ENV.DURACAO || '1m';

const ordemAteConclusao = new Trend('ordem_ate_conclusao', true);
const depositoAteConfirmacao = new Trend('deposito_ate_confirmacao', true);
const ordensExecutadas = new Counter('ordens_executadas');
const ordensRejeitadas = new Counter('ordens_rejeitadas');

export const options = {
  scenarios: {
    investidores: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '20s', target: INVESTIDORES },
        { duration: DURACAO, target: INVESTIDORES },
        { duration: '10s', target: 0 },
      ],
      gracefulRampDown: '15s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.02'],
    'http_req_duration{tipo:leitura}': ['p(95)<500'],
    'http_req_duration{tipo:escrita}': ['p(95)<1500'],
    ordem_ate_conclusao: ['p(95)<5000'],
    deposito_ate_confirmacao: ['p(95)<10000'],
    checks: ['rate>0.97'],
  },
};

// Ativos líquidos (ORBT3 fica de fora: tem só 10 ações e é usado no teste de concorrência).
const CARTEIRA_ALVO = ['PETR4', 'VALE3', 'ITUB4', 'WEGE3', 'KO', 'SHEL'];

export function setup() {
  return { tokenDoGerente: entrarComoGerente() };
}

// Estado por VU: cada usuário virtual é um investidor diferente durante todo o teste.
let investidor = null;

function prepararInvestidor(tokenDoGerente) {
  const cadastro = cadastrarInvestidor(tokenDoGerente, 50000);
  const sessao = entrarComoCliente(cadastro.email, cadastro.pin);
  aguardarConta(sessao.token);
  return { ...sessao, pin: cadastro.pin, posicoes: {} };
}

export default function ({ tokenDoGerente }) {
  if (!investidor) {
    group('cadastro e login', () => { investidor = prepararInvestidor(tokenDoGerente); });
  }
  const { token, pin } = investidor;

  group('consulta do mercado', () => {
    check(get('/api/ativos', { token }), { 'catálogo': r => r.status === 200 });
    check(get('/api/contas/me', { token }), { 'saldo': r => r.status === 200 });
  });

  group('compra assinada', () => {
    const ticker = CARTEIRA_ALVO[Math.floor(Math.random() * CARTEIRA_ALVO.length)];
    const quantidade = 1 + Math.floor(Math.random() * 3);
    const assinatura = assinar(token, pin);
    const inicio = Date.now();
    const res = enviarOrdem(token, assinatura, ticker, 'COMPRA', quantidade, aceitando(422));
    if (!check(res, { 'compra aceita': r => r.status === 202 })) return;
    const ordem = aguardarOrdem(token, res.json('id'));
    ordemAteConclusao.add(Date.now() - inicio);
    if (ordem && ordem.status === 'EXECUTADA') {
      ordensExecutadas.add(1);
      investidor.posicoes[ticker] = (investidor.posicoes[ticker] || 0) + quantidade;
    } else {
      ordensRejeitadas.add(1);
    }
    check(ordem, { 'compra concluída pela saga': o => o !== null });
  });

  const comPosicao = Object.keys(investidor.posicoes).filter(t => investidor.posicoes[t] > 0);
  if (comPosicao.length && Math.random() < 0.3) {
    group('venda assinada', () => {
      const ticker = comPosicao[Math.floor(Math.random() * comPosicao.length)];
      const assinatura = assinar(token, pin);
      const inicio = Date.now();
      const res = enviarOrdem(token, assinatura, ticker, 'VENDA', 1, aceitando(422));
      if (!check(res, { 'venda aceita': r => r.status === 202 })) return;
      const ordem = aguardarOrdem(token, res.json('id'));
      ordemAteConclusao.add(Date.now() - inicio);
      if (ordem && ordem.status === 'EXECUTADA') {
        ordensExecutadas.add(1);
        investidor.posicoes[ticker] -= 1;
      } else {
        ordensRejeitadas.add(1);
      }
    });
  }

  if (Math.random() < 0.15) {
    group('depósito via Pix', () => {
      const inicio = Date.now();
      const cobranca = solicitarDeposito(token, 100 + Math.floor(Math.random() * 900), 'PIX');
      const pago = aguardarPagamento(token, cobranca.id);
      depositoAteConfirmacao.add(Date.now() - inicio);
      check(pago, { 'depósito confirmado': p => p !== null && p.status === 'CONFIRMADO' });
    });
  }

  group('carteira e extrato', () => {
    check(get('/api/carteiras/me', { token }), { 'carteira': r => r.status === 200 });
    check(get('/api/relatorios/me', { token }), { 'extrato de investimentos': r => r.status === 200 });
  });

  sleep(1);
}

export function handleSummary(data) {
  return {
    stdout: resumoEmTexto(`OrbitaPay · jornada de ${INVESTIDORES} investidores`, data, [
      'checks', 'iterations', 'http_reqs', 'http_req_duration', 'http_req_failed', 'ordem_ate_conclusao',
      'deposito_ate_confirmacao', 'ordens_executadas', 'ordens_rejeitadas',
    ]),
  };
}
