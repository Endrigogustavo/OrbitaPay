import type { Folha, Formulario } from '@/@types/orbita';
import type { useGerente } from '../GerenteContext';
import type { useMercado } from '../MercadoContext';
import type { useSessao } from '../SessaoContext';

export interface Extra {
  form?: Formulario;
  amt?: string;
  qty?: number;
  err?: string;
}

export interface Ferramentas {
  sessao: ReturnType<typeof useSessao>;
  mercado: ReturnType<typeof useMercado>;
  gerente: ReturnType<typeof useGerente>;
  folha: Folha | null;
  form: Formulario;
  valor: string;
  quantidade: number;
  abrir: (folha: Folha, extra?: Extra) => void;
  fechar: () => void;
  falhar: (mensagem: string) => void;
  ocupar: (tarefa: () => Promise<unknown>) => Promise<void>;
  avisar: (mensagem: string) => void;
  atualizarFolha: (alterar: (folha: Folha | null) => Folha | null) => void;
  tratarErro: (falha: unknown) => void;
  erroDePin: (falha: unknown) => void;
}
