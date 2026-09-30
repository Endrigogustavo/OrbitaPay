import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import { uid } from '@/constants/formatacao';

export interface Aviso {
  id: string;
  msg: string;
}

interface ToastContextValue {
  aviso: Aviso | null;
  avisar: (msg: string) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

const DURACAO = 2600;

export function ToastProvider({ children }: { children: ReactNode }) {
  const [aviso, setAviso] = useState<Aviso | null>(null);
  const timer = useRef<ReturnType<typeof setTimeout>>(undefined);

  const avisar = useCallback((msg: string) => {
    clearTimeout(timer.current);
    setAviso({ msg, id: uid() });
    timer.current = setTimeout(() => setAviso(null), DURACAO);
  }, []);

  useEffect(() => () => clearTimeout(timer.current), []);

  const value = useMemo(() => ({ aviso, avisar }), [aviso, avisar]);
  return <ToastContext.Provider value={value}>{children}</ToastContext.Provider>;
}

export function useToast(): ToastContextValue {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast precisa estar dentro de <ToastProvider>');
  return ctx;
}
