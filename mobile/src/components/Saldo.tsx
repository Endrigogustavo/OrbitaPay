import { useEffect, useState } from 'react';
import { brl } from '@/constants/formatacao';
import { T } from './ui';

// Último valor exibido, para a animação continuar de onde parou ao voltar para a tela.
const ULTIMO: { id: string | null; v: number } = { id: null, v: 0 };

interface SaldoProps {
  value: number;
  /** Id do cliente: trocar de cliente reinicia a contagem a partir de zero. */
  id: string;
  hide: boolean;
}

/** Saldo com contagem animada até o valor atual. */
export function Saldo({ value, id, hide }: SaldoProps) {
  const [disp, setDisp] = useState(ULTIMO.id === id ? ULTIMO.v : 0);
  useEffect(() => {
    const first = ULTIMO.id !== id, from = first ? 0 : ULTIMO.v;
    ULTIMO.id = id;
    if (!first && from === value) return;
    let raf = 0;
    const tm = setTimeout(() => {
      const t0 = Date.now();
      const step = () => {
        const p = Math.min(1, (Date.now() - t0) / 1100), e = 1 - Math.pow(1 - p, 4);
        ULTIMO.v = from + (value - from) * e;
        setDisp(ULTIMO.v);
        if (p < 1) raf = requestAnimationFrame(step);
      };
      raf = requestAnimationFrame(step);
    }, first ? 250 : 0);
    return () => { clearTimeout(tm); cancelAnimationFrame(raf); };
  }, [value, id]);
  return <T h num style={{ fontSize: 50, lineHeight: 54, marginTop: 14, marginBottom: 16 }}>{hide ? 'R$ ••••••' : brl(disp)}</T>;
}
