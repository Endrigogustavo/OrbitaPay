// Carga nas leituras públicas do mercado (o que o app consulta a cada poucos segundos).
//   k6 run k6/carga-mercado.js
//   k6 run -e TAXA=200 -e DURACAO=2m k6/carga-mercado.js
import { check } from 'k6';
import { get, resumoEmTexto } from './lib/orbita.js';

const TAXA = Number(__ENV.TAXA || 50);
const DURACAO = __ENV.DURACAO || '1m';

export const options = {
  scenarios: {
    mercado: {
      executor: 'ramping-arrival-rate',
      startRate: Math.max(1, Math.floor(TAXA / 5)),
      timeUnit: '1s',
      preAllocatedVUs: 20,
      maxVUs: 200,
      stages: [
        { duration: '15s', target: TAXA },
        { duration: DURACAO, target: TAXA },
        { duration: '10s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<300', 'p(99)<800'],
    'http_req_duration{name:GET /api/ativos}': ['p(95)<400'],
    checks: ['rate>0.99'],
  },
};

export function setup() {
  const res = get('/api/ativos');
  if (res.status !== 200) throw new Error(`catálogo indisponível: ${res.status}`);
  return { tickers: res.json().map(a => a.ticker) };
}

export default function ({ tickers }) {
  const ticker = tickers[Math.floor(Math.random() * tickers.length)];
  const sorteio = Math.random();
  if (sorteio < 0.5) {
    check(get('/api/ativos', {}, { nome: 'GET /api/ativos' }), { 'lista de ativos': r => r.status === 200 });
  } else if (sorteio < 0.75) {
    check(get(`/api/ofertas/${ticker}`, {}, { nome: 'GET /api/ofertas/{ticker}' }), { 'oferta do ativo': r => r.status === 200 });
  } else if (sorteio < 0.95) {
    check(get(`/api/ativos/${ticker}`, {}, { nome: 'GET /api/ativos/{ticker}' }), { 'detalhe do ativo': r => r.status === 200 });
  } else {
    check(get('/api/bolsas', {}, { nome: 'GET /api/bolsas' }), { 'bolsas': r => r.status === 200 });
  }
}

export function handleSummary(data) {
  return { stdout: resumoEmTexto(`OrbitaPay · carga no mercado (${TAXA} req/s)`, data, ['checks', 'http_reqs', 'http_req_duration', 'http_req_failed', 'iterations', 'dropped_iterations']) };
}
