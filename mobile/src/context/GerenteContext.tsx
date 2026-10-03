import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import type { ClienteDoBackoffice } from '@/@types/orbita';
import { comoErroDaApi } from '@/integration/http';
import { paraClienteDoBackoffice } from '@/integration/mapeadores';
import { useSessao } from './SessaoContext';
import { useToast } from './ToastContext';

interface GerenteContextValue {
  ativo: boolean;
  clientes: ClienteDoBackoffice[];
  carregarBackoffice: () => Promise<void>;
  entrar: (codigo: string) => Promise<void>;
  sair: (mensagem?: string) => void;
}

const GerenteContext = createContext<GerenteContextValue | null>(null);

export function GerenteProvider({ children }: { children: ReactNode }) {
  const { api, sessao, definirTokenDoGerente } = useSessao();
  const { avisar } = useToast();
  const [ativo, setAtivo] = useState(false);
  const [clientes, setClientes] = useState<ClienteDoBackoffice[]>([]);

  const sair = useCallback((mensagem?: string) => {
    definirTokenDoGerente(null);
    setAtivo(false);
    setClientes([]);
    if (mensagem) avisar(mensagem);
  }, [definirTokenDoGerente, avisar]);

  const carregarBackoffice = useCallback(async () => {
    try {
      const [todos, contas] = await Promise.all([api.clientes.listar(), api.contas.listar()]);
      const porCliente = Object.fromEntries(contas.map(c => [c.clienteId, c]));
      setClientes(todos.map(c => paraClienteDoBackoffice(c, porCliente[c.id])));
    } catch (erro) {
      const e = comoErroDaApi(erro);
      if (e.status === 401 || e.status === 403) sair('Sessão de gerente expirada');
      else avisar(e.message);
    }
  }, [api, sair, avisar]);

  const entrar = useCallback(async (codigo: string) => {
    const credencial = await api.autenticacao.gerente(codigo);
    definirTokenDoGerente(credencial.token);
    setAtivo(true);
    avisar('Modo gerente ativado');
    carregarBackoffice();
  }, [api, definirTokenDoGerente, avisar, carregarBackoffice]);

  useEffect(() => { sair(); }, [sessao?.token, sair]);

  const value = useMemo<GerenteContextValue>(() => ({ ativo, clientes, carregarBackoffice, entrar, sair }),
    [ativo, clientes, carregarBackoffice, entrar, sair]);

  return <GerenteContext.Provider value={value}>{children}</GerenteContext.Provider>;
}

export function useGerente(): GerenteContextValue {
  const ctx = useContext(GerenteContext);
  if (!ctx) throw new Error('useGerente precisa estar dentro de <GerenteProvider>');
  return ctx;
}
