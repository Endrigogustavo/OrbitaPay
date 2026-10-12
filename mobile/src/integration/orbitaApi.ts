import type {
  AcessoDto, AtivoDto, AtivoRequestDto, AtualizacaoDeClienteDto, CadastroDeClienteDto, CarteiraDto, ClienteDto, ComprovanteDto,
  ContaDto, CredencialDto, MetodoDeDeposito, MetodoDePagamentoDto, OrdemDto, OrdemRequestDto, PagamentoDto, SessaoDto,
} from '@/@types/api';
import { URL_DO_GATEWAY } from './configuracao';
import { criarClienteHttp, type LerToken } from './http';

const METODOS_DE_PAGAMENTO: Record<MetodoDeDeposito, MetodoDePagamentoDto> = { PIX: 'PIX', Boleto: 'BOLETO', TED: 'TED' };

export function criarOrbitaApi(lerToken: LerToken) {
  const http = criarClienteHttp(URL_DO_GATEWAY, lerToken);

  return {
    autenticacao: {
      entrar: (email: string, pin: string) => http<SessaoDto>('POST', '/api/autenticacao/sessoes', { corpo: { email, pin } }),
      assinar: (pin: string) => http<CredencialDto>('POST', '/api/autenticacao/assinaturas', { corpo: { pin }, autenticado: true }),
      alterarPin: (pinAtual: string, novoPin: string) => http<void>('PUT', '/api/autenticacao/pin', { corpo: { pinAtual, novoPin }, autenticado: true }),
      meuAcesso: () => http<AcessoDto>('GET', '/api/autenticacao/me', { autenticado: true }),
    },

    clientes: {
      cadastrar: (dados: CadastroDeClienteDto) => http<ClienteDto>('POST', '/api/clientes', { corpo: dados }),
      cadastrarPeloGerente: (dados: CadastroDeClienteDto) => http<ClienteDto>('POST', '/api/clientes', { corpo: dados, autenticado: true }),
      meu: () => http<ClienteDto>('GET', '/api/clientes/me', { autenticado: true }),
      atualizarMeu: (dados: AtualizacaoDeClienteDto) => http<ClienteDto>('PUT', '/api/clientes/me', { corpo: dados, autenticado: true }),
      bloquearMinhaConta: () => http<ClienteDto>('POST', '/api/clientes/me/bloqueio', { autenticado: true }),
      desbloquearMinhaConta: (assinatura: string) => http<ClienteDto>('POST', '/api/clientes/me/desbloqueio', { autenticado: true, assinatura }),
      encerrarMinhaConta: () => http<void>('DELETE', '/api/clientes/me', { autenticado: true }),
      listar: () => http<ClienteDto[]>('GET', '/api/clientes', { autenticado: true }),
      atualizar: (id: string, dados: AtualizacaoDeClienteDto) => http<ClienteDto>('PUT', '/api/clientes/' + id, { corpo: dados, autenticado: true }),
      bloquear: (id: string) => http<ClienteDto>('POST', '/api/clientes/' + id + '/bloqueio', { autenticado: true }),
      desbloquear: (id: string) => http<ClienteDto>('POST', '/api/clientes/' + id + '/desbloqueio', { autenticado: true }),
      remover: (id: string) => http<void>('DELETE', '/api/clientes/' + id, { autenticado: true }),
    },

    contas: {
      minha: () => http<ContaDto>('GET', '/api/contas/me', { autenticado: true }),
      sacar: (valor: number, assinatura: string) => http<ComprovanteDto>('POST', '/api/contas/me/saques', { corpo: { valor }, autenticado: true, assinatura }),
      listar: () => http<ContaDto[]>('GET', '/api/contas', { autenticado: true }),
    },

    pagamentos: {
      solicitar: (valor: number, metodo: MetodoDeDeposito) => http<PagamentoDto>('POST', '/api/pagamentos', { corpo: { valor, metodo: METODOS_DE_PAGAMENTO[metodo] }, autenticado: true }),
      buscar: (id: string) => http<PagamentoDto>('GET', '/api/pagamentos/' + id, { autenticado: true }),
      meus: () => http<PagamentoDto[]>('GET', '/api/pagamentos', { autenticado: true }),
    },

    ativos: {
      listar: () => http<AtivoDto[]>('GET', '/api/ativos'),
      listarNaBolsa: (dados: AtivoRequestDto) => http<AtivoDto>('POST', '/api/ativos', { corpo: dados, autenticado: true }),
      atualizar: (ticker: string, dados: AtivoRequestDto) => http<AtivoDto>('PUT', '/api/ativos/' + ticker, { corpo: dados, autenticado: true }),
      remover: (ticker: string) => http<void>('DELETE', '/api/ativos/' + ticker, { autenticado: true }),
    },

    ordens: {
      enviar: (ordem: OrdemRequestDto, assinatura: string) => http<OrdemDto>('POST', '/api/ordens', { corpo: ordem, autenticado: true, assinatura }),
      buscar: (id: string) => http<OrdemDto>('GET', '/api/ordens/' + id, { autenticado: true }),
    },

    carteira: {
      minha: () => http<CarteiraDto>('GET', '/api/carteiras/me', { autenticado: true }),
    },
  };
}

export type OrbitaApi = ReturnType<typeof criarOrbitaApi>;
