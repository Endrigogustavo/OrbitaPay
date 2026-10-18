import type {
  AcessoDto, AtivoDto, AtivoRequestDto, AtualizacaoDeClienteDto, CadastroDeClienteDto, CarteiraDto, ClienteDto, ComprovanteDto,
  ContaDto, CredencialCriadaDto, CredencialDto, MetodoDeDeposito, MetodoDePagamentoDto, OrdemDto, OrdemRequestDto, PagamentoDto, SessaoDto,
} from '@/@types/api';
import { URL_DO_GATEWAY } from './configuracao';
import { criarClienteHttp } from './http';

const METODOS_DE_PAGAMENTO: Record<MetodoDeDeposito, MetodoDePagamentoDto> = { PIX: 'PIX', Boleto: 'BOLETO', TED: 'TED' };

export function criarOrbitaApi() {
  const http = criarClienteHttp(URL_DO_GATEWAY);

  async function cadastrarCliente({ pin, ...dados }: CadastroDeClienteDto) {
    const acesso = await http<SessaoDto>('POST', '/api/autenticacao/credenciais', { corpo: { email: dados.email, pin } });
    try {
      const cliente = await http<ClienteDto>('POST', '/api/clientes', { corpo: dados });
      return { cliente, acesso };
    } catch (erro) {
      await http<void>('DELETE', '/api/autenticacao/credenciais/me').catch(() => {});
      throw erro;
    }
  }

  async function cadastrarClientePeloGerente({ pin, ...dados }: CadastroDeClienteDto) {
    const { clienteId } = await http<CredencialCriadaDto>('POST', '/api/autenticacao/credenciais', { corpo: { email: dados.email, pin } });
    try {
      const cliente = await http<ClienteDto>('POST', '/api/clientes', { corpo: { ...dados, clienteId } });
      return { cliente };
    } catch (erro) {
      await http<void>('DELETE', '/api/autenticacao/credenciais/' + clienteId).catch(() => {});
      throw erro;
    }
  }

  return {
    autenticacao: {
      entrar: (email: string, pin: string) => http<SessaoDto>('POST', '/api/autenticacao/sessoes', { corpo: { email, pin } }),
      sessaoAtual: () => http<SessaoDto>('GET', '/api/autenticacao/sessoes/atual'),
      sair: () => http<void>('DELETE', '/api/autenticacao/sessoes/atual'),
      tornarGerente: (nome: string, assinatura: string) => http<SessaoDto>('POST', '/api/autenticacao/me/gerente', { corpo: { nome }, assinatura }),
      assinar: (pin: string) => http<CredencialDto>('POST', '/api/autenticacao/assinaturas', { corpo: { pin } }),
      alterarPin: (pinAtual: string, novoPin: string) => http<void>('PUT', '/api/autenticacao/pin', { corpo: { pinAtual, novoPin } }),
      meuAcesso: () => http<AcessoDto>('GET', '/api/autenticacao/me'),
    },

    clientes: {
      cadastrar: cadastrarCliente,
      cadastrarPeloGerente: cadastrarClientePeloGerente,
      meu: () => http<ClienteDto>('GET', '/api/clientes/me'),
      atualizarMeu: (dados: AtualizacaoDeClienteDto) => http<ClienteDto>('PUT', '/api/clientes/me', { corpo: dados }),
      bloquearMinhaConta: () => http<ClienteDto>('POST', '/api/clientes/me/bloqueio'),
      desbloquearMinhaConta: (assinatura: string) => http<ClienteDto>('POST', '/api/clientes/me/desbloqueio', { assinatura }),
      encerrarMinhaConta: () => http<void>('DELETE', '/api/clientes/me'),
      listar: () => http<ClienteDto[]>('GET', '/api/clientes'),
      atualizar: (id: string, dados: AtualizacaoDeClienteDto) => http<ClienteDto>('PUT', '/api/clientes/' + id, { corpo: dados }),
      bloquear: (id: string) => http<ClienteDto>('POST', '/api/clientes/' + id + '/bloqueio'),
      desbloquear: (id: string) => http<ClienteDto>('POST', '/api/clientes/' + id + '/desbloqueio'),
      remover: (id: string) => http<void>('DELETE', '/api/clientes/' + id),
    },

    contas: {
      minha: () => http<ContaDto>('GET', '/api/contas/me'),
      sacar: (valor: number, assinatura: string) => http<ComprovanteDto>('POST', '/api/contas/me/saques', { corpo: { valor }, assinatura }),
      listar: () => http<ContaDto[]>('GET', '/api/contas'),
    },

    pagamentos: {
      solicitar: (valor: number, metodo: MetodoDeDeposito) => http<PagamentoDto>('POST', '/api/pagamentos', { corpo: { valor, metodo: METODOS_DE_PAGAMENTO[metodo] } }),
      buscar: (id: string) => http<PagamentoDto>('GET', '/api/pagamentos/' + id),
      meus: () => http<PagamentoDto[]>('GET', '/api/pagamentos'),
    },

    ativos: {
      listar: () => http<AtivoDto[]>('GET', '/api/ativos'),
      listarNaBolsa: (dados: AtivoRequestDto) => http<AtivoDto>('POST', '/api/ativos', { corpo: dados }),
      atualizar: (ticker: string, dados: AtivoRequestDto) => http<AtivoDto>('PUT', '/api/ativos/' + ticker, { corpo: dados }),
      remover: (ticker: string) => http<void>('DELETE', '/api/ativos/' + ticker),
    },

    ordens: {
      enviar: (ordem: OrdemRequestDto, assinatura: string) => http<OrdemDto>('POST', '/api/ordens', { corpo: ordem, assinatura }),
      buscar: (id: string) => http<OrdemDto>('GET', '/api/ordens/' + id),
    },

    carteira: {
      minha: () => http<CarteiraDto>('GET', '/api/carteiras/me'),
    },
  };
}

export type OrbitaApi = ReturnType<typeof criarOrbitaApi>;
