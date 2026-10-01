// Fumaça: percorre uma vez cada bounded context pelo gateway para confirmar que o ecossistema está de pé.
//   k6 run k6/smoke.js
import { check, group } from 'k6';
import {
  CLIENTES_DE_DEMONSTRACAO, aguardarOrdem, aguardarPagamento, assinar, enviarOrdem, entrarComoCliente,
  entrarComoGerente, get, resumoEmTexto, solicitarDeposito,
} from './lib/orbita.js';

export const options = {
  vus: 1,
  iterations: 1,
  thresholds: {
    checks: ['rate==1'],
    http_req_failed: ['rate==0'],
  },
};

export default function () {
  const { email, pin } = CLIENTES_DE_DEMONSTRACAO.ana;
  let sessao;

  group('Autenticação', () => {
    sessao = entrarComoCliente(email, pin);
    const acesso = get('/api/autenticacao/me', sessao);
    check(acesso, { 'credencial do cliente': r => r.status === 200 && r.json('email') === email });
  });

  group('Clientes', () => {
    const me = get('/api/clientes/me', sessao);
    check(me, { 'cadastro do cliente': r => r.status === 200 && r.json('id') === sessao.clienteId });
  });

  group('Ativos', () => {
    check(get('/api/ativos'), { 'catálogo de ativos': r => r.status === 200 && r.json().length > 0 });
    check(get('/api/bolsas'), { 'bolsas': r => r.status === 200 && r.json().length > 0 });
  });

  group('Contas', () => {
    check(get('/api/contas/me', sessao), { 'conta do cliente': r => r.status === 200 && r.json('saldo') >= 0 });
  });

  group('Negociação (saga de compra)', () => {
    const assinatura = assinar(sessao.token, pin);
    const res = enviarOrdem(sessao.token, assinatura, 'PETR4', 'COMPRA', 1);
    check(res, { 'ordem aceita (202 PENDENTE)': r => r.status === 202 && r.json('status') === 'PENDENTE' });
    const ordem = aguardarOrdem(sessao.token, res.json('id'));
    check(ordem, { 'ordem executada pela saga': o => o !== null && o.status === 'EXECUTADA' });
  });

  group('Carteira', () => {
    const carteira = get('/api/carteiras/me', sessao);
    check(carteira, { 'posição de PETR4 na carteira': r => r.status === 200 && r.json('posicoes').some(p => p.ticker === 'PETR4') });
  });

  group('Pagamentos (ACL Pix)', () => {
    const cobranca = solicitarDeposito(sessao.token, 10, 'PIX');
    check(cobranca, { 'Pix copia e cola gerado': c => c.instrucoes.codigo.startsWith('000201') });
    const pago = aguardarPagamento(sessao.token, cobranca.id);
    check(pago, { 'Pix conciliado e confirmado': p => p !== null && p.status === 'CONFIRMADO' });
  });

  group('Relatórios', () => {
    check(get('/api/relatorios/me', sessao), { 'extrato de investimentos': r => r.status === 200 });
    const gerente = { token: entrarComoGerente() };
    check(get('/api/relatorios/gerencial', gerente), { 'relatório gerencial': r => r.status === 200 && r.json('clientes.ativos') > 0 });
  });
}

export function handleSummary(data) {
  const texto = resumoEmTexto('OrbitaPay · teste de fumaça', data, ['checks', 'http_reqs', 'http_req_duration', 'http_req_failed']);
  return { stdout: texto };
}
