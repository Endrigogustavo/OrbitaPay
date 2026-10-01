// Contratos HTTP expostos pelo gateway. Os campos BigDecimal chegam como número no JSON.

export type MotivoBloqueioDto = 'PIN' | 'GERENTE' | 'CLIENTE';

export interface ClienteDto {
  id: string;
  nome: string;
  email: string;
  cpf: string;
  bloqueado: boolean;
  motivoBloqueio: MotivoBloqueioDto | null;
  clienteDesde: string;
}

export interface CredencialDto {
  token: string;
  expiraEm: string;
}

export interface SessaoDto extends CredencialDto {
  clienteId: string;
}

/** Credencial de acesso guardada pelo contexto de Autenticação (PIN e tentativas). */
export interface AcessoDto {
  clienteId: string;
  email: string;
  tentativasFalhas: number;
  tentativasPermitidas: number;
}

export type TipoLancamentoDto = 'ABERTURA' | 'DEPOSITO' | 'SAQUE' | 'COMPRA_DE_ACOES' | 'VENDA_DE_ACOES';

export interface LancamentoDto {
  id: string;
  tipo: TipoLancamentoDto;
  valor: number;
  descricao: string;
  /** Ordem ou pagamento que originou o lançamento. */
  referencia: string | null;
  ocorridoEm: string;
}

export interface ContaDto {
  id: string;
  clienteId: string;
  titular: string;
  titularBloqueado: boolean;
  agencia: string;
  numero: string;
  saldo: number;
  lancamentos: LancamentoDto[];
  abertaEm: string;
}

export interface ComprovanteDto {
  lancamento: LancamentoDto;
  novoSaldo: number;
  protocolo: string;
}

export interface AtivoDto {
  ticker: string;
  nome: string;
  setor: string;
  bolsa: string;
  moeda: string;
  cambio: number;
  cotacao: number;
  abertura: number;
  variacaoPercentual: number;
  historico: number[];
  quantidadeEmitida: number;
}

export interface PosicaoDto {
  ticker: string;
  nome: string;
  quantidade: number;
  quantidadeReservada: number;
  precoMedio: number;
  cotacao: number;
  moeda: string;
  valorDeMercado: number;
  custo: number;
  resultadoPercentual: number;
}

export interface CarteiraDto {
  clienteId: string;
  posicoes: PosicaoDto[];
  valorDeMercado: number;
  custo: number;
}

export type TipoOrdemDto = 'COMPRA' | 'VENDA';
export type StatusOrdemDto = 'PENDENTE' | 'EXECUTADA' | 'REJEITADA';

export interface OrdemDto {
  id: string;
  ticker: string;
  tipo: TipoOrdemDto;
  quantidade: number;
  precoUnitario: number;
  moeda: string;
  cambio: number;
  valorTotal: number;
  status: StatusOrdemDto;
  motivoRejeicao: string | null;
  criadaEm: string;
  atualizadaEm: string;
}

export interface ErroDto {
  codigo?: string;
  mensagem?: string;
  detalhes?: Record<string, unknown>;
}

export interface CadastroDeClienteDto {
  nome: string;
  email: string;
  cpf: string;
  pin: string;
  depositoInicial?: number;
}

export interface AtualizacaoDeClienteDto {
  nome: string;
  email: string;
  cpf?: string;
}

export interface AtivoRequestDto {
  ticker: string;
  nome: string;
  setor: string;
  bolsa: string;
  cotacao: number;
}

export interface OrdemRequestDto {
  ticker: string;
  tipo: TipoOrdemDto;
  quantidade: number;
}

export type MetodoDeDeposito = 'PIX' | 'TED' | 'Boleto';

export type MetodoDePagamentoDto = 'PIX' | 'BOLETO' | 'TED';
export type StatusDoPagamentoDto = 'PENDENTE' | 'CONFIRMADO' | 'EXPIRADO';

export interface PagamentoDto {
  id: string;
  valor: number;
  metodo: MetodoDePagamentoDto;
  status: StatusDoPagamentoDto;
  instrucoes: { codigo: string; descricao: string; validoAte: string };
  valorPago: number | null;
  criadoEm: string;
  concluidoEm: string | null;
}
