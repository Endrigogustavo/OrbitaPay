import type { ErroDto } from '@/@types/api';

export type Perfil = 'cliente' | 'gerente';
export type Credenciais = Record<Perfil, () => string | null>;
export type MetodoHttp = 'GET' | 'POST' | 'PUT' | 'DELETE';

export interface OpcoesDaRequisicao {
  corpo?: unknown;
  autenticacao?: Perfil;
  assinatura?: string;
}

export class ErroDaApi extends Error {
  readonly status: number;
  readonly codigo: string;
  readonly detalhes: Record<string, unknown>;

  constructor(status: number, codigo: string, mensagem: string, detalhes?: Record<string, unknown>) {
    super(mensagem);
    this.status = status;
    this.codigo = codigo;
    this.detalhes = detalhes || {};
  }
}

const lerJson = (texto: string): unknown => {
  try {
    return texto ? JSON.parse(texto) : null;
  } catch {
    return null;
  }
};

export type Requisitar = <T>(metodo: MetodoHttp, caminho: string, opcoes?: OpcoesDaRequisicao) => Promise<T>;

export function criarClienteHttp(urlBase: string, credenciais: Credenciais): Requisitar {
  return async function requisitar<T>(metodo: MetodoHttp, caminho: string, opcoes: OpcoesDaRequisicao = {}): Promise<T> {
    const { corpo, autenticacao, assinatura } = opcoes;
    const cabecalhos: Record<string, string> = { Accept: 'application/json' };
    if (corpo !== undefined) cabecalhos['Content-Type'] = 'application/json';
    const token = autenticacao ? credenciais[autenticacao]() : null;
    if (token) cabecalhos.Authorization = 'Bearer ' + token;
    if (assinatura) cabecalhos['X-Assinatura'] = assinatura;

    let resposta: Response;
    try {
      resposta = await fetch(urlBase + caminho, {
        method: metodo,
        headers: cabecalhos,
        body: corpo === undefined ? undefined : JSON.stringify(corpo),
      });
    } catch {
      throw new ErroDaApi(0, 'SEM_CONEXAO', 'Sem conexão com o servidor Órbita', {});
    }

    const dados = lerJson(await resposta.text());
    if (!resposta.ok) {
      const erro = (dados || {}) as ErroDto;
      throw new ErroDaApi(resposta.status, erro.codigo || 'ERRO',
        erro.mensagem || 'Não foi possível concluir (' + resposta.status + ')', erro.detalhes);
    }
    return dados as T;
  };
}

export const ehErroDaApi = (erro: unknown): erro is ErroDaApi => erro instanceof ErroDaApi;

/** Normaliza qualquer falha para ErroDaApi, para as telas tratarem um único formato. */
export const comoErroDaApi = (erro: unknown): ErroDaApi =>
  ehErroDaApi(erro) ? erro : new ErroDaApi(-1, 'ERRO', erro instanceof Error ? erro.message : 'Erro inesperado');
