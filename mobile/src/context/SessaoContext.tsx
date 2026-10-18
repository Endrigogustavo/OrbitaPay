import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import type { AcessoDto, CarteiraDto, ClienteDto, ContaDto, SessaoDto } from '@/@types/api';
import type { SessaoAtiva, Usuario } from '@/@types/orbita';
import { comoErroDaApi, type ErroDaApi } from '@/integration/http';
import { paraUsuario } from '@/integration/mapeadores';
import { criarOrbitaApi, type OrbitaApi } from '@/integration/orbitaApi';
import { useToast } from './ToastContext';

export type Fase = 'splash' | 'login' | 'app';

interface SessaoContextValue {
  api: OrbitaApi;
  fase: Fase;
  sessao: SessaoAtiva | null;
  usuario: Usuario | null;
  equipe: boolean;
  admin: boolean;
  carregarCliente: () => Promise<ClienteDto>;
  entrar: (email: string, pin: string, mensagem?: string) => Promise<void>;
  assumirSessao: (resposta: SessaoDto, mensagem?: string) => Promise<void>;
  encerrarSessao: (mensagem?: string | null) => void;
  definirPausa: (pausado: boolean) => void;
}

const SessaoContext = createContext<SessaoContextValue | null>(null);

const DURACAO_DO_SPLASH = 2300;
const INTERVALO_DA_CONTA = 8000;
const ESPERA_PELA_CONTA = 1200;

const ehDaEquipe = (sessao: SessaoAtiva | null) => sessao?.perfil === 'GERENTE' || sessao?.perfil === 'ADMIN';
const ROTULO_DO_PERFIL = { CLIENTE: 'Cliente', GERENTE: 'Gerente', ADMIN: 'Admin' };
const paraSessao = (resposta: SessaoDto): SessaoAtiva => ({ perfil: resposta.perfil, clienteId: resposta.clienteId, nome: resposta.nome });

export function SessaoProvider({ children }: { children: ReactNode }) {
  const { avisar } = useToast();
  const [fase, setFase] = useState<Fase>('splash');
  const [sessao, setSessao] = useState<SessaoAtiva | null>(null);
  const [me, setMe] = useState<ClienteDto | null>(null);
  const [conta, setConta] = useState<ContaDto | null>(null);
  const [carteira, setCarteira] = useState<CarteiraDto | null>(null);
  const [acesso, setAcesso] = useState<AcessoDto | null>(null);

  const sessaoAtual = useRef<SessaoAtiva | null>(null);
  const carteiraAtual = useRef<CarteiraDto | null>(null);
  const faseAtual = useRef<Fase>('splash');
  const pausado = useRef(false);
  const esperaPelaConta = useRef<ReturnType<typeof setTimeout>>(undefined);

  const api = useMemo(() => criarOrbitaApi(), []);

  sessaoAtual.current = sessao;
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
    sessaoAtual.current = null;
    clearTimeout(esperaPelaConta.current);
    api.autenticacao.sair().catch(() => {});
    setSessao(null);
    setMe(null);
    setConta(null);
    setCarteira(null);
    setAcesso(null);
    setFase('login');
    if (mensagem) avisar(mensagem);
  }, [api, avisar]);

  const assumirSessao = useCallback(async (resposta: SessaoDto, mensagem?: string) => {
    const nova = paraSessao(resposta);
    sessaoAtual.current = nova;
    setSessao(nova);
    if (ehDaEquipe(nova)) {
      clearTimeout(esperaPelaConta.current);
      setMe(null);
      setConta(null);
      setCarteira(null);
      setAcesso(null);
      setFase('app');
      avisar(mensagem || 'Olá, ' + (nova.nome || '').split(' ')[0] + ' · acesso ' + ROTULO_DO_PERFIL[nova.perfil]);
      return;
    }
    const cliente = await carregarCliente();
    setFase('app');
    avisar(mensagem || (cliente.bloqueado ? 'Conta em modo restrito' : 'Bem-vindo(a) de volta, ' + cliente.nome.split(' ')[0]));
  }, [carregarCliente, avisar]);

  const entrar = useCallback(async (email: string, pin: string, mensagem?: string) => {
    await assumirSessao(await api.autenticacao.entrar(email, pin), mensagem);
  }, [api, assumirSessao]);

  useEffect(() => {
    let ativo = true;
    const inicio = api.autenticacao.sessaoAtual().then(
      (resposta): ErroDaApi | null => {
        if (ativo) {
          sessaoAtual.current = paraSessao(resposta);
          setSessao(sessaoAtual.current);
        }
        return null;
      },
      erro => comoErroDaApi(erro),
    );
    const abrir = setTimeout(() => {
      inicio.then(async falha => {
        if (!ativo) return;
        if (!sessaoAtual.current) {
          if (falha?.status === 0) avisar(falha.message);
          return setFase('login');
        }
        if (ehDaEquipe(sessaoAtual.current)) return setFase('app');
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
      if (sessaoAtual.current && faseAtual.current === 'app' && !pausado.current && !ehDaEquipe(sessaoAtual.current)) {
        carregarCliente().catch(() => {});
      }
    }, INTERVALO_DA_CONTA);
    return () => {
      ativo = false;
      clearTimeout(abrir);
      clearInterval(atualizar);
      clearTimeout(esperaPelaConta.current);
    };
  }, [api, carregarCliente, encerrarSessao, avisar]);

  const definirPausa = useCallback((valor: boolean) => { pausado.current = valor; }, []);

  const usuario = useMemo(() => paraUsuario(me, conta, carteira, acesso), [me, conta, carteira, acesso]);
  const equipe = ehDaEquipe(sessao);
  const admin = sessao?.perfil === 'ADMIN';

  const value = useMemo<SessaoContextValue>(() => ({
    api, fase, sessao, usuario, equipe, admin, carregarCliente, entrar, assumirSessao, encerrarSessao, definirPausa,
  }), [api, fase, sessao, usuario, equipe, admin, carregarCliente, entrar, assumirSessao, encerrarSessao, definirPausa]);

  return <SessaoContext.Provider value={value}>{children}</SessaoContext.Provider>;
}

export function useSessao(): SessaoContextValue {
  const ctx = useContext(SessaoContext);
  if (!ctx) throw new Error('useSessao precisa estar dentro de <SessaoProvider>');
  return ctx;
}
