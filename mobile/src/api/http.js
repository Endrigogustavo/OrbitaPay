export class ErroDaApi extends Error {
  constructor(status, codigo, mensagem, detalhes) {
    super(mensagem);
    this.status = status;
    this.codigo = codigo;
    this.detalhes = detalhes || {};
  }
}

const lerJson = texto => {
  try {
    return texto ? JSON.parse(texto) : null;
  } catch (erro) {
    return null;
  }
};

export function criarClienteHttp(urlBase, credenciais) {
  return async function requisitar(metodo, caminho, opcoes = {}) {
    const { corpo, autenticacao, assinatura } = opcoes;
    const cabecalhos = { Accept: 'application/json' };
    if (corpo !== undefined) cabecalhos['Content-Type'] = 'application/json';
    const token = autenticacao ? credenciais[autenticacao]() : null;
    if (token) cabecalhos.Authorization = 'Bearer ' + token;
    if (assinatura) cabecalhos['X-Assinatura'] = assinatura;

    let resposta;
    try {
      resposta = await fetch(urlBase + caminho, {
        method: metodo,
        headers: cabecalhos,
        body: corpo === undefined ? undefined : JSON.stringify(corpo),
      });
    } catch (erro) {
      throw new ErroDaApi(0, 'SEM_CONEXAO', 'Sem conexão com o servidor Órbita', {});
    }

    const dados = lerJson(await resposta.text());
    if (!resposta.ok) {
      throw new ErroDaApi(resposta.status, (dados && dados.codigo) || 'ERRO',
        (dados && dados.mensagem) || 'Não foi possível concluir (' + resposta.status + ')', dados && dados.detalhes);
    }
    return dados;
  };
}
