import type { Acao, AcaoExibida, Posicoes, PosicaoExibida, Usuario } from '@/@types/orbita';
import { EXM } from '@/constants/bolsas';
import { brl, money, pct, pts } from '@/constants/formatacao';
import { C, corDaVariacao } from '@/constants/tema';
import { useMercado } from './MercadoContext';
import { useOperacoes } from './OperacoesContext';
import { useSessao } from './SessaoContext';

export interface Painel {
  usuario: Usuario | null;
  bloqueada: boolean;
  posicoes: Posicoes;
  /** Valor de mercado da carteira, em reais. */
  valorDaCarteira: number;
  /** Custo de aquisição da carteira, em reais. */
  custoDaCarteira: number;
  listaDePosicoes: PosicaoExibida[];
  exibir: (acao: Acao) => AcaoExibida;
}

/** Junta cliente, cotações e ações da tela em números prontos para exibir. */
export function usePainel(): Painel {
  const { usuario } = useSessao();
  const { porTicker } = useMercado();
  const { abrirAtivo, editarAtivo } = useOperacoes();
  const posicoes = usuario?.holdings || {};

  let valorDaCarteira = 0, custoDaCarteira = 0;
  const listaDePosicoes: PosicaoExibida[] = [];
  Object.keys(posicoes).forEach(ticker => {
    const st = porTicker[ticker], h = posicoes[ticker];
    if (!st || !h.qty) return;
    const val = h.qty * st.price * st.fx, cost = h.qty * h.avg * st.fx;
    valorDaCarteira += val;
    custoDaCarteira += cost;
    listaDePosicoes.push({ ticker, qty: h.qty + ' un.', val: brl(val), pl: pct(cost ? (val / cost - 1) * 100 : 0), on: () => abrirAtivo(ticker) });
  });

  const exibir = (st: Acao): AcaoExibida => {
    const e = EXM[st.ex], ch = (st.price / st.hist[0] - 1) * 100, subiu = ch >= 0;
    return {
      ticker: st.ticker, name: st.name, sector: st.sector, exCode: e?.code ?? st.ex, priceStr: money(st.price, st.cur), chgStr: pct(ch), arrow: subiu ? '▲' : '▼',
      chgColor: corDaVariacao(subiu), spark: pts(st.hist, 64, 26).map(p => p.join(',')).join(' '), sparkColor: subiu ? C.a600 : C.n500,
      flashBg: st.flash === 'up' ? C.a100 : st.flash === 'down' ? C.n200 : undefined, held: !!posicoes[st.ticker]?.qty,
      on: () => abrirAtivo(st.ticker),
      edit: () => editarAtivo(st),
    };
  };

  return { usuario, bloqueada: !!usuario?.blocked, posicoes, valorDaCarteira, custoDaCarteira, listaDePosicoes, exibir };
}
