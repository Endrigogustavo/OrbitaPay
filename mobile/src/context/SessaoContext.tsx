import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import type { AcessoDto, CarteiraDto, ClienteDto, ContaDto } from '@/@types/api';
import type { SessaoSalva, Usuario } from '@/@types/orbita';
import { comoErroDaApi } from '@/integration/http';
import { paraUsuario } from '@/integration/mapeadores';
import { criarOrbitaApi, type OrbitaApi } from '@/integration/orbitaApi';
import { lerSessao, salvarSessao } from '@/integration/sessaoArmazenada';
import { useToast } from './ToastContext';

export type Fase = 'splash' | 'login' | 'app';

interface SessaoContextValue {
  api: OrbitaApi;
  fase: Fase;
  sessao: SessaoSalva | null;
  usuario: Usuario | null;
  carregarCliente: () => Promise<ClienteDto>;
  entrar: (email: string, pin: string, mensagem?: string) => Promise<void>;
  encerrarSessao: (mensagem?: string | null) => void;
  definirTokenDoGerente: (token: string | null) => void;
  definirPausa: (pausado: boolean) => void;
}

const SessaoContext = createContext<SessaoContextValue | null>(null);

const DURACAO_DO_SPLASH = 2300;
const INTERVALO_DA_CONTA = 8000;
const ESPERA_PELA_CONTA = 1200;

export function SessaoProvider({ children }: { children: ReactNode }) {
  const { avisar } = useToast();
  const [fase, setFase] = useState<Fase>('splash');
  const [sessao, setSessao] = useState<SessaoSalva | null>(null);
  const [me, setMe] = useState<ClienteDto | null>(null);
  const [conta, setConta] = useState<ContaDto | null>(null);
  const [carteira, setCarteira] = useState<CarteiraDto | null>(null);
  const [acesso, setAcesso] = useState<AcessoDto | null>(null);

  const token = useRef<string | null>(null);
  const tokenDoGerente = useRef<string | null>(null);
  const carteiraAtual = useRef<CarteiraDto | null>(null);
  const faseAtual = useRef<Fase>('splash');
  const pausado = useRef(false);
  const esperaPelaConta = useRef<ReturnType<typeof setTimeout>>(undefined);

  const api = useMemo(() => criarOrbitaApi({ cliente: () => token.current, gerente: () => tokenDoGerente.current }), []);

  carteiraAtual.current = carteira;
  faseAtual.current = fase;

  const carregarCliente = useCallback(async (): Promise<ClienteDto> => {
    const [cliente, contaDoCliente, carteiraDoCliente, acessoDoCliente] = await Promise.all([
      api.clientes.meu(),
      api.contas.minha().catch(e => { if (comoErroDaApi(e).status === 404) return null; throw e; }),
      api.carteira.minha().catch(() => carteiraAtual.current),
      api.autenticacao.meuAcesso().catch(() => null),
    ]);
    setMe(cliente);
    setConta(contaDoCliente);
    setCarteira(carteiraDoCliente);
    setAcesso(acessoDoCliente);
    if (!contaDoCliente) {
      clearTimeout(esperaPelaConta.current);
      esperaPelaConta.current = setTimeout(() => { carregarCliente().catch(() => {}); }, ESPERA_PELA_CONTA);
    }
    return cliente;
  }, [api]);

  const encerrarSessao = useCallback((mensagem?: string | null) => {
    token.current = null;
    tokenDoGerente.current = null;
    clearTimeout(esperaPelaConta.current);
    salvarSessao(null);
    setSessao(null);
    setMe(null);
    setConta(null);
    setCarteira(null);
    setAcesso(null);
    setFase('login');
    if (mensagem) avisar(mensagem);
  }, [avisar]);

  const entrar = useCallback(async (email: string, pin: string, mensagem?: string) => {
    const resposta = await api.autenticacao.cliente(email, pin);
    const nova: SessaoSalva = { token: resposta.token, clienteId: resposta.clienteId };
    token.current = resposta.token;
    tokenDoGerente.current = null;
    await salvarSessao(nova);
    setSessao(nova);
    const cliente = await carregarCliente();
    setFase('app');
    avisar(mensagem || (cliente.bloqueado ? 'Conta em modo restrito' : 'Bem-vindo(a) de volta, ' + cliente.nome.split(' ')[0]));
  }, [api, carregarCliente, avisar]);

  useEffect(() => {
    let ativo = true;
    const inicio = lerSessao().then(salva => {
      if (ativo && salva?.token) {
        token.current = salva.token;
        setSessao(salva);
      }
    });
    const abrir = setTimeout(() => {
      inicio.then(async () => {
        if (!ativo) return;
        if (!token.current) return setFase('login');
        try {
          await carregarCliente();
          setFase('app');
        } catch (erro) {
          const e = comoErroDaApi(erro);
          encerrarSessao(e.status === 0 ? e.message : null);
        }
      });
    }, DURACAO_DO_SPLASH);
    const atualizar = setInterval(() => {
      if (token.current && faseAtual.current === 'app' && !pausado.current) carregarCliente().catch(() => {});
    }, INTERVALO_DA_CONTA);
    return () => {
      ativo = false;
      clearTimeout(abrir);
      clearInterval(atualizar);
      clearTimeout(esperaPelaConta.current);
    };
  }, [carregarCliente, encerrarSessao]);

  const definirTokenDoGerente = useCallback((valor: string | null) => { tokenDoGerente.current = valor; }, []);
  const definirPausa = useCallback((valor: boolean) => { pausado.current = valor; }, []);

  const usuario = useMemo(() => paraUsuario(me, conta, carteira, acesso), [me, conta, carteira, acesso]);

  const value = useMemo<SessaoContextValue>(() => ({
    api, fase, sessao, usuario, carregarCliente, entrar, encerrarSessao, definirTokenDoGerente, definirPausa,
  }), [api, fase, sessao, usuario, carregarCliente, entrar, encerrarSessao, definirTokenDoGerente, definirPausa]);

  return <SessaoContext.Provider value={value}>{children}</SessaoContext.Provider>;
}

export function useSessao(): SessaoContextValue {
  const ctx = useContext(SessaoContext);
  if (!ctx) throw new Error('useSessao precisa estar dentro de <SessaoProvider>');
  return ctx;
}
