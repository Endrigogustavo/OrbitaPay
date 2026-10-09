import type { OrdemDto } from '@/@types/api';
import type { FolhaDo } from '@/@types/orbita';
import { EXM } from '@/constants/bolsas';
import { brl, esperar, money } from '@/constants/formatacao';
import { IC } from '@/constants/icones';
import { comoErroDaApi } from '@/integration/http';
import type { Ferramentas } from './ferramentas';

const TENTATIVAS_DE_ACOMPANHAMENTO = 40;

type FolhaDeOrdem = FolhaDo<'pin'> & { purpose: 'trade' };

export function criarOperacoesDeNegociacao(f: Ferramentas) {
  const { api, usuario, carregarCliente } = f.sessao;

  function abrirAtivo(ticker: string) {
    f.abrir({ kind: 'stock', ticker, mode: 'buy' }, { qty: 1 });
  }

  function confirmarOrdem() {
    const folha = f.folha;
    if (!folha || folha.kind !== 'stock' || !usuario) return;
    const acao = f.mercado.porTicker[folha.ticker];
    if (!acao) return;
    const total = +(f.quantidade * acao.price * acao.fx).toFixed(2);
    const posicao = usuario.holdings[acao.ticker];
    const disponivelParaVenda = posicao ? posicao.qty - posicao.reserved : 0;

    if (usuario.blocked) return f.abrir({ kind: 'locked' });
    if (folha.mode === 'buy' && total > usuario.balance) {
      return f.falhar('Saldo insuficiente para ' + f.quantidade + ' ' + acao.ticker + ' · faltam '
        + brl(total - usuario.balance));
    }
    if (folha.mode === 'sell' && f.quantidade > disponivelParaVenda) {
      return f.falhar(disponivelParaVenda
        ? 'Você tem apenas ' + disponivelParaVenda + ' ' + acao.ticker
        : 'Você não possui ' + acao.ticker);
    }
    f.abrir({
      kind: 'pin', purpose: 'trade', mode: folha.mode, ticker: acao.ticker, qty: f.quantidade, price: acao.price, total,
    });
  }

  async function acompanharOrdem(ordemId: string): Promise<OrdemDto | null> {
    for (let tentativa = 0; tentativa < TENTATIVAS_DE_ACOMPANHAMENTO; tentativa++) {
      const ordem = await api.ordens.buscar(ordemId);
      if (ordem.status !== 'PENDENTE') return ordem;
      await esperar(400);
    }
    return null;
  }

  async function negociar(pedido: FolhaDeOrdem, assinatura: string) {
    const compra = pedido.mode === 'buy';
    f.abrir({
      kind: 'processing', ticker: pedido.ticker, mode: pedido.mode, qty: pedido.qty, total: pedido.total,
      status: 'Enviando ordem à negociação',
    });
    try {
      const ordem = await api.ordens.enviar(
        { ticker: pedido.ticker, tipo: compra ? 'COMPRA' : 'VENDA', quantidade: pedido.qty }, assinatura);
      f.atualizarFolha(atual => (atual && atual.kind === 'processing'
        ? { ...atual, ordemId: ordem.id, status: compra ? 'Debitando sua conta' : 'Reservando suas ações' }
        : atual));
      const final = await acompanharOrdem(ordem.id);
      await carregarCliente().catch(() => {});
      mostrarResultado(final, compra);
    } catch (falha) {
      const erro = comoErroDaApi(falha);
      const sessaoExpirada = erro.status === 401 && erro.codigo === 'SESSAO_OBRIGATORIA';
      if (sessaoExpirada || erro.codigo === 'CONTA_BLOQUEADA') return f.tratarErro(erro);
      f.abrir({ kind: 'stock', ticker: pedido.ticker, mode: pedido.mode }, { qty: pedido.qty, err: erro.message });
    }
  }

  function mostrarResultado(ordem: OrdemDto | null, compra: boolean) {
    if (ordem?.status === 'EXECUTADA') {
      const bolsa = EXM[f.mercado.porTicker[ordem.ticker]?.ex ?? ''];
      f.abrir({
        kind: 'success',
        title: compra ? 'Ordem de compra executada' : 'Ordem de venda executada',
        big: brl(Number(ordem.valorTotal)),
        lines: [
          ['Ativo', ordem.ticker],
          ['Quantidade', ordem.quantidade + ' ações'],
          ['Preço executado', money(Number(ordem.precoUnitario), ordem.moeda)],
          ['Bolsa', bolsa?.full || '—'],
          ['Ordem', '#' + ordem.id.slice(-8).toUpperCase()],
        ],
      });
      return;
    }
    if (ordem?.status === 'REJEITADA') {
      f.abrir({
        kind: 'confirm', d: IC.alert, title: 'Ordem rejeitada',
        body: ordem.motivoRejeicao || 'A ordem não pôde ser executada.', hasGo: false, cancel: 'Entendi',
      });
      return;
    }
    f.abrir({
      kind: 'confirm', d: IC.alert, title: 'Ordem em processamento',
      body: 'A liquidação está demorando mais que o normal. Acompanhe pelas movimentações.', hasGo: false,
      cancel: 'Entendi',
    });
  }

  return { abrirAtivo, confirmarOrdem, negociar };
}
