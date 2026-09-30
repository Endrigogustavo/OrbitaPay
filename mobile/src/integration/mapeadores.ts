import type { AtivoDto, CarteiraDto, ClienteDto, ContaDto, LancamentoDto, MotivoBloqueioDto, TipoLancamentoDto } from '@/@types/api';
import type { Acao, ClienteDoBackoffice, MotivoBloqueio, Movimentacao, Posicoes, TipoMovimentacao, Usuario } from '@/@types/orbita';

const MESES = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

const MOTIVOS: Record<MotivoBloqueioDto, MotivoBloqueio> = { PIN: 'pin', GERENTE: 'admin', CLIENTE: 'user' };

const TIPOS_DE_LANCAMENTO: Record<TipoLancamentoDto, TipoMovimentacao> = {
  ABERTURA: 'sys',
  DEPOSITO: 'dep',
  SAQUE: 'saq',
  COMPRA_DE_ACOES: 'buy',
  VENDA_DE_ACOES: 'sell',
};

const numero = (valor: number | string | null | undefined): number => Number(valor || 0);

const mesEAno = (iso: string): string => {
  const data = new Date(iso);
  return isNaN(data.getTime()) ? '—' : MESES[data.getMonth()] + ' ' + data.getFullYear();
};

const motivo = (dto: MotivoBloqueioDto | null): MotivoBloqueio | null => (dto && MOTIVOS[dto]) || null;

export function paraAcao(ativo: AtivoDto, anterior?: Acao): Acao {
  const preco = numero(ativo.cotacao);
  const flash = anterior && anterior.price !== preco ? (preco >= anterior.price ? 'up' : 'down') : null;
  return {
    ticker: ativo.ticker,
    name: ativo.nome,
    sector: ativo.setor,
    ex: ativo.bolsa,
    cur: ativo.moeda,
    fx: numero(ativo.cambio),
    price: preco,
    hist: (ativo.historico && ativo.historico.length ? ativo.historico : [preco]).map(numero),
    issued: ativo.quantidadeEmitida,
    flash,
  };
}

export function paraMovimentacao(lancamento: LancamentoDto): Movimentacao {
  return {
    id: lancamento.id,
    type: TIPOS_DE_LANCAMENTO[lancamento.tipo] || 'sys',
    amt: numero(lancamento.valor),
    desc: lancamento.descricao,
    ts: Date.parse(lancamento.ocorridoEm),
  };
}

export function paraPosicoes(carteira: CarteiraDto | null): Posicoes {
  const posicoes: Posicoes = {};
  (carteira?.posicoes || []).forEach(p => {
    posicoes[p.ticker] = { qty: p.quantidade, reserved: p.quantidadeReservada, avg: numero(p.precoMedio) };
  });
  return posicoes;
}

export function paraUsuario(cliente: ClienteDto | null, conta: ContaDto | null, carteira: CarteiraDto | null): Usuario | null {
  if (!cliente) return null;
  return {
    id: cliente.id,
    name: cliente.nome,
    email: cliente.email,
    cpf: cliente.cpf,
    blocked: cliente.bloqueado,
    reason: motivo(cliente.motivoBloqueio),
    fails: cliente.tentativasFalhas,
    maxFails: cliente.tentativasPermitidas,
    since: mesEAno(cliente.clienteDesde),
    acct: conta ? conta.numero : '—',
    agency: conta ? conta.agencia : '0001',
    balance: conta ? numero(conta.saldo) : 0,
    tx: conta ? conta.lancamentos.map(paraMovimentacao) : [],
    holdings: paraPosicoes(carteira),
  };
}

export function paraClienteDoBackoffice(cliente: ClienteDto, conta?: ContaDto): ClienteDoBackoffice {
  return {
    id: cliente.id,
    name: cliente.nome,
    email: cliente.email,
    cpf: cliente.cpf,
    blocked: cliente.bloqueado,
    reason: motivo(cliente.motivoBloqueio),
    acct: conta ? conta.numero : '—',
    balance: conta ? numero(conta.saldo) : 0,
  };
}
