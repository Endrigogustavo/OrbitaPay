import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';
import {
  CLIENTES_DE_DEMONSTRACAO, aceitando, aguardarOrdem, assinar, enviarOrdem, entrarComoCliente, get, post,
  resumoEmTexto,
} from './lib/orbita.js';

const ORDENS = 20;
const SAQUES = 10;
const VALOR_DO_SAQUE = 500;
const TICKER = 'ORBT3';

const ordensAceitas = new Counter('ordens_aceitas');
const ordensRecusadasNaEntrada = new Counter('ordens_recusadas_na_entrada');
const ordensExecutadas = new Counter('ordens_executadas');
const ordensRejeitadas = new Counter('ordens_rejeitadas_pela_saga');
const saquesAprovados = new Counter('saques_aprovados');
const saquesRecusados = new Counter('saques_recusados');
const conflitos = new Counter('recursos_ocupados_409');

export const options = {
  scenarios: {
    compras: { executor: 'per-vu-iterations', exec: 'comprar', vus: ORDENS, iterations: 1, maxDuration: '60s' },
    saques: { executor: 'per-vu-iterations', exec: 'sacar', vus: SAQUES, iterations: 1, startTime: '25s', maxDuration: '30s' },
  },
  thresholds: {
    checks: ['rate==1'],
    recursos_ocupados_409: ['count==0'],
  },
};

export function setup() {
  const { ana, bruno } = CLIENTES_DE_DEMONSTRACAO;
  const sessaoAna = entrarComoCliente(ana.email, ana.pin);
  const sessaoBruno = entrarComoCliente(bruno.email, bruno.pin);
  const oferta = get(`/api/ofertas/${TICKER}`).json();
  const conta = get('/api/contas/me', sessaoBruno).json();
  const dados = {
    inicio: new Date().toISOString(),
    ana: { token: sessaoAna.token, assinatura: assinar(sessaoAna.token, ana.pin) },
    bruno: { token: sessaoBruno.token, assinatura: assinar(sessaoBruno.token, bruno.pin) },
    ofertaInicial: oferta.quantidadeDisponivel,
    saldoInicial: conta.saldo,
  };
  console.log(`Estado inicial: ${TICKER} disponível=${dados.ofertaInicial} · saldo do Bruno=R$ ${dados.saldoInicial}`);
  return dados;
}

export function comprar({ ana }) {
  const res = enviarOrdem(ana.token, ana.assinatura, TICKER, 'COMPRA', 1, aceitando(409, 422));
  if (res.status === 409) return conflitos.add(1);
  if (res.status === 422) return ordensRecusadasNaEntrada.add(1);
  ordensAceitas.add(1);
  const ordem = aguardarOrdem(ana.token, res.json('id'));
  if (ordem && ordem.status === 'EXECUTADA') ordensExecutadas.add(1);
  else ordensRejeitadas.add(1);
}

export function sacar({ bruno }) {
  const res = post('/api/contas/me/saques', { valor: VALOR_DO_SAQUE }, bruno,
    { nome: 'POST /api/contas/me/saques', responseCallback: aceitando(409, 422) });
  if (res.status === 409) conflitos.add(1);
  else if (res.status === 200) saquesAprovados.add(1);
  else saquesRecusados.add(1);
}

export function teardown(dados) {
  sleep(1);
  const inicio = Date.parse(dados.inicio);
  const ofertaFinal = get(`/api/ofertas/${TICKER}`).json('quantidadeDisponivel');
  const executadas = get('/api/ordens', { token: dados.ana.token }).json()
    .filter(o => o.ticker === TICKER && o.status === 'EXECUTADA' && Date.parse(o.criadaEm) >= inicio).length;
  const conta = get('/api/contas/me', { token: dados.bruno.token }).json();
  const saques = conta.lancamentos.filter(l => l.tipo === 'SAQUE' && Date.parse(l.ocorridoEm) >= inicio).length;
  const esperadasNaCompra = Math.min(ORDENS, dados.ofertaInicial);
  const esperadosNoSaque = Math.min(SAQUES, Math.floor(dados.saldoInicial / VALOR_DO_SAQUE));
  const saldoEsperado = Math.round((dados.saldoInicial - saques * VALOR_DO_SAQUE) * 100) / 100;

  console.log(`${TICKER}: ${executadas} executadas (esperado ${esperadasNaCompra}) · oferta final ${ofertaFinal}`);
  console.log(`Saques: ${saques} aprovados (esperado ${esperadosNoSaque}) · saldo final R$ ${conta.saldo}`);

  check(null, {
    [`${TICKER}: executadas = min(${ORDENS}, oferta inicial)`]: () => executadas === esperadasNaCompra,
    [`${TICKER}: oferta final = inicial − executadas`]: () => ofertaFinal === dados.ofertaInicial - executadas,
    [`${TICKER}: nenhuma ação vendida além do disponível`]: () => ofertaFinal >= 0,
    [`Saques: aprovados = min(${SAQUES}, saldo ÷ ${VALOR_DO_SAQUE})`]: () => saques === esperadosNoSaque,
    ['Saques: saldo final = inicial − saques']: () => Math.abs(conta.saldo - saldoEsperado) < 0.001,
    ['Saques: saldo nunca negativo']: () => conta.saldo >= 0,
  });
}

export function handleSummary(data) {
  const texto = resumoEmTexto('OrbitaPay · teste de concorrência (trava pessimista)', data, [
    'ordens_aceitas', 'ordens_recusadas_na_entrada', 'ordens_executadas', 'ordens_rejeitadas_pela_saga',
    'saques_aprovados', 'saques_recusados', 'recursos_ocupados_409', 'http_req_duration',
  ]);
  const arquivo = `relatorio-concorrencia-${new Date().toISOString().replace(/[:.]/g, '-')}.txt`;
  return { stdout: texto, [arquivo]: texto };
}
