import type {
  AcessoDto, AtivoDto, AtivoRequestDto, AtualizacaoDeClienteDto, CadastroDeClienteDto, CarteiraDto, ClienteDto, ComprovanteDto,
  ContaDto, CredencialDto, MetodoDeDeposito, MetodoDePagamentoDto, OrdemDto, OrdemRequestDto, PagamentoDto, SessaoDto,
} from '@/@types/api';
import { URL_DO_GATEWAY } from './configuracao';
import { criarClienteHttp, type Credenciais } from './http';

const CLIENTE = 'cliente';
const GERENTE = 'gerente';

const METODOS_DE_PAGAMENTO: Record<MetodoDeDeposito, MetodoDePagamentoDto> = { PIX: 'PIX', Boleto: 'BOLETO', TED: 'TED' };

export function criarOrbitaApi(credenciais: Credenciais) {
  const http = criarClienteHttp(URL_DO_GATEWAY, credenciais);

  return {
    autenticacao: {
      cliente: (email: string, pin: string) => http<SessaoDto>('POST', '/api/autenticacao/clientes', { corpo: { email, pin } }),
      gerente: (codigo: string) => http<CredencialDto>('POST', '/api/autenticacao/gerente', { corpo: { codigo } }),
      assinar: (pin: string) => http<CredencialDto>('POST', '/api/autenticacao/assinaturas', { corpo: { pin }, autenticacao: CLIENTE }),
      alterarPin: (pinAtual: string, novoPin: string) => http<void>('PUT', '/api/autenticacao/pin', { corpo: { pinAtual, novoPin }, autenticacao: CLIENTE }),
      meuAcesso: () => http<AcessoDto>('GET', '/api/autenticacao/me', { autenticacao: CLIENTE }),
    },

    clientes: {
      cadastrar: (dados: CadastroDeClienteDto) => http<ClienteDto>('POST', '/api/clientes', { corpo: dados }),
      cadastrarPeloGerente: (dados: CadastroDeClienteDto) => http<ClienteDto>('POST', '/api/clientes', { corpo: dados, autenticacao: GERENTE }),
      meu: () => http<ClienteDto>('GET', '/api/clientes/me', { autenticacao: CLIENTE }),
      atualizarMeu: (dados: AtualizacaoDeClienteDto) => http<ClienteDto>('PUT', '/api/clientes/me', { corpo: dados, autenticacao: CLIENTE }),
      bloquearMinhaConta: () => http<ClienteDto>('POST', '/api/clientes/me/bloqueio', { autenticacao: CLIENTE }),
      desbloquearMinhaConta: (assinatura: string) => http<ClienteDto>('POST', '/api/clientes/me/desbloqueio', { autenticacao: CLIENTE, assinatura }),
      encerrarMinhaConta: () => http<void>('DELETE', '/api/clientes/me', { autenticacao: CLIENTE }),
      listar: () => http<ClienteDto[]>('GET', '/api/clientes', { autenticacao: GERENTE }),
      atualizar: (id: string, dados: AtualizacaoDeClienteDto) => http<ClienteDto>('PUT', '/api/clientes/' + id, { corpo: dados, autenticacao: GERENTE }),
      bloquear: (id: string) => http<ClienteDto>('POST', '/api/clientes/' + id + '/bloqueio', { autenticacao: GERENTE }),
      desbloquear: (id: string) => http<ClienteDto>('POST', '/api/clientes/' + id + '/desbloqueio', { autenticacao: GERENTE }),
      remover: (id: string) => http<void>('DELETE', '/api/clientes/' + id, { autenticacao: GERENTE }),
    },

    contas: {
      minha: () => http<ContaDto>('GET', '/api/contas/me', { autenticacao: CLIENTE }),
      sacar: (valor: number, assinatura: string) => http<ComprovanteDto>('POST', '/api/contas/me/saques', { corpo: { valor }, autenticacao: CLIENTE, assinatura }),
      listar: () => http<ContaDto[]>('GET', '/api/contas', { autenticacao: GERENTE }),
    },

    pagamentos: {
      solicitar: (valor: number, metodo: MetodoDeDeposito) => http<PagamentoDto>('POST', '/api/pagamentos', { corpo: { valor, metodo: METODOS_DE_PAGAMENTO[metodo] }, autenticacao: CLIENTE }),
      buscar: (id: string) => http<PagamentoDto>('GET', '/api/pagamentos/' + id, { autenticacao: CLIENTE }),
      meus: () => http<PagamentoDto[]>('GET', '/api/pagamentos', { autenticacao: CLIENTE }),
    },

    ativos: {
      listar: () => http<AtivoDto[]>('GET', '/api/ativos'),
      listarNaBolsa: (dados: AtivoRequestDto) => http<AtivoDto>('POST', '/api/ativos', { corpo: dados, autenticacao: GERENTE }),
      atualizar: (ticker: string, dados: AtivoRequestDto) => http<AtivoDto>('PUT', '/api/ativos/' + ticker, { corpo: dados, autenticacao: GERENTE }),
      remover: (ticker: string) => http<void>('DELETE', '/api/ativos/' + ticker, { autenticacao: GERENTE }),
    },

    ordens: {
      enviar: (ordem: OrdemRequestDto, assinatura: string) => http<OrdemDto>('POST', '/api/ordens', { corpo: ordem, autenticacao: CLIENTE, assinatura }),
      buscar: (id: string) => http<OrdemDto>('GET', '/api/ordens/' + id, { autenticacao: CLIENTE }),
    },

    carteira: {
      minha: () => http<CarteiraDto>('GET', '/api/carteiras/me', { autenticacao: CLIENTE }),
    },
  };
}

export type OrbitaApi = ReturnType<typeof criarOrbitaApi>;
