import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import type { Acao, InfoDaBolsa } from '@/@types/orbita';
import { EX } from '@/constants/bolsas';
import { localInfo } from '@/constants/formatacao';
import { paraAcao } from '@/integration/mapeadores';
import { useSessao } from './SessaoContext';

interface MercadoContextValue {
  acoes: Acao[];
  /** Ações indexadas pelo ticker. */
  porTicker: Record<string, Acao | undefined>;
  /** Horário local e se a bolsa está aberta, por código da bolsa. */
  infos: Record<string, InfoDaBolsa>;
  bolsaSelecionada: string;
  /** Muda a cada seleção no globo, para disparar a animação de voo até a bolsa. */
  voo: number;
  atualizarMercado: () => Promise<void>;
  selecionarBolsa: (codigo: string) => void;
  mostrarBolsa: (codigo: string) => void;
}

const MercadoContext = createContext<MercadoContextValue | null>(null);

const INTERVALO_DAS_COTACOES = 3000;
const DURACAO_DO_DESTAQUE = 800;

export function MercadoProvider({ children }: { children: ReactNode }) {
  const { api } = useSessao();
  const [acoes, setAcoes] = useState<Acao[]>([]);
  const [agora, setAgora] = useState(() => Date.now());
  const [bolsaSelecionada, setBolsaSelecionada] = useState('B3');
  const [voo, setVoo] = useState(0);
  const destaque = useRef<ReturnType<typeof setTimeout>>(undefined);

  const atualizarMercado = useCallback(async () => {
    try {
      const ativos = await api.ativos.listar();
      setAgora(Date.now());
      setAcoes(atuais => {
        const anteriores = Object.fromEntries(atuais.map(x => [x.ticker, x]));
        return ativos.map(a => paraAcao(a, anteriores[a.ticker]));
      });
      clearTimeout(destaque.current);
      destaque.current = setTimeout(() => setAcoes(atuais => atuais.map(x => (x.flash ? { ...x, flash: null } : x))), DURACAO_DO_DESTAQUE);
    } catch {
      setAgora(Date.now());
    }
  }, [api]);

  useEffect(() => {
    atualizarMercado();
    const iv = setInterval(atualizarMercado, INTERVALO_DAS_COTACOES);
    return () => { clearInterval(iv); clearTimeout(destaque.current); };
  }, [atualizarMercado]);

  const selecionarBolsa = useCallback((codigo: string) => {
    setBolsaSelecionada(codigo);
    setVoo(n => n + 1);
  }, []);

  const porTicker = useMemo(() => Object.fromEntries(acoes.map(x => [x.ticker, x])), [acoes]);
  const infos = useMemo(() => {
    const now = new Date(agora);
    return Object.fromEntries(EX.map(e => [e.code, localInfo(e, now)]));
  }, [agora]);

  const value = useMemo<MercadoContextValue>(() => ({
    acoes, porTicker, infos, bolsaSelecionada, voo, atualizarMercado, selecionarBolsa, mostrarBolsa: setBolsaSelecionada,
  }), [acoes, porTicker, infos, bolsaSelecionada, voo, atualizarMercado, selecionarBolsa]);

  return <MercadoContext.Provider value={value}>{children}</MercadoContext.Provider>;
}

export function useMercado(): MercadoContextValue {
  const ctx = useContext(MercadoContext);
  if (!ctx) throw new Error('useMercado precisa estar dentro de <MercadoProvider>');
  return ctx;
}
