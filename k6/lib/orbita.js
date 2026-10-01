// Cliente HTTP do gateway OrbitaPay para os testes k6. Todas as requisições passam pelo gateway (:8080),
// exatamente como o app mobile faz.
import http from 'k6/http';
import { check, fail, sleep } from 'k6';

export const BASE_URL = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
export const CODIGO_DO_GERENTE = __ENV.CODIGO_GERENTE || '0000';

export const CLIENTES_DE_DEMONSTRACAO = {
  ana: { email: 'ana@orbita.com', pin: '1234' },
  bruno: { email: 'bruno@orbita.com', pin: '4321' },
};

/** Status que o teste considera respostas válidas além de 2xx (ex.: 422 de regra de negócio esperada). */
export const aceitando = (...status) => http.expectedStatuses({ min: 200, max: 299 }, ...status);

function cabecalhos({ token, assinatura } = {}) {
  const h = { 'Content-Type': 'application/json', Accept: 'application/json' };
  if (token) h.Authorization = `Bearer ${token}`;
  if (assinatura) h['X-Assinatura'] = assinatura;
  return h;
}

function parametros(credenciais, nome, tipo, responseCallback) {
  const p = { headers: cabecalhos(credenciais), tags: { name: nome, tipo } };
  if (responseCallback) p.responseCallback = responseCallback;
  return p;
}

export function get(caminho, credenciais = {}, { nome = caminho, responseCallback } = {}) {
  return http.get(BASE_URL + caminho, parametros(credenciais, nome, 'leitura', responseCallback));
}

export function post(caminho, corpo, credenciais = {}, { nome = caminho, responseCallback } = {}) {
  return http.post(BASE_URL + caminho, corpo === undefined ? null : JSON.stringify(corpo),
    parametros(credenciais, nome, 'escrita', responseCallback));
}

export function put(caminho, corpo, credenciais = {}, { nome = caminho, responseCallback } = {}) {
  return http.put(BASE_URL + caminho, JSON.stringify(corpo), parametros(credenciais, nome, 'escrita', responseCallback));
}

function exigir(res, descricao, status = 200) {
  const ok = check(res, { [`${descricao} → ${status}`]: r => r.status === status });
  if (!ok) fail(`${descricao} falhou: HTTP ${res.status} ${res.body}`);
  return res;
}

// ---------- Autenticação ----------

export function entrarComoCliente(email, pin) {
  const res = exigir(post('/api/autenticacao/clientes', { email, pin }), 'login do cliente');
  return { token: res.json('token'), clienteId: res.json('clienteId') };
}

export function entrarComoGerente(codigo = CODIGO_DO_GERENTE) {
  return exigir(post('/api/autenticacao/gerente', { codigo }), 'login do gerente').json('token');
}

/** Assinatura de curta duração exigida pelo gateway em saques, ordens e desbloqueio. */
export function assinar(token, pin) {
  return exigir(post('/api/autenticacao/assinaturas', { pin }, { token }), 'assinatura com PIN').json('token');
}

// ---------- Clientes ----------

function cpfAleatorio() {
  let cpf = '';
  for (let i = 0; i < 11; i++) cpf += Math.floor(Math.random() * 10);
  return cpf;
}

/** Cadastra um cliente exclusivo para o teste, já com saldo inicial (só o gerente pode informar o depósito). */
export function cadastrarInvestidor(tokenDoGerente, depositoInicial = 50000) {
  const marca = `${__VU}-${Date.now()}-${Math.floor(Math.random() * 1e6)}`;
  const dados = { nome: `Investidor K6 ${marca}`, email: `k6.${marca}@orbita.com`, cpf: cpfAleatorio(), pin: '2468', depositoInicial };
  const res = exigir(post('/api/clientes', dados, { token: tokenDoGerente }, { nome: 'POST /api/clientes' }), 'cadastro do investidor', 201);
  return { id: res.json('id'), email: dados.email, pin: dados.pin };
}

/** A conta é aberta de forma assíncrona pelo contexto de Contas depois do evento cliente.cadastrado. */
export function aguardarConta(token, tentativas = 40) {
  for (let i = 0; i < tentativas; i++) {
    const res = get('/api/contas/me', { token }, { responseCallback: aceitando(404) });
    if (res.status === 200) return res.json();
    sleep(0.25);
  }
  fail('a conta não foi aberta a tempo');
}

// ---------- Negociação ----------

export function enviarOrdem(token, assinatura, ticker, tipo, quantidade, responseCallback) {
  return post('/api/ordens', { ticker, tipo, quantidade }, { token, assinatura }, { nome: 'POST /api/ordens', responseCallback });
}

/** Acompanha a saga até a ordem sair de PENDENTE. Retorna a ordem final (ou null se não terminou). */
export function aguardarOrdem(token, ordemId, tentativas = 60, intervalo = 0.25) {
  for (let i = 0; i < tentativas; i++) {
    const res = get(`/api/ordens/${ordemId}`, { token }, { nome: 'GET /api/ordens/{id}' });
    if (res.status === 200 && res.json('status') !== 'PENDENTE') return res.json();
    sleep(intervalo);
  }
  return null;
}

// ---------- Pagamentos ----------

export function solicitarDeposito(token, valor, metodo = 'PIX') {
  return exigir(post('/api/pagamentos', { valor, metodo }, { token }, { nome: 'POST /api/pagamentos' }), `cobrança ${metodo}`, 201).json();
}

export function aguardarPagamento(token, pagamentoId, tentativas = 40, intervalo = 0.5) {
  for (let i = 0; i < tentativas; i++) {
    const res = get(`/api/pagamentos/${pagamentoId}`, { token }, { nome: 'GET /api/pagamentos/{id}' });
    if (res.status === 200 && res.json('status') !== 'PENDENTE') return res.json();
    sleep(intervalo);
  }
  return null;
}

// ---------- Relatório de saída ----------

function linhasDeChecks(grupo, prefixo = '') {
  const linhas = [];
  for (const c of grupo.checks || []) {
    const total = c.passes + c.fails;
    linhas.push(`${c.fails === 0 ? '[OK]  ' : '[FALHA]'} ${prefixo}${c.name} (${c.passes}/${total})`);
  }
  for (const g of grupo.groups || []) linhas.push(...linhasDeChecks(g, `${prefixo}${g.name} › `));
  return linhas;
}

function valorDaMetrica(m) {
  const v = m.values;
  if (m.type === 'counter') return `${v.count}`;
  if (m.type === 'rate') return `${(v.rate * 100).toFixed(2)}%`;
  if (m.type === 'trend') return `média ${v.avg.toFixed(1)}ms · p95 ${v['p(95)'].toFixed(1)}ms · máx ${v.max.toFixed(1)}ms`;
  return JSON.stringify(v);
}

/** Resumo em texto puro (sem dependências externas), usado no handleSummary de cada script. */
export function resumoEmTexto(titulo, data, metricas) {
  const linhas = [
    '='.repeat(72),
    ` ${titulo}`,
    ` Gateway: ${BASE_URL} · gerado em ${new Date().toISOString()}`,
    '='.repeat(72),
    '',
    'VERIFICAÇÕES',
    ...linhasDeChecks(data.root_group),
    '',
    'MÉTRICAS',
  ];
  for (const nome of metricas) {
    const m = data.metrics[nome];
    if (m) linhas.push(`  ${nome.padEnd(32)} ${valorDaMetrica(m)}`);
  }
  const limites = Object.entries(data.metrics).filter(([, m]) => m.thresholds);
  if (limites.length) {
    linhas.push('', 'LIMITES (thresholds)');
    for (const [nome, m] of limites) {
      for (const [regra, r] of Object.entries(m.thresholds)) linhas.push(`  ${r.ok ? '[OK]  ' : '[FALHA]'} ${nome}: ${regra}`);
    }
  }
  return linhas.join('\n') + '\n';
}
