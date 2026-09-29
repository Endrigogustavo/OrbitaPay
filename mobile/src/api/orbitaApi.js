import { URL_DO_GATEWAY } from './configuracao';
import { criarClienteHttp } from './http';

const CLIENTE = 'cliente';
const GERENTE = 'gerente';

export function criarOrbitaApi(credenciais) {
  const http = criarClienteHttp(URL_DO_GATEWAY, credenciais);

  return {
    autenticacao: {
      cliente: (email, pin) => http('POST', '/api/autenticacao/clientes', { corpo: { email, pin } }),
      gerente: codigo => http('POST', '/api/autenticacao/gerente', { corpo: { codigo } }),
    },

    clientes: {
      cadastrar: dados => http('POST', '/api/clientes', { corpo: dados }),
      cadastrarPeloGerente: dados => http('POST', '/api/clientes', { corpo: dados, autenticacao: GERENTE }),
      meu: () => http('GET', '/api/clientes/me', { autenticacao: CLIENTE }),
      atualizarMeu: dados => http('PUT', '/api/clientes/me', { corpo: dados, autenticacao: CLIENTE }),
      alterarPin: (pinAtual, novoPin) => http('PUT', '/api/clientes/me/pin', { corpo: { pinAtual, novoPin }, autenticacao: CLIENTE }),
      assinar: pin => http('POST', '/api/clientes/me/assinaturas', { corpo: { pin }, autenticacao: CLIENTE }),
      bloquearMinhaConta: () => http('POST', '/api/clientes/me/bloqueio', { autenticacao: CLIENTE }),
      desbloquearMinhaConta: pin => http('POST', '/api/clientes/me/desbloqueio', { corpo: { pin }, autenticacao: CLIENTE }),
      encerrarMinhaConta: () => http('DELETE', '/api/clientes/me', { autenticacao: CLIENTE }),
      listar: () => http('GET', '/api/clientes', { autenticacao: GERENTE }),
      atualizar: (id, dados) => http('PUT', '/api/clientes/' + id, { corpo: dados, autenticacao: GERENTE }),
      bloquear: id => http('POST', '/api/clientes/' + id + '/bloqueio', { autenticacao: GERENTE }),
      desbloquear: id => http('POST', '/api/clientes/' + id + '/desbloqueio', { autenticacao: GERENTE }),
      remover: id => http('DELETE', '/api/clientes/' + id, { autenticacao: GERENTE }),
    },

    contas: {
      minha: () => http('GET', '/api/contas/me', { autenticacao: CLIENTE }),
      depositar: (valor, metodo) => http('POST', '/api/contas/me/depositos', { corpo: { valor, metodo }, autenticacao: CLIENTE }),
      sacar: (valor, assinatura) => http('POST', '/api/contas/me/saques', { corpo: { valor }, autenticacao: CLIENTE, assinatura }),
      listar: () => http('GET', '/api/contas', { autenticacao: GERENTE }),
    },

    ativos: {
      listar: () => http('GET', '/api/ativos'),
      listarNaBolsa: dados => http('POST', '/api/ativos', { corpo: dados, autenticacao: GERENTE }),
      atualizar: (ticker, dados) => http('PUT', '/api/ativos/' + ticker, { corpo: dados, autenticacao: GERENTE }),
      remover: ticker => http('DELETE', '/api/ativos/' + ticker, { autenticacao: GERENTE }),
    },

    ordens: {
      enviar: (ordem, assinatura) => http('POST', '/api/ordens', { corpo: ordem, autenticacao: CLIENTE, assinatura }),
      buscar: id => http('GET', '/api/ordens/' + id, { autenticacao: CLIENTE }),
    },

    carteira: {
      minha: () => http('GET', '/api/carteiras/me', { autenticacao: CLIENTE }),
    },
  };
}
