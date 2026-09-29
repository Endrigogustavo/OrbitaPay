const MESES = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

const MOTIVOS = { PIN: 'pin', GERENTE: 'admin', CLIENTE: 'user' };

const TIPOS_DE_LANCAMENTO = {
  ABERTURA: 'sys',
  DEPOSITO: 'dep',
  SAQUE: 'saq',
  COMPRA_DE_ACOES: 'buy',
  VENDA_DE_ACOES: 'sell',
};

const numero = valor => Number(valor || 0);

const mesEAno = iso => {
  const data = new Date(iso);
  return isNaN(data) ? '—' : MESES[data.getMonth()] + ' ' + data.getFullYear();
};

export function paraAcao(ativo, anterior) {
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

export function paraMovimentacao(lancamento) {
  return {
    id: lancamento.id,
    type: TIPOS_DE_LANCAMENTO[lancamento.tipo] || 'sys',
    amt: numero(lancamento.valor),
    desc: lancamento.descricao,
    ts: Date.parse(lancamento.ocorridoEm),
  };
}

export function paraPosicoes(carteira) {
  const posicoes = {};
  ((carteira && carteira.posicoes) || []).forEach(p => {
    posicoes[p.ticker] = { qty: p.quantidade, reserved: p.quantidadeReservada, avg: numero(p.precoMedio) };
  });
  return posicoes;
}

export function paraUsuario(cliente, conta, carteira) {
  if (!cliente) return null;
  return {
    id: cliente.id,
    name: cliente.nome,
    email: cliente.email,
    cpf: cliente.cpf,
    blocked: cliente.bloqueado,
    reason: MOTIVOS[cliente.motivoBloqueio] || null,
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

export function paraClienteDoBackoffice(cliente, conta) {
  return {
    id: cliente.id,
    name: cliente.nome,
    email: cliente.email,
    cpf: cliente.cpf,
    blocked: cliente.bloqueado,
    reason: MOTIVOS[cliente.motivoBloqueio] || null,
    acct: conta ? conta.numero : '—',
    balance: conta ? numero(conta.saldo) : 0,
  };
}
