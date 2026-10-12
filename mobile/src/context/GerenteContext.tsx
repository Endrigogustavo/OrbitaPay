import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import type { ClienteDoBackoffice } from '@/@types/orbita';
import { comoErroDaApi } from '@/integration/http';
import { paraClienteDoBackoffice } from '@/integration/mapeadores';
import { useSessao } from './SessaoContext';
import { useToast } from './ToastContext';

interface GerenteContextValue {
  clientes: ClienteDoBackoffice[];
  carregarBackoffice: () => Promise<void>;
}

const GerenteContext = createContext<GerenteContextValue | null>(null);

export function GerenteProvider({ children }: { children: ReactNode }) {
  const { api, fase, equipe, encerrarSessao } = useSessao();
  const { avisar } = useToast();
  const [clientes, setClientes] = useState<ClienteDoBackoffice[]>([]);

  const carregarBackoffice = useCallback(async () => {
    try {
      const [todos, contas] = await Promise.all([api.clientes.listar(), api.contas.listar()]);
      const porCliente = Object.fromEntries(contas.map(c => [c.clienteId, c]));
      setClientes(todos.map(c => paraClienteDoBackoffice(c, porCliente[c.id])));
    } catch (erro) {
      const e = comoErroDaApi(erro);
      if (e.status === 401 || e.status === 403) encerrarSessao('Sessão expirada · entre de novo');
      else avisar(e.message);
    }
  }, [api, encerrarSessao, avisar]);

  useEffect(() => {
    if (equipe && fase === 'app') carregarBackoffice();
    else setClientes([]);
  }, [equipe, fase, carregarBackoffice]);

  const value = useMemo<GerenteContextValue>(() => ({ clientes, carregarBackoffice }), [clientes, carregarBackoffice]);

  return <GerenteContext.Provider value={value}>{children}</GerenteContext.Provider>;
}

export function useGerente(): GerenteContextValue {
  const ctx = useContext(GerenteContext);
  if (!ctx) throw new Error('useGerente precisa estar dentro de <GerenteProvider>');
  return ctx;
}
