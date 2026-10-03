import { createContext, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import type { MetodoDeDeposito, OrdemDto, PagamentoDto } from '@/@types/api';
import type {
  Acao, CampoDoFormulario, ClienteDoBackoffice, Confirmacao, Folha, FolhaDo, Formulario, ModoDeOrdem,
} from '@/@types/orbita';
import { EXM } from '@/constants/bolsas';
import { brl, esperar, maskCpf, money, parseNum } from '@/constants/formatacao';
import { IC } from '@/constants/icones';
import { comoErroDaApi } from '@/integration/http';
import { useGerente } from './GerenteContext';
import { useMercado } from './MercadoContext';
import { useSessao } from './SessaoContext';
import { useToast } from './ToastContext';

interface Extra {
  form?: Formulario;
  amt?: string;
  qty?: number;
  err?: string;
}

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
const LIMITE_DO_SAQUE = 5000;
const TENTATIVAS_DE_ACOMPANHAMENTO = 40;
const TENTATIVAS_DE_CONFIRMACAO_DO_DEPOSITO = 30;
const ROTULO_DO_CODIGO: Record<MetodoDeDeposito, string> = { PIX: 'Pix copia e cola', Boleto: 'Linha digitável', TED: 'Dados para TED' };
const CAMPOS: CampoDoFormulario[] = ['name', 'email', 'cpf', 'pin', 'pin2', 'pinOld', 'balance', 'ticker', 'sname', 'sector', 'price'];

export function OperacoesProvider({ children }: { children: ReactNode }) {
  const { api, fase, sessao, usuario, carregarCliente, encerrarSessao, definirPausa } = useSessao();
  const mercado = useMercado();
  const gerente = useGerente();
  const { avisar } = useToast();

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

  function pressionar(tecla: string) {
    const atual = folhaAtual.current;
    if (!atual) return;
    if (atual.kind === 'amount') {
      let a = valorAtual.current;
      if (tecla === 'del') a = a.slice(0, -1);
      else if (tecla === '00') a = a ? a + '00' : a;
      else a = (a + tecla).replace(/^0+/, '');
      if (a.length > 9) return;
      setValor(a);
      setErro('');
    }
    if (atual.kind === 'pin') {
      const anterior = pinAtual.current;
      if (anterior.length >= 4 && tecla !== 'del') return;
      const p = tecla === 'del' ? anterior.slice(0, -1) : anterior + tecla;
      setPin(p);
      setErro('');
      if (p.length === 4 && !verificacao.current) {
        verificacao.current = setTimeout(() => { verificacao.current = null; verificarPin(p); }, 220);
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

  function confirmarValor() {
    const atual = folha, v = parseInt(valor || '0', 10) / 100;
    if (!usuario || !atual || atual.kind !== 'amount') return;
    if (v <= 0) return falhar('Digite um valor maior que zero');
    if (atual.op === 'dep') return depositar(v, atual.method || 'PIX');
    if (usuario.blocked) return abrir({ kind: 'locked' });
    if (v > usuario.balance) return falhar('Saldo insuficiente · disponível ' + brl(usuario.balance));
    if (v > LIMITE_DO_SAQUE) return falhar('Limite por saque: R$ 5.000,00');
    abrir({ kind: 'pin', purpose: 'withdraw', v });
  }

  function depositar(v: number, metodo: MetodoDeDeposito) {
    return ocupar(async () => {
      const cobranca = await api.pagamentos.solicitar(v, metodo);
      const codigo = cobranca.instrucoes.codigo;
      abrir({ kind: 'success', title: 'Cobrança gerada', big: brl(v), lines: [['Método', metodo], [ROTULO_DO_CODIGO[metodo], codigo.length > 28 ? codigo.slice(0, 28) + '…' : codigo], ['Situação', 'Aguardando pagamento'], ['Protocolo', '#' + cobranca.id.slice(-8).toUpperCase()]] }, { amt: '' });
      acompanharDeposito(cobranca).catch(() => {});
    });
  }

  async function acompanharDeposito(cobranca: PagamentoDto) {
    for (let tentativa = 0; tentativa < TENTATIVAS_DE_CONFIRMACAO_DO_DEPOSITO; tentativa++) {
      await esperar(1500);
      const atual = await api.pagamentos.buscar(cobranca.id).catch(() => null);
      if (!atual || atual.status === 'PENDENTE') continue;
      if (atual.status === 'CONFIRMADO') {
        await esperar(600);
        await carregarCliente().catch(() => {});
        avisar('Depósito de ' + brl(Number(atual.valorPago ?? atual.valor)) + ' confirmado');
      } else {
        avisar('A cobrança expirou sem pagamento');
      }
      return;
    }
  }

  function sacar(v: number, assinatura: string) {
    return ocupar(async () => {
      const comprovante = await api.contas.sacar(v, assinatura);
      await carregarCliente();
      abrir({ kind: 'success', title: 'Saque autorizado', big: brl(v), lines: [['Código de retirada', comprovante.protocolo], ['Válido por', '30 minutos'], ['Novo saldo', brl(Number(comprovante.novoSaldo))]] }, { amt: '' });
    });
  }

  function abrirAtivo(ticker: string) {
    abrir({ kind: 'stock', ticker, mode: 'buy' }, { qty: 1 });
  }

  function editarAtivo(st: Acao) {
    abrir({ kind: 'stockForm', edit: true }, { form: { ticker: st.ticker, sname: st.name, sector: st.sector, price: String(st.price).replace('.', ','), ex: st.ex } });
  }

  function confirmarOrdem() {
    const atual = folha;
    if (!atual || atual.kind !== 'stock' || !usuario) return;
    const st = mercado.porTicker[atual.ticker];
    if (!st) return;
    const total = +(quantidade * st.price * st.fx).toFixed(2), h = usuario.holdings[st.ticker], held = h ? h.qty - h.reserved : 0;
    if (usuario.blocked) return abrir({ kind: 'locked' });
    if (atual.mode === 'buy' && total > usuario.balance) return falhar('Saldo insuficiente para ' + quantidade + ' ' + st.ticker + ' · faltam ' + brl(total - usuario.balance));
    if (atual.mode === 'sell' && quantidade > held) return falhar(held ? 'Você tem apenas ' + held + ' ' + st.ticker : 'Você não possui ' + st.ticker);
    abrir({ kind: 'pin', purpose: 'trade', mode: atual.mode, ticker: st.ticker, qty: quantidade, price: st.price, total });
  }

  async function acompanharOrdem(ordemId: string): Promise<OrdemDto | null> {
    for (let tentativa = 0; tentativa < TENTATIVAS_DE_ACOMPANHAMENTO; tentativa++) {
      const ordem = await api.ordens.buscar(ordemId);
      if (ordem.status !== 'PENDENTE') return ordem;
      await esperar(400);
    }
    return null;
  }

  async function negociar(sh: FolhaDo<'pin'> & { purpose: 'trade' }, assinatura: string) {
    const compra = sh.mode === 'buy';
    abrir({ kind: 'processing', ticker: sh.ticker, mode: sh.mode, qty: sh.qty, total: sh.total, status: 'Enviando ordem à negociação' });
    try {
      const ordem = await api.ordens.enviar({ ticker: sh.ticker, tipo: compra ? 'COMPRA' : 'VENDA', quantidade: sh.qty }, assinatura);
      setFolha(f => (f && f.kind === 'processing' ? { ...f, ordemId: ordem.id, status: compra ? 'Debitando sua conta' : 'Reservando suas ações' } : f));
      const final = await acompanharOrdem(ordem.id);
      await carregarCliente().catch(() => {});
      if (final?.status === 'EXECUTADA') {
        const bolsa = EXM[mercado.porTicker[final.ticker]?.ex ?? ''];
        abrir({ kind: 'success', title: compra ? 'Ordem de compra executada' : 'Ordem de venda executada', big: brl(Number(final.valorTotal)),
          lines: [['Ativo', final.ticker], ['Quantidade', final.quantidade + ' ações'], ['Preço executado', money(Number(final.precoUnitario), final.moeda)], ['Bolsa', bolsa?.full || '—'], ['Ordem', '#' + final.id.slice(-8).toUpperCase()]] });
      } else if (final?.status === 'REJEITADA') {
        abrir({ kind: 'confirm', d: IC.alert, title: 'Ordem rejeitada', body: final.motivoRejeicao || 'A ordem não pôde ser executada.', hasGo: false, cancel: 'Entendi' });
      } else {
        abrir({ kind: 'confirm', d: IC.alert, title: 'Ordem em processamento', body: 'A liquidação está demorando mais que o normal. Acompanhe pelas movimentações.', hasGo: false, cancel: 'Entendi' });
      }
    } catch (falha) {
      const e = comoErroDaApi(falha);
      if ((e.status === 401 && e.codigo === 'SESSAO_OBRIGATORIA') || e.codigo === 'CONTA_BLOQUEADA') return tratarErro(e);
      abrir({ kind: 'stock', ticker: sh.ticker, mode: sh.mode }, { qty: sh.qty, err: e.message });
    }
  }

  async function verificarPin(p: string) {
    const sh = folhaAtual.current;
    if (!sh || sh.kind !== 'pin') return;
    if (sh.purpose === 'manager') return entrarComoGerente(p);
    if (sh.purpose === 'unblock') return desbloquearComPin(p);
    let assinatura: string;
    try {
      assinatura = (await api.autenticacao.assinar(p)).token;
    } catch (e) {
      return erroDePin(e);
    }
    if (sh.purpose === 'withdraw') return sacar(sh.v, assinatura);
    if (sh.purpose === 'trade') return negociar(sh, assinatura);
  }

  async function desbloquearComPin(p: string) {
    try {
      const assinatura = (await api.autenticacao.assinar(p)).token;
      await api.clientes.desbloquearMinhaConta(assinatura);
      await carregarCliente();
      abrir({ kind: 'success', title: 'Conta desbloqueada', big: 'Tudo certo', lines: [['Saques', 'Liberados'], ['Ordens na bolsa', 'Liberadas']] });
    } catch (e) {
      erroDePin(e);
    }
  }

  async function entrarComoGerente(codigo: string) {
    try {
      await gerente.entrar(codigo);
      fechar();
    } catch (falha) {
      const e = comoErroDaApi(falha);
      falhar(e.status === 0 ? e.message : 'Código de gerente inválido');
    }
  }

  function salvarCliente() {
    const atual = folha, f = form;
    if (!atual || atual.kind !== 'userForm') return;
    ocupar(async () => {
      if (atual.mode === 'self') {
        await api.clientes.atualizarMeu({ nome: f.name ?? '', email: f.email ?? '' });
        await carregarCliente();
        fechar();
        return avisar('Dados atualizados');
      }
      if (atual.mode === 'new') {
        if ((f.pin || '').length !== 4) return falhar('PIN inicial de 4 dígitos');
        const deposito = f.balance ? parseNum(f.balance) : 0;
        if (isNaN(deposito) || deposito < 0) return falhar('Saldo inicial inválido');
        const cliente = await api.clientes.cadastrarPeloGerente({ nome: f.name ?? '', email: f.email ?? '', cpf: f.cpf ?? '', pin: f.pin ?? '', depositoInicial: deposito });
        fechar();
        avisar('Cliente ' + cliente.nome.split(' ')[0] + ' cadastrado');
        await esperar(700);
        return gerente.carregarBackoffice();
      }
      if (!atual.id) return;
      await api.clientes.atualizar(atual.id, { nome: f.name ?? '', email: f.email ?? '', cpf: f.cpf ?? '' });
      fechar();
      avisar('Dados atualizados');
      await gerente.carregarBackoffice();
      if (atual.id === sessao?.clienteId) carregarCliente().catch(() => {});
    });
  }

  function alternarBloqueio(cliente: ClienteDoBackoffice) {
    ocupar(async () => {
      if (cliente.blocked) await api.clientes.desbloquear(cliente.id);
      else await api.clientes.bloquear(cliente.id);
      avisar(cliente.name.split(' ')[0] + (cliente.blocked ? ' desbloqueada(o)' : ' bloqueada(o)'));
      await gerente.carregarBackoffice();
      if (cliente.id === sessao?.clienteId) carregarCliente().catch(() => {});
    });
  }

  function removerCliente(cliente: ClienteDoBackoffice) {
    ocupar(async () => {
      await api.clientes.remover(cliente.id);
      if (cliente.id === sessao?.clienteId) return encerrarSessao('Cliente excluído');
      setFolha(null);
      avisar('Cliente excluído');
      await gerente.carregarBackoffice();
    });
  }

  function salvarPin() {
    const f = form;
    if ((f.pin || '').length !== 4) return falhar('Novo PIN com 4 dígitos');
    if (f.pin !== f.pin2) return falhar('Os PINs não conferem');
    ocupar(async () => {
      await api.autenticacao.alterarPin(f.pinOld ?? '', f.pin ?? '');
      fechar();
      avisar('PIN alterado');
    });
  }

  function bloquearMinhaConta() {
    ocupar(async () => {
      await api.clientes.bloquearMinhaConta();
      await carregarCliente();
      fechar();
      avisar('Conta bloqueada');
    });
  }

  function encerrarMinhaConta() {
    ocupar(async () => {
      await api.clientes.encerrarMinhaConta();
      encerrarSessao('Conta encerrada');
    });
  }

  function salvarAtivo() {
    const atual = folha, f = form, t = (f.ticker || '').trim(), p = parseNum(f.price);
    if (!atual || atual.kind !== 'stockForm') return;
    if (t.length < 2) return falhar('Ticker com pelo menos 2 caracteres');
    if (!(f.sname || '').trim()) return falhar('Informe o nome da empresa');
    if (!(p > 0)) return falhar('Preço precisa ser maior que zero');
    const dados = { ticker: t, nome: (f.sname || '').trim(), setor: (f.sector || '').trim(), bolsa: f.ex || 'B3', cotacao: p };
    ocupar(async () => {
      if (atual.edit) await api.ativos.atualizar(t, dados);
      else await api.ativos.listarNaBolsa(dados);
      await mercado.atualizarMercado();
      if (!atual.edit) mercado.mostrarBolsa(dados.bolsa);
      fechar();
      avisar(atual.edit ? t + ' atualizada' : t + ' listada na ' + dados.bolsa);
    });
  }

  function removerAtivo(ticker: string) {
    ocupar(async () => {
      await api.ativos.remover(ticker);
      await mercado.atualizarMercado();
      fechar();
      avisar(ticker + ' removida');
    });
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
    confirmarValor, confirmarOrdem, abrirAtivo, editarAtivo, salvarCliente, alternarBloqueio, removerCliente,
    salvarPin, bloquearMinhaConta, encerrarMinhaConta, salvarAtivo, removerAtivo,
  };

  return <OperacoesContext.Provider value={value}>{children}</OperacoesContext.Provider>;
}

export function useOperacoes(): OperacoesContextValue {
  const ctx = useContext(OperacoesContext);
  if (!ctx) throw new Error('useOperacoes precisa estar dentro de <OperacoesProvider>');
  return ctx;
}
