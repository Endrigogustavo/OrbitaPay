import { createContext, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import type { MetodoDeDeposito } from '@/@types/api';
import type {
  Acao, CampoDoFormulario, ClienteDoBackoffice, Confirmacao, Folha, Formulario, ModoDeOrdem,
} from '@/@types/orbita';
import { maskCpf } from '@/constants/formatacao';
import { comoErroDaApi } from '@/integration/http';
import { useGerente } from './GerenteContext';
import { useMercado } from './MercadoContext';
import { useSessao } from './SessaoContext';
import { useToast } from './ToastContext';
import { criarOperacoesDoBanco } from './operacoes/banco';
import type { Extra, Ferramentas } from './operacoes/ferramentas';
import { criarOperacoesDeGerencia } from './operacoes/gerencia';
import { criarOperacoesDeNegociacao } from './operacoes/negociacao';
import { criarOperacoesDoPerfil } from './operacoes/perfil';

interface OperacoesContextValue {
  folha: Folha | null;
  fechando: boolean;
  form: Formulario;
  erro: string;
  tremor: number;
  pin: string;
  valor: string;
  quantidade: number;
  ocupado: boolean;
  ocultarValores: boolean;

  abrir: (folha: Folha, extra?: Extra) => void;
  fechar: () => void;
  confirmar: (confirmacao: Omit<Confirmacao, 'kind'>) => void;
  alternarOcultarValores: () => void;
  campo: Record<CampoDoFormulario, (v: string) => void>;
  definirBolsaDoFormulario: (codigo: string) => void;

  pressionar: (tecla: string) => void;
  somarAoValor: (reais: number) => void;
  definirMetodo: (metodo: MetodoDeDeposito) => void;
  definirModo: (modo: ModoDeOrdem) => void;
  alterarQuantidade: (delta: number) => void;

  confirmarValor: () => void;
  confirmarOrdem: () => void;
  abrirAtivo: (ticker: string) => void;
  editarAtivo: (acao: Acao) => void;
  salvarCliente: () => void;
  alternarBloqueio: (cliente: ClienteDoBackoffice) => void;
  removerCliente: (cliente: ClienteDoBackoffice) => void;
  salvarPin: () => void;
  bloquearMinhaConta: () => void;
  encerrarMinhaConta: () => void;
  salvarAtivo: () => void;
  removerAtivo: (ticker: string) => void;
}

const OperacoesContext = createContext<OperacoesContextValue | null>(null);

const DURACAO_DO_FECHAMENTO = 260;
const CAMPOS: CampoDoFormulario[] = ['name', 'email', 'cpf', 'pin', 'pin2', 'pinOld', 'balance', 'ticker', 'sname', 'sector', 'price'];

export function OperacoesProvider({ children }: { children: ReactNode }) {
  const sessao = useSessao();
  const mercado = useMercado();
  const gerente = useGerente();
  const { avisar } = useToast();
  const { fase, carregarCliente, encerrarSessao, definirPausa } = sessao;

  const [folha, setFolha] = useState<Folha | null>(null);
  const [fechando, setFechando] = useState(false);
  const [form, setForm] = useState<Formulario>({});
  const [erro, setErro] = useState('');
  const [tremor, setTremor] = useState(0);
  const [pin, setPinState] = useState('');
  const [valor, setValorState] = useState('');
  const [quantidade, setQuantidade] = useState(1);
  const [ocupado, setOcupado] = useState(false);
  const [ocultarValores, setOcultarValores] = useState(false);

  const folhaAtual = useRef<Folha | null>(null);
  const pinAtual = useRef('');
  const valorAtual = useRef('');
  const emAndamento = useRef(false);
  const fechamento = useRef<ReturnType<typeof setTimeout>>(undefined);
  const verificacao = useRef<ReturnType<typeof setTimeout> | null>(null);
  folhaAtual.current = folha;

  const setPin = (p: string) => { pinAtual.current = p; setPinState(p); };
  const setValor = (v: string) => { valorAtual.current = v; setValorState(v); };

  useEffect(() => { definirPausa(!!folha); }, [folha, definirPausa]);
  useEffect(() => { if (fase !== 'app') setFolha(null); }, [fase]);
  useEffect(() => () => {
    clearTimeout(fechamento.current);
    if (verificacao.current) clearTimeout(verificacao.current);
  }, []);

  function abrir(nova: Folha, extra: Extra = {}) {
    clearTimeout(fechamento.current);
    setFolha(nova);
    setFechando(false);
    setErro(extra.err ?? '');
    setPin('');
    if (extra.form) setForm(extra.form);
    if (extra.amt !== undefined) setValor(extra.amt);
    if (extra.qty !== undefined) setQuantidade(extra.qty);
  }

  function fechar() {
    if (!folhaAtual.current) return;
    setFechando(true);
    clearTimeout(fechamento.current);
    fechamento.current = setTimeout(() => {
      setFolha(null);
      setFechando(false);
      setErro('');
      setPin('');
      setValor('');
    }, DURACAO_DO_FECHAMENTO);
  }

  function falhar(mensagem: string) {
    setErro(mensagem);
    setTremor(n => n + 1);
    setPin('');
  }

  async function ocupar(tarefa: () => Promise<unknown>) {
    if (emAndamento.current) return;
    emAndamento.current = true;
    setOcupado(true);
    try {
      await tarefa();
    } catch (e) {
      tratarErro(e);
    } finally {
      emAndamento.current = false;
      setOcupado(false);
    }
  }

  function tratarErro(falha: unknown) {
    const e = comoErroDaApi(falha);
    if (e.status === 401 && e.codigo === 'SESSAO_OBRIGATORIA' && fase === 'app') {
      encerrarSessao('Sessão expirada · entre novamente');
      return;
    }
    if (e.codigo === 'CLIENTE_BLOQUEADO' || e.codigo === 'CONTA_BLOQUEADA') {
      carregarCliente().catch(() => {});
      abrir({ kind: 'locked' });
      return;
    }
    falhar(e.message);
  }

  function erroDePin(falha: unknown) {
    const e = comoErroDaApi(falha);
    if (e.codigo === 'PIN_INCORRETO') {
      carregarCliente().catch(() => {});
      if (e.detalhes.bloqueado) {
        setTremor(n => n + 1);
        setPin('');
        setTimeout(() => abrir({ kind: 'locked' }), 350);
        return;
      }
    }
    tratarErro(e);
  }

  const ferramentas: Ferramentas = {
    sessao, mercado, gerente, folha, form, valor, quantidade,
    abrir, fechar, falhar, ocupar, avisar, atualizarFolha: setFolha, tratarErro, erroDePin,
  };
  const banco = criarOperacoesDoBanco(ferramentas);
  const negociacao = criarOperacoesDeNegociacao(ferramentas);
  const gerencia = criarOperacoesDeGerencia(ferramentas);
  const perfil = criarOperacoesDoPerfil(ferramentas);

  async function verificarPin(p: string) {
    const atual = folhaAtual.current;
    if (!atual || atual.kind !== 'pin') return;
    if (atual.purpose === 'manager') return gerencia.entrarComoGerente(p);
    if (atual.purpose === 'unblock') return perfil.desbloquearComPin(p);
    let assinatura: string;
    try {
      assinatura = (await sessao.api.autenticacao.assinar(p)).token;
    } catch (e) {
      return erroDePin(e);
    }
    if (atual.purpose === 'withdraw') return banco.sacar(atual.v, assinatura);
    if (atual.purpose === 'trade') return negociacao.negociar(atual, assinatura);
  }

  function pressionar(tecla: string) {
    const atual = folhaAtual.current;
    if (!atual) return;
    if (atual.kind === 'amount') {
      let digitado = valorAtual.current;
      if (tecla === 'del') digitado = digitado.slice(0, -1);
      else if (tecla === '00') digitado = digitado ? digitado + '00' : digitado;
      else digitado = (digitado + tecla).replace(/^0+/, '');
      if (digitado.length > 9) return;
      setValor(digitado);
      setErro('');
    }
    if (atual.kind === 'pin') {
      const anterior = pinAtual.current;
      if (anterior.length >= 4 && tecla !== 'del') return;
      const novo = tecla === 'del' ? anterior.slice(0, -1) : anterior + tecla;
      setPin(novo);
      setErro('');
      if (novo.length === 4 && !verificacao.current) {
        verificacao.current = setTimeout(() => { verificacao.current = null; verificarPin(novo); }, 220);
      }
    }
  }

  function somarAoValor(reais: number) {
    const atual = parseInt(valorAtual.current || '0', 10) / 100;
    setValor(String(Math.round((atual + reais) * 100)));
    setErro('');
  }

  function definirMetodo(metodo: MetodoDeDeposito) {
    setFolha(f => (f && f.kind === 'amount' ? { ...f, method: metodo } : f));
  }

  function definirModo(modo: ModoDeOrdem) {
    setFolha(f => (f && f.kind === 'stock' ? { ...f, mode: modo } : f));
    setErro('');
  }

  function alterarQuantidade(delta: number) {
    setQuantidade(q => Math.max(1, Math.min(9999, q + delta)));
    setErro('');
  }

  const campo = useMemo(() => Object.fromEntries(CAMPOS.map(k => [k, (entrada: string) => {
    let v = entrada;
    if (k.startsWith('pin')) v = v.replace(/\D/g, '').slice(0, 4);
    if (k === 'cpf') v = maskCpf(v);
    if (k === 'ticker') v = v.toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, 8);
    setForm(f => ({ ...f, [k]: v }));
    setErro('');
  }])) as Record<CampoDoFormulario, (v: string) => void>, []);

  const value: OperacoesContextValue = {
    folha, fechando, form, erro, tremor, pin, valor, quantidade, ocupado, ocultarValores,
    abrir, fechar,
    confirmar: confirmacao => abrir({ kind: 'confirm', ...confirmacao }),
    alternarOcultarValores: () => setOcultarValores(v => !v),
    campo,
    definirBolsaDoFormulario: codigo => setForm(f => ({ ...f, ex: codigo })),
    pressionar, somarAoValor, definirMetodo, definirModo, alterarQuantidade,
    confirmarValor: banco.confirmarValor,
    confirmarOrdem: negociacao.confirmarOrdem,
    abrirAtivo: negociacao.abrirAtivo,
    editarAtivo: gerencia.editarAtivo,
    salvarCliente: gerencia.salvarCliente,
    alternarBloqueio: gerencia.alternarBloqueio,
    removerCliente: gerencia.removerCliente,
    salvarAtivo: gerencia.salvarAtivo,
    removerAtivo: gerencia.removerAtivo,
    salvarPin: perfil.salvarPin,
    bloquearMinhaConta: perfil.bloquearMinhaConta,
    encerrarMinhaConta: perfil.encerrarMinhaConta,
  };

  return <OperacoesContext.Provider value={value}>{children}</OperacoesContext.Provider>;
}

export function useOperacoes(): OperacoesContextValue {
  const ctx = useContext(OperacoesContext);
  if (!ctx) throw new Error('useOperacoes precisa estar dentro de <OperacoesProvider>');
  return ctx;
}
