import React, { useEffect, useRef, useState } from 'react';
import { View, ScrollView, Pressable, BackHandler, Animated, Easing, Dimensions, KeyboardAvoidingView, StyleSheet } from 'react-native';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaProvider, useSafeAreaInsets } from 'react-native-safe-area-context';
import { GestureHandlerRootView, ScrollView as GestureScrollView } from 'react-native-gesture-handler';
import { useFonts } from 'expo-font';
import { Barlow_400Regular } from '@expo-google-fonts/barlow/400Regular';
import { Barlow_500Medium } from '@expo-google-fonts/barlow/500Medium';
import { BarlowCondensed_600SemiBold } from '@expo-google-fonts/barlow-condensed/600SemiBold';
import Svg, { Polyline, Polygon, Line, Rect, Circle } from 'react-native-svg';

import { C } from './src/theme';
import {
  MAX_ATTEMPTS, EX, EXM, IC, TABS, DEMO, brl, money, pct, uid, ini, maskCpf, parseNum, fmtT, pts, localInfo, fh2,
} from './src/data';
import {
  T, Kicker, Icon, Blueprint, Anim, Spin, Logo, PulseDot, Scan, Shake, Btn, Cta, Plain, Tag, Field, Input, Seg,
  ErrBox, InfoGrid, Avatar, Row,
} from './src/ui';
import Globe from './src/Globe';
import { criarOrbitaApi } from './src/api/orbitaApi';
import { URL_DO_GATEWAY } from './src/api/configuracao';
import { lerSessao, salvarSessao } from './src/api/sessaoArmazenada';
import { paraAcao, paraUsuario, paraClienteDoBackoffice } from './src/api/mapeadores';

const OUT = Easing.bezier(0.2, 0.9, 0.25, 1);
const ABS = StyleSheet.absoluteFill;
const REASON = { pin: 'Bloqueada após tentativas de PIN incorretas.', admin: 'Bloqueada pelo gerente do banco.', user: 'Você bloqueou a conta por segurança.' };
const up = v => (v ? C.a700 : C.n700);
const esperar = ms => new Promise(resolve => setTimeout(resolve, ms));

function initState() {
  return {
    sessao: null, me: null, conta: null, carteira: null, stocks: [], gerente: null, clientesGerente: [],
    screen: 'splash', tab: 'home', sheet: null, closing: false, form: {}, err: '', shake: 0, amt: '', pin: '', qty: 1,
    toast: null, hide: false, mgr: false, bv: 'clients', q: '', mf: 'Todas', selEx: 'B3', exN: 0, now: Date.now(), ocupado: false,
  };
}

function Splash() {
  const grow = useRef(new Animated.Value(0)).current;
  useEffect(() => { Animated.timing(grow, { toValue: 1, duration: 1900, easing: Easing.bezier(0.6, 0, 0.2, 1), useNativeDriver: true }).start(); }, []);
  return (
    <Anim type="fadeIn" duration={300} style={[ABS, { zIndex: 40, backgroundColor: C.a900, justifyContent: 'space-between', paddingTop: 120, paddingHorizontal: 34, paddingBottom: 64 }]}>
      <View style={{ width: 170, height: 170 }}>
        <Spin duration={18000} style={ABS}>
          <Svg width={170} height={170}><Circle cx={85} cy={85} r={84.5} fill="none" stroke={C.a300} strokeWidth={1} strokeDasharray="3 3" /></Svg>
        </Spin>
        <Anim type="frame" duration={800} style={{ position: 'absolute', top: 28, left: 28, right: 28, bottom: 28, borderWidth: 1, borderColor: C.a300, borderRadius: 200 }} />
        <Anim type="pop" delay={200} duration={700} style={{ position: 'absolute', top: 62, left: 62, right: 62, bottom: 62, backgroundColor: C.bg, borderRadius: 200 }} />
        <Spin duration={2200} easing={Easing.bezier(0.5, 0, 0.5, 1)} style={{ position: 'absolute', top: -2, left: -2, right: -2, bottom: -2 }}>
          <View style={{ position: 'absolute', top: 0, left: '50%', width: 10, height: 10, marginLeft: -5, backgroundColor: C.bg }} />
        </Spin>
        <Spin duration={3400} reverse style={{ position: 'absolute', top: 26, left: 26, right: 26, bottom: 26 }}>
          <View style={{ position: 'absolute', bottom: 0, left: '50%', width: 6, height: 6, marginLeft: -3, backgroundColor: C.a300 }} />
        </Spin>
      </View>
      <View style={{ gap: 14 }}>
        <View style={{ flexDirection: 'row', overflow: 'hidden' }}>
          {'ÓRBITA'.split('').map((c, i) => (
            <Anim key={i} type="letter" delay={350 + i * 70} duration={600}>
              <T h style={{ fontSize: 76, lineHeight: 86, letterSpacing: 3, color: C.bg }}>{c}</T>
            </Anim>
          ))}
        </View>
        <Anim type="fadeUp" delay={800} duration={600}><T style={{ fontSize: 16, color: C.a200 }}>Banco, bolsa e o mundo em órbita.</T></Anim>
        <View style={{ height: 1, backgroundColor: 'rgba(242,242,243,0.25)', marginTop: 18 }}>
          <Animated.View style={{ height: 1, backgroundColor: C.bg, transformOrigin: 'left', transform: [{ scaleX: grow }] }} />
        </View>
        <View style={{ flexDirection: 'row', justifyContent: 'space-between' }}>
          <Kicker color={C.a300} style={{ letterSpacing: 1.5 }}>Conectando ao gateway</Kicker>
          <Kicker color={C.a300}>v2.0</Kicker>
        </View>
      </View>
    </Anim>
  );
}

const BAL = { id: null, v: 0 };
function Balance({ value, id, hide }) {
  const [disp, setDisp] = useState(BAL.id === id ? BAL.v : 0);
  useEffect(() => {
    const first = BAL.id !== id, from = first ? 0 : BAL.v;
    BAL.id = id;
    if (!first && from === value) return;
    let raf;
    const tm = setTimeout(() => {
      const t0 = Date.now();
      const step = () => {
        const p = Math.min(1, (Date.now() - t0) / 1100), e = 1 - Math.pow(1 - p, 4);
        BAL.v = from + (value - from) * e; setDisp(BAL.v);
        if (p < 1) raf = requestAnimationFrame(step);
      };
      raf = requestAnimationFrame(step);
    }, first ? 250 : 0);
    return () => { clearTimeout(tm); cancelAnimationFrame(raf); };
  }, [value, id]);
  return <T h num style={{ fontSize: 50, lineHeight: 54, marginTop: 14, marginBottom: 16 }}>{hide ? 'R$ ••••••' : brl(disp)}</T>;
}

function Tape({ items }) {
  const v = useRef(new Animated.Value(0)).current;
  const [w, setW] = useState(0);
  useEffect(() => {
    if (!w) return;
    const l = Animated.loop(Animated.timing(v, { toValue: 1, duration: 46000, easing: Easing.linear, useNativeDriver: true }));
    l.start();
    return () => l.stop();
  }, [w]);
  return (
    <View style={{ marginHorizontal: -20, borderTopWidth: 1, borderBottomWidth: 1, borderColor: C.divider, backgroundColor: C.surface }}>
      <ScrollView horizontal scrollEnabled={false} showsHorizontalScrollIndicator={false}>
        <Animated.View onLayout={e => { if (!w) setW(e.nativeEvent.layout.width); }}
          style={{ flexDirection: 'row', transform: [{ translateX: v.interpolate({ inputRange: [0, 1], outputRange: [0, -w / 2 || 0] }) }] }}>
          {items.map((t, i) => (
            <Pressable key={i} onPress={t.on} style={{ flexDirection: 'row', alignItems: 'baseline', gap: 6, paddingVertical: 9, paddingHorizontal: 16, borderRightWidth: 1, borderRightColor: C.divider }}>
              <T h style={{ fontSize: 14 }}>{t.ticker}</T>
              <T num style={{ fontSize: 13 }}>{t.priceStr}</T>
              <T num style={{ fontSize: 12, color: t.chgColor }}>{t.arrow} {t.chgStr}</T>
            </Pressable>
          ))}
        </Animated.View>
      </ScrollView>
    </View>
  );
}

function TabBar({ tab, onGo, bottom }) {
  const x = useRef(new Animated.Value(0)).current;
  const [w, setW] = useState(0);
  const idx = TABS.findIndex(t => t.k === tab);
  useEffect(() => { Animated.timing(x, { toValue: idx, duration: 400, easing: OUT, useNativeDriver: true }).start(); }, [idx]);
  return (
    <View onLayout={e => setW(e.nativeEvent.layout.width)} style={{ position: 'absolute', left: 0, right: 0, bottom: 0, height: 64 + bottom, paddingBottom: bottom, backgroundColor: C.bg, borderTopWidth: 1, borderTopColor: C.divider, flexDirection: 'row', zIndex: 10 }}>
      <Animated.View style={{ position: 'absolute', top: -1, left: 0, width: w / 5, height: 2, backgroundColor: C.accent, transform: [{ translateX: x.interpolate({ inputRange: [0, 4], outputRange: [0, (w * 4) / 5] }) }] }} />
      {TABS.map(t => {
        const on = t.k === tab, col = on ? C.a700 : C.n600;
        return (
          <Pressable key={t.k} onPress={() => onGo(t.k)} style={{ flex: 1, alignItems: 'center', justifyContent: 'center', gap: 4 }}>
            <Anim key={on ? 'on' : 'off'} type={on ? 'pop' : 'none'} duration={450}><Icon d={IC[t.i]} size={24} color={col} /></Anim>
            <T m style={{ fontSize: 11, letterSpacing: 0.2, color: col }}>{t.l}</T>
          </Pressable>
        );
      })}
    </View>
  );
}

function Sheet({ closing, onClose, bottom, children }) {
  const H = Dimensions.get('window').height;
  const y = useRef(new Animated.Value(H)).current, o = useRef(new Animated.Value(0)).current;
  useEffect(() => {
    Animated.parallel(closing
      ? [Animated.timing(y, { toValue: H, duration: 260, easing: Easing.in(Easing.quad), useNativeDriver: true }), Animated.timing(o, { toValue: 0, duration: 260, useNativeDriver: true })]
      : [Animated.timing(y, { toValue: 0, duration: 450, easing: OUT, useNativeDriver: true }), Animated.timing(o, { toValue: 1, duration: 300, useNativeDriver: true })]).start();
  }, [closing]);
  return (
    <View style={[ABS, { zIndex: 30 }]}>
      <Animated.View style={[ABS, { backgroundColor: C.backdrop, opacity: o }]}><Pressable style={{ flex: 1 }} onPress={onClose} /></Animated.View>
      <KeyboardAvoidingView behavior="padding" style={{ flex: 1, justifyContent: 'flex-end' }} pointerEvents="box-none">
        <Animated.View style={{ maxHeight: '90%', backgroundColor: C.bg, borderTopWidth: 1, borderTopColor: C.divider, transform: [{ translateY: y }], elevation: 16 }}>
          <ScrollView keyboardShouldPersistTaps="handled" contentContainerStyle={{ paddingTop: 10, paddingHorizontal: 22, paddingBottom: 36 + bottom }}>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
              <View style={{ width: 40, height: 3, backgroundColor: C.n400 }} />
              <Btn kind="ghost" onPress={onClose} style={{ width: 36, height: 36, paddingHorizontal: 0, paddingVertical: 0, borderRadius: 18 }}><Icon d={IC.x} size={20} /></Btn>
            </View>
            {children}
          </ScrollView>
        </Animated.View>
      </KeyboardAvoidingView>
    </View>
  );
}

function Blink() {
  const [on, setOn] = useState(true);
  useEffect(() => { const iv = setInterval(() => setOn(v => !v), 500); return () => clearInterval(iv); }, []);
  return <View style={{ width: 2, height: 44, backgroundColor: C.accent, alignSelf: 'center', opacity: on ? 1 : 0 }} />;
}

const Spark = ({ points, color }) => (
  <Svg width={64} height={26} viewBox="0 0 64 26"><Polyline points={points} fill="none" stroke={color} strokeWidth={1.3} strokeLinejoin="round" /></Svg>
);

const Heading = ({ kicker, title, size = 36, right, dot }) => (
  <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-end' }}>
    <View style={{ flex: 1 }}>
      <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
        {dot && <PulseDot size={7} duration={1400} />}
        <Kicker>{kicker}</Kicker>
      </View>
      <T h style={{ fontSize: size, lineHeight: size * 1.12, marginTop: 4 }}>{title}</T>
    </View>
    {right}
  </View>
);

const SheetTitle = ({ kicker, title, sub, size = 30 }) => (
  <View>
    {!!kicker && <Kicker>{kicker}</Kicker>}
    <T h style={{ fontSize: size, lineHeight: size * 1.12, marginTop: 2 }}>{title}</T>
    {!!sub && <T style={{ fontSize: 14, color: C.n800 }}>{sub}</T>}
  </View>
);

const BigIcon = ({ d, size = 96, icon = 48, bg, color = C.a800, sw = 1.5, anim = 'frame' }) => (
  <Blueprint anim={anim} style={{ width: size, height: size, alignItems: 'center', justifyContent: 'center', backgroundColor: bg }}>
    <Icon d={d} size={icon} color={color} sw={sw} />
  </Blueprint>
);

class Orbita extends React.Component {
  state = initState();
  scrollRef = React.createRef();
  token = null;
  tokenGerente = null;
  api = criarOrbitaApi({ cliente: () => this.token, gerente: () => this.tokenGerente });

  fh = (() => {
    const o = {};
    ['name', 'email', 'cpf', 'pin', 'pin2', 'pinOld', 'balance', 'ticker', 'sname', 'sector', 'price'].forEach(k => {
      o[k] = v => {
        if (k.indexOf('pin') === 0) v = v.replace(/\D/g, '').slice(0, 4);
        if (k === 'cpf') v = maskCpf(v);
        if (k === 'ticker') v = v.toUpperCase().replace(/[^A-Z0-9]/g, '').slice(0, 8);
        this.setState(s => ({ form: { ...s.form, [k]: v }, err: '' }));
      };
    });
    return o;
  })();

  componentDidMount() {
    this.inicio = lerSessao().then(sessao => {
      if (sessao && sessao.token) {
        this.token = sessao.token;
        this.setState({ sessao });
      }
    });
    this.t1 = setTimeout(() => this.inicio.then(() => this.abrirApp()), 2300);
    this.atualizarMercado();
    this.ivMercado = setInterval(() => this.atualizarMercado(), 3000);
    this.ivConta = setInterval(() => {
      if (this.token && this.state.screen === 'app' && !this.state.sheet) this.carregarCliente().catch(() => {});
    }, 8000);
    this.back = BackHandler.addEventListener('hardwareBackPress', this.onBack);
  }

  componentWillUnmount() {
    clearTimeout(this.t1); clearInterval(this.ivMercado); clearInterval(this.ivConta); clearTimeout(this.ft); clearTimeout(this.tt);
    clearTimeout(this.ct); clearTimeout(this.tc);
    this.back && this.back.remove();
  }

  componentDidUpdate(_, p) {
    if (p.tab !== this.state.tab && this.scrollRef.current) this.scrollRef.current.scrollTo({ y: 0, animated: false });
  }

  onBack = () => {
    const s = this.state;
    if (s.sheet) { this.close(); return true; }
    if (s.screen === 'signup') { this.setState({ screen: 'login', form: {}, err: '' }); return true; }
    if (s.screen === 'app' && s.tab !== 'home') { this.setState({ tab: 'home', q: '' }); return true; }
    return false;
  };

  usuario() { return paraUsuario(this.state.me, this.state.conta, this.state.carteira); }
  toast(msg) { clearTimeout(this.tt); this.setState({ toast: { msg, id: uid() } }); this.tt = setTimeout(() => this.setState({ toast: null }), 2600); }
  fail(msg) { this.setState(s => ({ err: msg, shake: s.shake + 1, pin: '' })); }

  async abrirApp() {
    if (!this.token) return this.setState({ screen: 'login' });
    try {
      await this.carregarCliente();
      this.setState({ screen: 'app' });
    } catch (erro) {
      this.encerrarSessao(erro.status === 0 ? erro.message : null);
    }
  }

  async atualizarMercado() {
    try {
      const ativos = await this.api.ativos.listar();
      this.setState(s => {
        const anteriores = Object.fromEntries(s.stocks.map(x => [x.ticker, x]));
        return { now: Date.now(), stocks: ativos.map(a => paraAcao(a, anteriores[a.ticker])) };
      });
      clearTimeout(this.ft);
      this.ft = setTimeout(() => this.setState(s => ({ stocks: s.stocks.map(x => (x.flash ? { ...x, flash: null } : x)) })), 800);
    } catch (erro) {
      this.setState({ now: Date.now() });
    }
  }

  async carregarCliente() {
    const [me, conta, carteira] = await Promise.all([
      this.api.clientes.meu(),
      this.api.contas.minha().catch(e => { if (e.status === 404) return null; throw e; }),
      this.api.carteira.minha().catch(() => this.state.carteira),
    ]);
    this.setState({ me, conta, carteira });
    if (!conta) {
      clearTimeout(this.tc);
      this.tc = setTimeout(() => this.carregarCliente().catch(() => {}), 1200);
    }
    return me;
  }

  async carregarBackoffice() {
    try {
      const [clientes, contas] = await Promise.all([this.api.clientes.listar(), this.api.contas.listar()]);
      const porCliente = Object.fromEntries(contas.map(c => [c.clienteId, c]));
      this.setState({ clientesGerente: clientes.map(c => paraClienteDoBackoffice(c, porCliente[c.id])) });
    } catch (erro) {
      if (erro.status === 401 || erro.status === 403) this.sairDoModoGerente('Sessão de gerente expirada');
      else this.toast(erro.message);
    }
  }

  async ocupar(tarefa) {
    if (this.emAndamento) return;
    this.emAndamento = true;
    this.setState({ ocupado: true });
    try {
      await tarefa();
    } catch (erro) {
      this.tratarErro(erro);
    } finally {
      this.emAndamento = false;
      this.setState({ ocupado: false });
    }
  }

  tratarErro(erro) {
    if (erro.status === 401 && erro.codigo === 'SESSAO_OBRIGATORIA' && this.state.screen === 'app') {
      this.encerrarSessao('Sessão expirada · entre novamente');
      return;
    }
    if (erro.codigo === 'CLIENTE_BLOQUEADO' || erro.codigo === 'CONTA_BLOQUEADA') {
      this.carregarCliente().catch(() => {});
      this.open({ kind: 'locked' });
      return;
    }
    this.fail(erro.message);
  }

  erroDePin(erro) {
    if (erro.codigo === 'PIN_INCORRETO') {
      this.carregarCliente().catch(() => {});
      if (erro.detalhes.bloqueado) {
        this.setState(st => ({ shake: st.shake + 1, pin: '' }));
        setTimeout(() => this.open({ kind: 'locked' }), 350);
        return;
      }
    }
    this.tratarErro(erro);
  }

  open(sheet, extra) { clearTimeout(this.ct); this.setState({ sheet, closing: false, err: '', pin: '', ...(extra || {}) }); }
  close = () => {
    if (!this.state.sheet) return;
    this.setState({ closing: true });
    clearTimeout(this.ct);
    this.ct = setTimeout(() => this.setState({ sheet: null, closing: false, err: '', pin: '', amt: '' }), 260);
  };

  press(k) {
    const sh = this.state.sheet;
    if (!sh) return;
    if (sh.kind === 'amount') this.setState(s => {
      let a = s.amt;
      if (k === 'del') a = a.slice(0, -1); else if (k === '00') a = a ? a + '00' : a; else a = (a + k).replace(/^0+/, '');
      return a.length > 9 ? null : { amt: a, err: '' };
    });
    if (sh.kind === 'pin') this.setState(s => {
      if (s.pin.length >= 4 && k !== 'del') return null;
      const p = k === 'del' ? s.pin.slice(0, -1) : s.pin + k;
      if (p.length === 4 && !this._vt) this._vt = setTimeout(() => { this._vt = null; this.verifyPin(p); }, 220);
      return { pin: p, err: '' };
    });
  }

  amountGo = () => {
    const s = this.state, sh = s.sheet, u = this.usuario(), v = parseInt(s.amt || '0', 10) / 100;
    if (!u || !sh) return;
    if (v <= 0) return this.fail('Digite um valor maior que zero');
    if (sh.op === 'dep') return this.depositar(v, sh.method || 'PIX');
    if (u.blocked) return this.open({ kind: 'locked' });
    if (v > u.balance) return this.fail('Saldo insuficiente · disponível ' + brl(u.balance));
    if (v > 5000) return this.fail('Limite por saque: R$ 5.000,00');
    this.open({ kind: 'pin', purpose: 'withdraw', v });
  };

  depositar(v, metodo) {
    return this.ocupar(async () => {
      const comprovante = await this.api.contas.depositar(v, metodo);
      await this.carregarCliente();
      this.open({ kind: 'success', title: 'Depósito confirmado', big: brl(v), lines: [['Método', metodo], ['Novo saldo', brl(Number(comprovante.novoSaldo))], ['Protocolo', '#' + comprovante.protocolo]] }, { amt: '' });
    });
  }

  sacar(v, assinatura) {
    return this.ocupar(async () => {
      const comprovante = await this.api.contas.sacar(v, assinatura);
      await this.carregarCliente();
      this.open({ kind: 'success', title: 'Saque autorizado', big: brl(v), lines: [['Código de retirada', comprovante.protocolo], ['Válido por', '30 minutos'], ['Novo saldo', brl(Number(comprovante.novoSaldo))]] }, { amt: '' });
    });
  }

  openStock(t) { this.open({ kind: 'stock', ticker: t, mode: 'buy' }, { qty: 1 }); }

  tradeGo = () => {
    const s = this.state, sh = s.sheet, u = this.usuario(), st = s.stocks.find(x => x.ticker === sh.ticker);
    if (!st || !u) return;
    const total = +(s.qty * st.price * st.fx).toFixed(2), h = u.holdings[st.ticker], held = h ? h.qty - h.reserved : 0;
    if (u.blocked) return this.open({ kind: 'locked' });
    if (sh.mode === 'buy' && total > u.balance) return this.fail('Saldo insuficiente para ' + s.qty + ' ' + st.ticker + ' · faltam ' + brl(total - u.balance));
    if (sh.mode === 'sell' && s.qty > held) return this.fail(held ? 'Você tem apenas ' + held + ' ' + st.ticker : 'Você não possui ' + st.ticker);
    this.open({ kind: 'pin', purpose: 'trade', mode: sh.mode, ticker: st.ticker, qty: s.qty, price: st.price, total });
  };

  async negociar(sh, assinatura) {
    const tipo = sh.mode === 'buy' ? 'COMPRA' : 'VENDA';
    this.open({ kind: 'processing', ticker: sh.ticker, mode: sh.mode, qty: sh.qty, total: sh.total, status: 'Enviando ordem à negociação' });
    try {
      const ordem = await this.api.ordens.enviar({ ticker: sh.ticker, tipo, quantidade: sh.qty }, assinatura);
      this.setState(st => (st.sheet && st.sheet.kind === 'processing' ? { sheet: { ...st.sheet, ordemId: ordem.id, status: sh.mode === 'buy' ? 'Debitando sua conta' : 'Reservando suas ações' } } : null));
      const final = await this.acompanharOrdem(ordem.id);
      await this.carregarCliente().catch(() => {});
      const e = EXM[final.ticker ? this.state.stocks.find(x => x.ticker === final.ticker)?.ex : null] || {};
      if (final.status === 'EXECUTADA') {
        this.open({ kind: 'success', title: sh.mode === 'buy' ? 'Ordem de compra executada' : 'Ordem de venda executada', big: brl(Number(final.valorTotal)),
          lines: [['Ativo', final.ticker], ['Quantidade', final.quantidade + ' ações'], ['Preço executado', money(Number(final.precoUnitario), final.moeda)], ['Bolsa', e.full || '—'], ['Ordem', '#' + final.id.slice(-8).toUpperCase()]] });
      } else if (final.status === 'REJEITADA') {
        this.open({ kind: 'confirm', d: IC.alert, title: 'Ordem rejeitada', body: final.motivoRejeicao || 'A ordem não pôde ser executada.', hasGo: false, cancel: 'Entendi' });
      } else {
        this.open({ kind: 'confirm', d: IC.alert, title: 'Ordem em processamento', body: 'A liquidação está demorando mais que o normal. Acompanhe pelas movimentações.', hasGo: false, cancel: 'Entendi' });
      }
    } catch (erro) {
      if (erro.status === 401 && erro.codigo === 'SESSAO_OBRIGATORIA') return this.tratarErro(erro);
      if (erro.codigo === 'CONTA_BLOQUEADA') return this.tratarErro(erro);
      this.open({ kind: 'stock', ticker: sh.ticker, mode: sh.mode }, { qty: sh.qty, err: erro.message });
    }
  }

  async acompanharOrdem(ordemId) {
    for (let tentativa = 0; tentativa < 40; tentativa++) {
      const ordem = await this.api.ordens.buscar(ordemId);
      if (ordem.status !== 'PENDENTE') return ordem;
      await esperar(400);
    }
    return { status: 'PENDENTE' };
  }

  async verifyPin(p) {
    const sh = this.state.sheet;
    if (!sh || sh.kind !== 'pin') return;
    if (sh.purpose === 'manager') return this.entrarComoGerente(p);
    if (sh.purpose === 'unblock') return this.desbloquearComPin(p);
    let assinatura;
    try {
      assinatura = (await this.api.clientes.assinar(p)).token;
    } catch (erro) {
      return this.erroDePin(erro);
    }
    if (sh.purpose === 'withdraw') return this.sacar(sh.v, assinatura);
    if (sh.purpose === 'trade') return this.negociar(sh, assinatura);
  }

  async desbloquearComPin(pin) {
    try {
      await this.api.clientes.desbloquearMinhaConta(pin);
      await this.carregarCliente();
      this.open({ kind: 'success', title: 'Conta desbloqueada', big: 'Tudo certo', lines: [['Saques', 'Liberados'], ['Ordens na bolsa', 'Liberadas']] });
    } catch (erro) {
      this.erroDePin(erro);
    }
  }

  async entrarComoGerente(codigo) {
    try {
      const credencial = await this.api.autenticacao.gerente(codigo);
      this.tokenGerente = credencial.token;
      this.setState({ mgr: true, bv: 'clients', gerente: credencial.token });
      this.close();
      this.toast('Modo gerente ativado');
      this.carregarBackoffice();
    } catch (erro) {
      this.fail(erro.status === 0 ? erro.message : 'Código de gerente inválido');
    }
  }

  sairDoModoGerente(mensagem) {
    this.tokenGerente = null;
    this.setState({ mgr: false, gerente: null, clientesGerente: [] });
    this.toast(mensagem);
  }

  login = () => {
    const f = this.state.form, email = (f.email || '').trim().toLowerCase();
    if (!email) return this.fail('Informe seu e-mail');
    if ((f.pin || '').length !== 4) return this.fail('O PIN tem 4 dígitos');
    this.ocupar(() => this.entrar(email, f.pin));
  };

  async entrar(email, pin, mensagem) {
    const resposta = await this.api.autenticacao.cliente(email, pin);
    const sessao = { token: resposta.token, clienteId: resposta.cliente.id };
    this.token = resposta.token;
    await salvarSessao(sessao);
    this.setState({ sessao, me: resposta.cliente });
    await this.carregarCliente();
    this.setState({ screen: 'app', tab: 'home', form: {}, err: '', mgr: false, gerente: null });
    this.toast(mensagem || (resposta.cliente.bloqueado ? 'Conta em modo restrito' : 'Bem-vindo(a) de volta, ' + resposta.cliente.nome.split(' ')[0]));
  }

  signup = () => {
    const f = this.state.form;
    if ((f.pin || '').length !== 4) return this.fail('Crie um PIN de 4 dígitos');
    if (f.pin !== f.pin2) return this.fail('Os PINs não conferem');
    this.ocupar(async () => {
      const cliente = await this.api.clientes.cadastrar({ nome: f.name, email: f.email, cpf: f.cpf, pin: f.pin });
      await this.entrar(cliente.email, f.pin, 'Conta criada · bem-vindo(a) à Órbita');
    });
  };

  encerrarSessao(mensagem) {
    this.token = null;
    this.tokenGerente = null;
    salvarSessao(null);
    this.setState({ sessao: null, me: null, conta: null, carteira: null, screen: 'login', form: {}, mgr: false, gerente: null, clientesGerente: [], tab: 'home', sheet: null });
    if (mensagem) this.toast(mensagem);
  }

  saveUser = () => {
    const s = this.state, sh = s.sheet, f = s.form;
    this.ocupar(async () => {
      if (sh.mode === 'self') {
        await this.api.clientes.atualizarMeu({ nome: f.name, email: f.email });
        await this.carregarCliente();
        this.close();
        return this.toast('Dados atualizados');
      }
      if (sh.mode === 'new') {
        if ((f.pin || '').length !== 4) return this.fail('PIN inicial de 4 dígitos');
        const deposito = f.balance ? parseNum(f.balance) : 0;
        if (isNaN(deposito) || deposito < 0) return this.fail('Saldo inicial inválido');
        const cliente = await this.api.clientes.cadastrarPeloGerente({ nome: f.name, email: f.email, cpf: f.cpf, pin: f.pin, depositoInicial: deposito });
        this.close();
        this.toast('Cliente ' + cliente.nome.split(' ')[0] + ' cadastrado');
        await esperar(700);
        return this.carregarBackoffice();
      }
      await this.api.clientes.atualizar(sh.id, { nome: f.name, email: f.email, cpf: f.cpf });
      this.close();
      this.toast('Dados atualizados');
      await this.carregarBackoffice();
      if (sh.id === (s.sessao && s.sessao.clienteId)) this.carregarCliente().catch(() => {});
    });
  };

  alternarBloqueio(cliente) {
    this.ocupar(async () => {
      if (cliente.blocked) await this.api.clientes.desbloquear(cliente.id);
      else await this.api.clientes.bloquear(cliente.id);
      this.toast(cliente.name.split(' ')[0] + (cliente.blocked ? ' desbloqueada(o)' : ' bloqueada(o)'));
      await this.carregarBackoffice();
      if (cliente.id === (this.state.sessao && this.state.sessao.clienteId)) this.carregarCliente().catch(() => {});
    });
  }

  removerCliente(cliente) {
    this.ocupar(async () => {
      await this.api.clientes.remover(cliente.id);
      if (cliente.id === (this.state.sessao && this.state.sessao.clienteId)) return this.encerrarSessao('Cliente excluído');
      this.setState({ sheet: null });
      this.toast('Cliente excluído');
      await this.carregarBackoffice();
    });
  }

  savePin = () => {
    const f = this.state.form;
    if ((f.pin || '').length !== 4) return this.fail('Novo PIN com 4 dígitos');
    if (f.pin !== f.pin2) return this.fail('Os PINs não conferem');
    this.ocupar(async () => {
      await this.api.clientes.alterarPin(f.pinOld, f.pin);
      this.close();
      this.toast('PIN alterado');
    });
  };

  bloquearMinhaConta() {
    this.ocupar(async () => {
      await this.api.clientes.bloquearMinhaConta();
      await this.carregarCliente();
      this.close();
      this.toast('Conta bloqueada');
    });
  }

  encerrarMinhaConta() {
    this.ocupar(async () => {
      await this.api.clientes.encerrarMinhaConta();
      this.encerrarSessao('Conta encerrada');
    });
  }

  saveStock = () => {
    const s = this.state, sh = s.sheet, f = s.form, t = (f.ticker || '').trim(), p = parseNum(f.price);
    if (t.length < 2) return this.fail('Ticker com pelo menos 2 caracteres');
    if (!(f.sname || '').trim()) return this.fail('Informe o nome da empresa');
    if (!(p > 0)) return this.fail('Preço precisa ser maior que zero');
    const dados = { ticker: t, nome: f.sname.trim(), setor: (f.sector || '').trim(), bolsa: f.ex || 'B3', cotacao: p };
    this.ocupar(async () => {
      if (sh.edit) await this.api.ativos.atualizar(t, dados);
      else await this.api.ativos.listarNaBolsa(dados);
      await this.atualizarMercado();
      if (!sh.edit) this.setState({ selEx: dados.bolsa });
      this.close();
      this.toast(sh.edit ? t + ' atualizada' : t + ' listada na ' + dados.bolsa);
    });
  };

  removerAtivo(ticker) {
    this.ocupar(async () => {
      await this.api.ativos.remover(ticker);
      await this.atualizarMercado();
      this.close();
      this.toast(ticker + ' removida');
    });
  }

  confirm(o) { this.open({ kind: 'confirm', ...o }); }

  selectEx = code => this.setState(s => ({ selEx: code, exN: s.exN + 1 }));
  go = tab => { if (this.state.tab !== tab) this.setState({ tab, q: '' }); };
  logout = () => this.encerrarSessao();

  sv(st, H) {
    const e = EXM[st.ex] || { code: st.ex }, ch = (st.price / st.hist[0] - 1) * 100, isUp = ch >= 0;
    return {
      ticker: st.ticker, name: st.name, sector: st.sector, exCode: e.code, priceStr: money(st.price, st.cur), chgStr: pct(ch), arrow: isUp ? '▲' : '▼',
      chgColor: up(isUp), spark: pts(st.hist, 64, 26).map(p => p.join(',')).join(' '), sparkColor: isUp ? C.a600 : C.n500,
      flashBg: st.flash === 'up' ? C.a100 : st.flash === 'down' ? C.n200 : undefined, held: !!(H[st.ticker] && H[st.ticker].qty),
      on: () => this.openStock(st.ticker),
      edit: () => this.open({ kind: 'stockForm', edit: true }, { form: { ticker: st.ticker, sname: st.name, sector: st.sector, price: String(st.price).replace('.', ','), ex: st.ex } }),
    };
  }

  derive() {
    const s = this.state, u = this.usuario(), SM = {};
    s.stocks.forEach(x => { SM[x.ticker] = x; });
    const infos = {};
    const now = new Date(s.now);
    EX.forEach(e => { infos[e.code] = localInfo(e, now); });
    const H = (u && u.holdings) || {};
    let pv = 0, pc = 0;
    const holdList = [];
    Object.keys(H).forEach(t => {
      const st = SM[t], h = H[t];
      if (!st || !h.qty) return;
      const val = h.qty * st.price * st.fx, cost = h.qty * h.avg * st.fx;
      pv += val; pc += cost;
      holdList.push({ ticker: t, qty: h.qty + ' un.', val: brl(val), pl: pct(cost ? (val / cost - 1) * 100 : 0), on: () => this.openStock(t) });
    });
    return { u, SM, infos, H, pv, pc, holdList, sv: st => this.sv(st, H), blocked: !!(u && u.blocked) };
  }

  renderLogin() {
    const s = this.state, fv = s.form;
    return (
      <Anim type="fadeUp" style={{ flex: 1 }}>
        <ScrollView keyboardShouldPersistTaps="handled" contentContainerStyle={{ flexGrow: 1, gap: 22, paddingHorizontal: 28, paddingTop: 28, paddingBottom: 40 }}>
          <View style={{ flexDirection: 'row', alignItems: 'center', gap: 10 }}>
            <Logo />
            <T h style={{ fontSize: 20, letterSpacing: 1.6, color: C.a800 }}>ÓRBITA</T>
          </View>
          <View style={{ marginTop: 36 }}>
            <Kicker style={{ marginBottom: 8 }}>Acesso seguro</Kicker>
            <T h style={{ fontSize: 48, lineHeight: 50 }}>Entre na sua órbita.</T>
          </View>
          <Shake n={s.shake} style={{ gap: 14 }}>
            <Field label="E-mail"><Input value={fv.email || ''} onChangeText={this.fh.email} placeholder="voce@orbita.com" keyboardType="email-address" autoCapitalize="none" style={{ minHeight: 48, fontSize: 16 }} /></Field>
            <Field label="PIN de 4 dígitos"><Input pin value={fv.pin || ''} onChangeText={this.fh.pin} placeholder="••••" onSubmitEditing={this.login} style={{ minHeight: 48, fontSize: 20, letterSpacing: 8 }} /></Field>
            <ErrBox soft msg={s.err} />
            <Cta label={s.ocupado ? 'Entrando…' : 'Entrar'} onPress={this.login} style={{ marginTop: 4 }} />
          </Shake>
          <View style={{ gap: 10 }}>
            <T style={{ fontSize: 12, color: C.n700 }}>Entrar rápido</T>
            <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
              {DEMO.map(x => (
                <Btn key={x.email} onPress={() => this.setState({ form: { email: x.email, pin: x.pin }, err: '' })} style={{ gap: 8 }}>
                  <Avatar text={ini(x.nome)} size={22} font={11} border={false} />
                  <T h style={{ fontSize: 14 }}>{x.nome}</T>
                </Btn>
              ))}
            </View>
            <T style={{ fontSize: 12, color: C.n600 }}>Demo: carla@orbita.com está bloqueada · código do gerente 0000</T>
          </View>
          <View style={{ marginTop: 'auto', gap: 6 }}>
            <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', borderTopWidth: 1, borderTopColor: C.divider, paddingTop: 16 }}>
              <T style={{ fontSize: 14, color: C.n700 }}>Ainda não é cliente?</T>
              <Btn kind="ghost" onPress={() => this.setState({ screen: 'signup', form: {}, err: '' })}><T h style={{ fontSize: 16, color: C.a700 }}>Criar conta →</T></Btn>
            </View>
            <T num style={{ fontSize: 11, color: C.n600 }}>Gateway: {URL_DO_GATEWAY}</T>
          </View>
        </ScrollView>
      </Anim>
    );
  }

  renderSignup() {
    const s = this.state, fv = s.form;
    return (
      <Anim type="fadeUp" style={{ flex: 1 }}>
        <ScrollView keyboardShouldPersistTaps="handled" contentContainerStyle={{ gap: 18, paddingHorizontal: 28, paddingTop: 12, paddingBottom: 40 }}>
          <Btn onPress={() => this.setState({ screen: 'login', form: {}, err: '' })} style={{ width: 40, height: 40, borderRadius: 20, paddingHorizontal: 0, alignSelf: 'flex-start' }}><Icon d={IC.chevronLeft} size={20} /></Btn>
          <View>
            <Kicker style={{ marginBottom: 6 }}>Nova conta · 1 minuto</Kicker>
            <T h style={{ fontSize: 40, lineHeight: 42 }}>Abra sua conta Órbita.</T>
          </View>
          <Shake n={s.shake} style={{ gap: 12 }}>
            <Field label="Nome completo"><Input value={fv.name || ''} onChangeText={this.fh.name} placeholder="Maria Souza" autoCapitalize="words" style={{ minHeight: 46, fontSize: 16 }} /></Field>
            <Field label="E-mail"><Input value={fv.email || ''} onChangeText={this.fh.email} placeholder="maria@email.com" keyboardType="email-address" autoCapitalize="none" style={{ minHeight: 46, fontSize: 16 }} /></Field>
            <Field label="CPF"><Input value={fv.cpf || ''} onChangeText={this.fh.cpf} placeholder="000.000.000-00" keyboardType="number-pad" style={{ minHeight: 46, fontSize: 16 }} /></Field>
            <View style={{ flexDirection: 'row', gap: 12 }}>
              <Field label="PIN" style={{ flex: 1 }}><Input pin value={fv.pin || ''} onChangeText={this.fh.pin} placeholder="••••" style={{ minHeight: 46 }} /></Field>
              <Field label="Confirmar PIN" style={{ flex: 1 }}><Input pin value={fv.pin2 || ''} onChangeText={this.fh.pin2} placeholder="••••" style={{ minHeight: 46 }} /></Field>
            </View>
            <ErrBox soft msg={s.err} />
            <Cta label={s.ocupado ? 'Criando…' : 'Criar conta'} onPress={this.signup} style={{ marginTop: 6 }} />
            <T style={{ fontSize: 12, color: C.n700 }}>Seu PIN autoriza saques e ordens na bolsa. Três erros bloqueiam a conta.</T>
          </Shake>
        </ScrollView>
      </Anim>
    );
  }

  renderHome(d) {
    const s = this.state, { u, pv, blocked } = d;
    const actions = [
      { label: 'Depositar', d: IC.dep, on: () => this.open({ kind: 'amount', op: 'dep', method: 'PIX' }, { amt: '' }) },
      { label: 'Sacar', d: IC.saq, locked: blocked, on: () => (blocked ? this.open({ kind: 'locked' }) : this.open({ kind: 'amount', op: 'saq' }, { amt: '' })) },
      { label: 'Investir', d: IC.chart, locked: blocked, on: () => this.go('market') },
      { label: 'Globo', d: IC.globe, on: () => this.go('globe') },
    ];
    const hide = s.hide;
    return (
      <View style={{ gap: 20, paddingHorizontal: 20, paddingTop: 10 }}>
        <View style={{ flexDirection: 'row', alignItems: 'center', gap: 12 }}>
          <Avatar text={ini(u.name)} />
          <View style={{ flex: 1 }}>
            <T h style={{ fontSize: 22, lineHeight: 24 }}>Olá, {u.name.split(' ')[0]}</T>
            <T num style={{ fontSize: 12, color: C.n700 }}>Ag {u.agency} · CC {u.acct}</T>
          </View>
          <Btn onPress={() => this.setState({ hide: !hide })} style={{ width: 40, height: 40, borderRadius: 20, paddingHorizontal: 0 }}><Icon d={hide ? IC.eyeOff : IC.eye} size={20} /></Btn>
        </View>

        <Blueprint anim="rise" style={{ paddingTop: 18, paddingHorizontal: 18, paddingBottom: 16 }}>
          <Scan />
          <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
            <Kicker>Saldo disponível</Kicker>
            <Tag kind={blocked ? 'neutral' : 'accent'} dot pulse={!blocked}>{blocked ? 'Bloqueada' : 'Ativa'}</Tag>
          </View>
          <Balance value={u.balance} id={u.id} hide={hide} />
          <View style={{ flexDirection: 'row', borderTopWidth: 1, borderTopColor: C.divider, paddingTop: 12, gap: 12 }}>
            <View style={{ flex: 1 }}><T style={{ fontSize: 11, color: C.n700 }}>Carteira de ações</T><T h num style={{ fontSize: 18 }}>{hide ? 'R$ ••••' : brl(pv)}</T></View>
            <View style={{ flex: 1 }}><T style={{ fontSize: 11, color: C.n700 }}>Patrimônio total</T><T h num style={{ fontSize: 18 }}>{hide ? 'R$ ••••' : brl(pv + u.balance)}</T></View>
          </View>
        </Blueprint>

        {blocked && (
          <Anim type="fadeUp" duration={400}>
            <Pressable onPress={() => this.open({ kind: 'locked' })} style={{ flexDirection: 'row', alignItems: 'center', gap: 12, padding: 14, backgroundColor: C.a900 }}>
              <Icon d={IC.lock} color={C.bg} />
              <View style={{ flex: 1 }}>
                <T h style={{ fontSize: 18, lineHeight: 20, color: C.bg }}>Conta bloqueada</T>
                <T style={{ fontSize: 13, color: C.a200 }}>{(REASON[u.reason] || '') + ' Toque para detalhes.'}</T>
              </View>
              <Icon d={IC.chevronRight} size={18} color={C.bg} />
            </Pressable>
          </Anim>
        )}

        <Blueprint style={{ flexDirection: 'row', gap: 1, backgroundColor: C.divider }}>
          {actions.map((a, i) => (
            <Anim key={a.label} type="rise" delay={100 + i * 60} duration={500} style={{ flex: 1 }}>
              <Pressable onPress={a.on} style={({ pressed }) => ({ gap: 18, paddingTop: 14, paddingHorizontal: 11, paddingBottom: 12, backgroundColor: pressed ? C.a200 : C.bg })}>
                <Icon d={a.d} color={C.a700} />
                <T h style={{ fontSize: 15, lineHeight: 16 }}>{a.label}</T>
                {a.locked && <Icon d={IC.lock} size={13} color={C.n700} style={{ position: 'absolute', top: 10, right: 9 }} />}
              </Pressable>
            </Anim>
          ))}
        </Blueprint>

        {s.stocks.length > 0 && <Tape items={[...s.stocks, ...s.stocks].map(d.sv)} />}

        <View>
          <View style={{ flexDirection: 'row', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 4 }}>
            <T h style={{ fontSize: 22 }}>Movimentações</T>
            <T style={{ fontSize: 12, color: C.n700 }}>{u.tx.length} registros</T>
          </View>
          {u.tx.length === 0 && <T style={{ paddingVertical: 14, fontSize: 13, color: C.n700 }}>Abrindo sua conta…</T>}
          {u.tx.slice(0, 8).map((t, i) => (
            <Anim key={t.id} type="fadeUp" delay={150 + i * 50} duration={400} style={{ flexDirection: 'row', alignItems: 'center', gap: 12, paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: C.divider }}>
              <View style={{ width: 38, height: 38, alignItems: 'center', justifyContent: 'center', borderWidth: 1, borderColor: C.divider }}>
                <Icon d={IC[t.type === 'sell' ? 'down' : t.type === 'buy' ? 'up' : t.type === 'sys' ? 'check' : t.type]} size={18} color={C.a700} />
              </View>
              <View style={{ flex: 1 }}>
                <T m numberOfLines={1} style={{ fontSize: 14 }}>{t.desc}</T>
                <T style={{ fontSize: 12, color: C.n700 }}>{fmtT(t.ts)}</T>
              </View>
              <T h num style={{ fontSize: 16, color: t.amt > 0 ? C.a700 : C.text }}>{t.amt === 0 ? '—' : (t.amt > 0 ? '+ ' : '− ') + brl(Math.abs(t.amt))}</T>
            </Anim>
          ))}
        </View>
      </View>
    );
  }

  stockRow(x, i, compact) {
    return (
      <Anim key={x.ticker} type="fadeUp" delay={compact ? 0 : Math.min(i, 12) * 30} duration={350}>
        <Row onPress={x.on} bg={x.flashBg} style={{ flexDirection: 'row', alignItems: 'center', gap: 10, paddingVertical: compact ? 10 : 12, paddingHorizontal: compact ? 0 : 6,
          borderColor: C.divider, [compact ? 'borderTopWidth' : 'borderBottomWidth']: 1 }}>
          <View style={{ flex: 1 }}>
            <View style={{ flexDirection: 'row', alignItems: 'center', gap: 6 }}>
              <T h style={{ fontSize: compact ? 17 : 18 }}>{x.ticker}</T>
              {!compact && <View style={{ paddingHorizontal: 5, paddingVertical: 1, borderWidth: 1, borderColor: C.divider }}><T style={{ fontSize: 10, letterSpacing: 0.6, color: C.n700 }}>{x.exCode}</T></View>}
              {!compact && x.held && <View style={{ width: 6, height: 6, borderRadius: 3, backgroundColor: C.accent }} />}
            </View>
            <T numberOfLines={1} style={{ fontSize: 12, color: C.n700 }}>{x.name}</T>
          </View>
          <Spark points={x.spark} color={x.sparkColor} />
          <View style={{ minWidth: 96, alignItems: 'flex-end' }}>
            <T h num style={{ fontSize: compact ? 15 : 16 }}>{x.priceStr}</T>
            <T num style={{ fontSize: 12, color: x.chgColor }}>{x.arrow} {x.chgStr}</T>
          </View>
        </Row>
      </Anim>
    );
  }

  renderMarket(d) {
    const s = this.state, { H, pv, pc, holdList } = d, q = (s.q || '').trim().toLowerCase();
    let ml = s.stocks;
    if (s.mf === 'Carteira') ml = ml.filter(x => H[x.ticker] && H[x.ticker].qty);
    else if (s.mf !== 'Todas') ml = ml.filter(x => x.ex === s.mf);
    if (q) ml = ml.filter(x => x.ticker.toLowerCase().includes(q) || x.name.toLowerCase().includes(q));
    const hide = s.hide;
    return (
      <View style={{ gap: 16, paddingHorizontal: 20, paddingTop: 10 }}>
        <Heading dot kicker="Mercado · ao vivo" title="Bolsa global" />
        <Blueprint anim="rise" style={{ padding: 16 }}>
          <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'baseline' }}>
            <Kicker>Sua carteira</Kicker>
            {!!pc && <T num style={{ fontSize: 13, color: up(pv >= pc) }}>{(pv >= pc ? '▲ ' : '▼ ') + pct((pv / pc - 1) * 100) + ' · ' + brl(pv - pc)}</T>}
          </View>
          <T h num style={{ fontSize: 38, lineHeight: 42, marginTop: 6 }}>{hide ? 'R$ ••••' : brl(pv)}</T>
          {holdList.length > 0 ? (
            <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ gap: 8, marginTop: 12 }}>
              {holdList.map(h => (
                <Row key={h.ticker} onPress={h.on} style={{ gap: 2, paddingVertical: 8, paddingHorizontal: 12, borderRadius: 12, borderWidth: 1, borderColor: C.divider }}>
                  <T h style={{ fontSize: 15 }}>{h.ticker} <T style={{ fontSize: 12, color: C.n700 }}>{h.qty}</T></T>
                  <T num style={{ fontSize: 12 }}>{h.val} · {h.pl}</T>
                </Row>
              ))}
            </ScrollView>
          ) : <T style={{ fontSize: 13, color: C.n700, marginTop: 6 }}>Nenhuma ação ainda. Toque em um ativo para comprar.</T>}
        </Blueprint>
        {this.searchBox('Buscar ticker ou empresa', 44)}
        <ScrollView horizontal showsHorizontalScrollIndicator={false} style={{ marginHorizontal: -20 }} contentContainerStyle={{ gap: 6, paddingHorizontal: 20 }}>
          {['Todas', 'Carteira', ...EX.map(e => e.code)].map(f => this.chip(f, s.mf === f, () => this.setState({ mf: f })))}
        </ScrollView>
        <View style={{ borderTopWidth: 1, borderTopColor: C.divider }}>
          {ml.map((st, i) => this.stockRow(d.sv(st), i))}
          {ml.length === 0 && <T style={{ paddingVertical: 28, fontSize: 14, color: C.n700 }}>{s.stocks.length ? 'Nenhum ativo encontrado.' : 'Carregando cotações…'}</T>}
        </View>
      </View>
    );
  }

  searchBox(placeholder, h) {
    return (
      <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8, borderWidth: 1, borderColor: C.divider, borderRadius: 12, paddingHorizontal: 12, backgroundColor: C.surface, flex: h === 40 ? 1 : undefined }}>
        <Icon d={IC.search} size={h === 40 ? 16 : 18} color={C.n600} />
        <Input value={this.state.q} onChangeText={q => this.setState({ q })} placeholder={placeholder} autoCapitalize="none"
          style={{ flex: 1, minHeight: h, borderWidth: 0, backgroundColor: 'transparent', paddingHorizontal: 0, fontSize: h === 40 ? 14 : 15 }} />
      </View>
    );
  }

  chip(label, on, onPress, dot) {
    const fg = on ? C.bg : C.text;
    return (
      <Pressable key={label} onPress={onPress} style={{ flexDirection: 'row', alignItems: 'center', gap: 7, paddingVertical: dot === undefined ? 6 : 7, paddingHorizontal: dot === undefined ? 14 : 13,
        borderRadius: 999, borderWidth: 1, borderColor: on ? C.accent : C.divider, backgroundColor: on ? C.accent : 'transparent' }}>
        {dot !== undefined && <View style={{ width: 7, height: 7, borderRadius: 4, borderWidth: 1, borderColor: fg, backgroundColor: dot ? fg : 'transparent' }} />}
        <T h style={{ fontSize: 14, color: fg }}>{label}</T>
      </Pressable>
    );
  }

  renderGlobe(d) {
    const s = this.state, { infos } = d, selE = EXM[s.selEx] || EX[0], selI = infos[selE.code];
    const exStocks = s.stocks.filter(x => x.ex === selE.code);
    return (
      <View>
        <View style={{ paddingHorizontal: 20, paddingTop: 10 }}>
          <Heading kicker="Terminal global" title="Mercados no mundo" right={
            <View style={{ alignItems: 'flex-end' }}>
              <T h num style={{ fontSize: 20 }}>{EX.filter(e => infos[e.code].open).length + '/' + EX.length}</T>
              <T style={{ fontSize: 12, color: C.n700 }}>bolsas abertas</T>
            </View>} />
        </View>
        <View style={{ marginTop: 4 }}>
          <Globe selEx={s.selEx} exN={s.exN} openKey={EX.map(e => (infos[e.code].open ? '1' : '0')).join('')} onSelect={this.selectEx} scrollRef={this.scrollRef} />
        </View>
        <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ gap: 6, paddingHorizontal: 20, paddingTop: 4, paddingBottom: 14 }}>
          {EX.map(e => this.chip(e.code, e.code === s.selEx, () => this.selectEx(e.code), !!infos[e.code].open))}
        </ScrollView>
        <View style={{ paddingHorizontal: 20 }}>
          <Blueprint key={s.exN} anim={s.exN % 2 ? 'fadeUp' : 'rise'} style={{ padding: 16 }}>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12 }}>
              <View style={{ flex: 1 }}>
                <Kicker>{selE.city} · {selE.cur}</Kicker>
                <T h style={{ fontSize: 24, lineHeight: 27 }}>{selE.full}</T>
              </View>
              <View style={{ alignItems: 'flex-end' }}>
                <T h num style={{ fontSize: 24, lineHeight: 27 }}>{selI.time}</T>
                <Tag kind={selI.open ? 'accent' : 'neutral'}>{selI.open ? 'Aberta' : 'Fechada'}</Tag>
              </View>
            </View>
            <View style={{ flexDirection: 'row', flexWrap: 'wrap', columnGap: 16, marginTop: 10, marginBottom: 4 }}>
              <T num style={{ fontSize: 12, color: C.n700 }}>{Math.abs(selE.lat).toFixed(2) + '°' + (selE.lat < 0 ? 'S' : 'N') + ' ' + Math.abs(selE.lon).toFixed(2) + '°' + (selE.lon < 0 ? 'W' : 'E')}</T>
              <T num style={{ fontSize: 12, color: C.n700 }}>{fh2(selE.o) + '–' + fh2(selE.c) + ' local'}</T>
              <T num style={{ fontSize: 12, color: C.n700 }}>{selE.cur === 'BRL' ? 'Moeda local' : '1 ' + selE.cur + ' = ' + brl(exStocks.length ? exStocks[0].fx : selE.fx)}</T>
            </View>
            {exStocks.map((st, i) => this.stockRow(d.sv(st), i, true))}
            {exStocks.length === 0 && <T style={{ borderTopWidth: 1, borderTopColor: C.divider, paddingTop: 10, fontSize: 13, color: C.n700 }}>Nenhuma ação listada nesta bolsa. Cadastre uma na aba Banco.</T>}
          </Blueprint>
        </View>
      </View>
    );
  }

  renderBank(d) {
    const s = this.state, q = (s.q || '').trim().toLowerCase();
    if (!s.mgr) {
      return (
        <View style={{ gap: 18, paddingHorizontal: 20, paddingTop: 50 }}>
          <BigIcon d={IC.shield} icon={40} color={C.a700} sw={1.2} />
          <View>
            <Kicker>Área restrita</Kicker>
            <T h style={{ fontSize: 36, lineHeight: 40, marginTop: 4, marginBottom: 6 }}>Painel do gerente</T>
            <T style={{ fontSize: 15, color: C.n800 }}>Cadastre, edite, bloqueie e exclua clientes. Liste novas ações nas bolsas do globo.</T>
          </View>
          <Cta label="Entrar com código" onPress={() => this.open({ kind: 'pin', purpose: 'manager' })} />
          <T style={{ fontSize: 12, color: C.n700 }}>Código de demonstração: 0000</T>
        </View>
      );
    }
    let clients = s.clientesGerente;
    if (q) clients = clients.filter(x => x.name.toLowerCase().includes(q) || x.email.includes(q));
    const stats = [['Clientes', String(s.clientesGerente.length)], ['Custódia', brl(s.clientesGerente.reduce((a, x) => a + x.balance, 0)).replace(/,\d\d$/, '')], ['Bloqueadas', String(s.clientesGerente.filter(x => x.blocked).length)]];
    return (
      <View style={{ gap: 16, paddingHorizontal: 20, paddingTop: 10 }}>
        <Heading kicker="Backoffice · gerente" title="Banco Órbita" right={
          <Btn icon={IC.logout} onPress={() => this.sairDoModoGerente('Modo gerente encerrado')}>Sair</Btn>} />
        <Blueprint style={{ flexDirection: 'row', gap: 1, backgroundColor: C.divider }}>
          {stats.map(([k, v], i) => (
            <Anim key={k} type="rise" delay={i * 70} duration={500} style={{ flex: 1, backgroundColor: C.bg, padding: 12 }}>
              <T style={{ fontSize: 11, color: C.n700 }}>{k}</T>
              <T h num numberOfLines={1} adjustsFontSizeToFit style={{ fontSize: 22, lineHeight: 26 }}>{v}</T>
            </Anim>
          ))}
        </Blueprint>
        <Seg options={[
          { label: 'Clientes', on: s.bv === 'clients', pick: () => { this.setState({ bv: 'clients', q: '' }); this.carregarBackoffice(); } },
          { label: 'Ações', on: s.bv === 'stocks', pick: () => this.setState({ bv: 'stocks', q: '' }) },
        ]} />
        {s.bv === 'clients' ? (
          <Anim key="c" type="fadeUp" duration={350} style={{ gap: 12 }}>
            <View style={{ flexDirection: 'row', gap: 8 }}>
              {this.searchBox('Buscar cliente', 40)}
              <Btn kind="primary" icon={IC.plus} onPress={() => this.open({ kind: 'userForm', mode: 'new' }, { form: {} })}>Novo</Btn>
            </View>
            <View style={{ borderTopWidth: 1, borderTopColor: C.divider }}>
              {clients.map((x, i) => (
                <Anim key={x.id} type="fadeUp" delay={i * 40} duration={350}>
                  <Row onPress={() => this.open({ kind: 'userForm', mode: 'admin', id: x.id }, { form: { name: x.name, email: x.email, cpf: x.cpf } })}
                    style={{ flexDirection: 'row', alignItems: 'center', gap: 12, paddingVertical: 12, paddingHorizontal: 4, borderBottomWidth: 1, borderBottomColor: C.divider }}>
                    <Avatar text={ini(x.name)} size={38} font={14} bg="transparent" />
                    <View style={{ flex: 1 }}>
                      <T m style={{ fontSize: 15 }}>{x.name}{s.sessao && x.id === s.sessao.clienteId ? ' · você' : ''}</T>
                      <T numberOfLines={1} style={{ fontSize: 12, color: C.n700 }}>{x.email}</T>
                    </View>
                    <View style={{ alignItems: 'flex-end', gap: 2 }}>
                      <T h num style={{ fontSize: 15 }}>{brl(x.balance)}</T>
                      <Tag kind={x.blocked ? 'neutral' : 'accent'} style={{ paddingHorizontal: 7, paddingVertical: 1 }}>{x.blocked ? 'Bloqueada' : 'Ativa'}</Tag>
                    </View>
                  </Row>
                </Anim>
              ))}
            </View>
          </Anim>
        ) : (
          <Anim key="s" type="fadeUp" duration={350} style={{ gap: 12 }}>
            <Cta label="Cadastrar nova ação" icon={IC.plus} h={48} size={16} onPress={() => this.open({ kind: 'stockForm', edit: false }, { form: { ex: 'B3' } })} />
            <View style={{ borderTopWidth: 1, borderTopColor: C.divider }}>
              {s.stocks.map(st => {
                const x = d.sv(st);
                return (
                  <Row key={x.ticker} onPress={x.edit} style={{ flexDirection: 'row', alignItems: 'center', gap: 12, paddingVertical: 11, paddingHorizontal: 4, borderBottomWidth: 1, borderBottomColor: C.divider }}>
                    <View style={{ flex: 1 }}>
                      <T h style={{ fontSize: 17 }}>{x.ticker} <T style={{ fontSize: 11, color: C.n700 }}>{x.exCode} · {x.sector}</T></T>
                      <T numberOfLines={1} style={{ fontSize: 12, color: C.n700 }}>{x.name}</T>
                    </View>
                    <T h num style={{ fontSize: 15 }}>{x.priceStr}</T>
                    <Icon d={IC.pencil} size={16} color={C.n600} />
                  </Row>
                );
              })}
            </View>
          </Anim>
        )}
      </View>
    );
  }

  renderProfile(d) {
    const { u, blocked, holdList } = d;
    const menu = [
      { d: IC.pencil, label: 'Editar dados', sub: 'Nome e e-mail', on: () => this.open({ kind: 'userForm', mode: 'self', id: u.id }, { form: { name: u.name, email: u.email } }) },
      { d: IC.key, label: 'Alterar PIN', sub: 'Usado em saques e ordens', on: () => this.open({ kind: 'changePin' }, { form: {} }) },
      blocked
        ? { d: IC.unlock, label: 'Desbloquear conta', sub: REASON[u.reason] || '', on: () => this.open({ kind: 'locked' }) }
        : { d: IC.lock, label: 'Bloquear minha conta', sub: 'Suspende saques e ordens na hora', on: () => this.confirm({ d: IC.lock, title: 'Bloquear conta?', body: 'Saques e ordens na bolsa ficam suspensos até você desbloquear com seu PIN. Depósitos continuam liberados.', label: 'Bloquear agora', hasGo: true, cancel: 'Cancelar',
          go: () => this.bloquearMinhaConta() }) },
      { d: IC.trash, label: 'Encerrar conta', sub: 'Exclui seus dados da Órbita', on: () => {
        const hasAssets = u.balance >= 0.01 || holdList.length > 0;
        this.confirm(hasAssets
          ? { d: IC.alert, title: 'Ainda há saldo', body: 'Para encerrar, saque ' + brl(u.balance) + (holdList.length ? ' e venda suas ' + holdList.length + ' posições em ações' : '') + ' primeiro.', hasGo: false, cancel: 'Entendi' }
          : { d: IC.trash, title: 'Encerrar conta?', body: 'Sua conta e histórico serão excluídos. Essa ação não pode ser desfeita.', label: 'Encerrar definitivamente', hasGo: true, cancel: 'Manter conta',
            go: () => this.encerrarMinhaConta() });
      } },
      { d: IC.logout, label: 'Sair', sub: 'Voltar para a tela de acesso', on: this.logout },
    ];
    return (
      <View style={{ gap: 22, paddingHorizontal: 20, paddingTop: 20 }}>
        <View style={{ flexDirection: 'row', gap: 16, alignItems: 'center' }}>
          <Blueprint anim="frame" style={{ width: 76, height: 76, alignItems: 'center', justifyContent: 'center', backgroundColor: C.a100 }}>
            <T h style={{ fontSize: 30, color: C.a800 }}>{ini(u.name)}</T>
          </Blueprint>
          <View style={{ flex: 1 }}>
            <T h style={{ fontSize: 30, lineHeight: 32 }}>{u.name}</T>
            <T style={{ fontSize: 13, color: C.n700 }}>{u.email}</T>
            <Tag kind={blocked ? 'neutral' : 'accent'} style={{ marginTop: 6 }}>{blocked ? 'Bloqueada' : 'Ativa'}</Tag>
          </View>
        </View>
        <InfoGrid items={[['CPF', '•••.' + (u.cpf || '').slice(4, 11) + '-••'], ['Conta', u.agency + ' · ' + u.acct], ['Cliente desde', u.since], ['Tentativas de PIN', (u.fails || 0) + ' de ' + (u.maxFails || MAX_ATTEMPTS)]]} />
        <View style={{ borderTopWidth: 1, borderTopColor: C.divider }}>
          {menu.map((m, i) => (
            <Anim key={m.label} type="fadeUp" delay={i * 50} duration={350}>
              <Row onPress={m.on} style={{ flexDirection: 'row', alignItems: 'center', gap: 14, paddingVertical: 14, paddingHorizontal: 4, borderBottomWidth: 1, borderBottomColor: C.divider }}>
                <Icon d={m.d} size={20} color={C.a700} />
                <View style={{ flex: 1 }}>
                  <T m style={{ fontSize: 15 }}>{m.label}</T>
                  <T style={{ fontSize: 12, color: C.n700 }}>{m.sub}</T>
                </View>
                <Icon d={IC.chevronRight} size={16} color={C.n600} />
              </Row>
            </Anim>
          ))}
        </View>
        <Kicker color={C.n600} style={{ letterSpacing: 1.3 }}>Órbita · microserviços v2.0</Kicker>
      </View>
    );
  }

  keypad(kind) {
    const keys = kind === 'pin' ? ['1', '2', '3', '4', '5', '6', '7', '8', '9', '', '0', 'del'] : ['1', '2', '3', '4', '5', '6', '7', '8', '9', '00', '0', 'del'];
    const rows = [0, 3, 6, 9].map(i => keys.slice(i, i + 3));
    return (
      <View style={{ gap: 1, backgroundColor: C.divider, borderWidth: 1, borderColor: C.divider, borderRadius: 16, overflow: 'hidden', marginTop: 16 }}>
        {rows.map((r, i) => (
          <View key={i} style={{ flexDirection: 'row', gap: 1 }}>
            {r.map((k, j) => (
              <Pressable key={j} onPress={() => k && this.press(k)} style={({ pressed }) => ({ flex: 1, height: 58, alignItems: 'center', justifyContent: 'center', backgroundColor: pressed && k ? C.a300 : C.bg })}>
                {k === 'del' ? <Icon d={IC.del} /> : <T h num style={{ fontSize: 26 }}>{k}</T>}
              </Pressable>
            ))}
          </View>
        ))}
      </View>
    );
  }

  sheetAmount(sh, u) {
    const s = this.state, v = parseInt(s.amt || '0', 10) / 100, dep = sh.op === 'dep';
    return (
      <>
        <Anim type="fadeUp" duration={350} style={{ gap: 14 }}>
          <SheetTitle kicker={dep ? 'Entrada' : 'Saída · requer PIN'} title={dep ? 'Depositar' : 'Sacar'} />
          {dep && <Seg options={['PIX', 'TED', 'Boleto'].map(m => ({ label: m, on: sh.method === m, pick: () => this.setState(st => ({ sheet: { ...st.sheet, method: m } })) }))} />}
          <View style={{ flexDirection: 'row', alignItems: 'baseline', gap: 8, borderBottomWidth: 1, borderBottomColor: C.divider, paddingTop: 8, paddingBottom: 12 }}>
            <T h style={{ fontSize: 24, color: C.n600 }}>R$</T>
            <T h num style={{ fontSize: 56, lineHeight: 60, color: v > 0 ? C.text : C.n500 }}>{brl(v).replace(/^R\$\s?/, '')}</T>
            <Blink />
          </View>
          <View style={{ flexDirection: 'row', justifyContent: 'space-between' }}>
            <T style={{ fontSize: 13, color: C.n700 }}>Saldo {brl(u ? u.balance : 0)}</T>
            <T style={{ fontSize: 13, color: C.n700 }}>{dep ? 'Máx. R$ 50 mil' : 'Máx. R$ 5 mil / saque'}</T>
          </View>
          <View style={{ flexDirection: 'row', gap: 6 }}>
            {[50, 100, 500, 1000].map(q => <Btn key={q} onPress={() => this.setState({ amt: String(Math.round((v + q) * 100)), err: '' })} style={{ flex: 1 }}>{'+' + q}</Btn>)}
          </View>
        </Anim>
        {!!s.err && <View style={{ marginTop: 12 }}><ErrBox icon msg={s.err} /></View>}
        {this.keypad('amount')}
        <Cta label={s.ocupado ? 'Processando…' : dep ? 'Depositar ' + brl(v) : 'Continuar'} h={54} onPress={this.amountGo} style={{ marginTop: 16 }} />
      </>
    );
  }

  sheetPin(sh, u) {
    const s = this.state, fails = (u && u.fails) || 0, max = (u && u.maxFails) || MAX_ATTEMPTS;
    const t = {
      withdraw: ['Autorizar saque', 'Confirme com seu PIN', sh.v ? 'Saque de ' + brl(sh.v) : ''],
      trade: ['Confirmar ordem', 'Assinatura digital', sh.ticker ? (sh.mode === 'buy' ? 'Compra' : 'Venda') + ' de ' + sh.qty + ' ' + sh.ticker + ' · ' + brl(sh.total) : ''],
      manager: ['Código do gerente', 'Acesso restrito', 'Digite o código de 4 dígitos · demo 0000'],
      unblock: ['Desbloquear conta', 'Segurança', 'Confirme seu PIN para reativar saques e ordens'],
    }[sh.purpose] || ['', '', ''];
    return (
      <>
        <Anim type="fadeUp" duration={350} style={{ gap: 16 }}>
          <SheetTitle kicker={t[1]} title={t[0]} sub={t[2]} />
          <View style={{ flexDirection: 'row', gap: 10 }}>
            {[0, 1, 2, 3].map(i => {
              const filled = i < s.pin.length;
              return (
                <View key={i} style={{ flex: 1, height: 62, alignItems: 'center', justifyContent: 'center', borderRadius: 14, borderWidth: 1,
                  borderColor: i === s.pin.length ? C.accent : C.divider, backgroundColor: filled ? C.a100 : 'transparent' }}>
                  {filled && <Anim type="pop" duration={300}><View style={{ width: 14, height: 14, borderRadius: 7, backgroundColor: C.a800 }} /></Anim>}
                </View>
              );
            })}
          </View>
          {sh.purpose !== 'manager' && (
            <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
              <T style={{ fontSize: 12, color: C.n700 }}>Tentativas</T>
              {Array.from({ length: max }, (_, i) => <View key={i} style={{ width: 10, height: 10, borderWidth: 1, borderColor: C.a700, backgroundColor: i < fails ? C.a700 : 'transparent' }} />)}
            </View>
          )}
        </Anim>
        {!!s.err && <View style={{ marginTop: 12 }}><ErrBox icon msg={s.err} /></View>}
        {this.keypad('pin')}
      </>
    );
  }

  sheetProcessing(sh) {
    return (
      <View style={{ gap: 16, paddingTop: 8 }}>
        <Blueprint anim="frame" style={{ width: 96, height: 96, alignItems: 'center', justifyContent: 'center', backgroundColor: C.a100 }}>
          <Logo size={48} core={15} sat={8} duration={1100} />
        </Blueprint>
        <Anim type="fadeUp" delay={150}>
          <Kicker>{sh.mode === 'buy' ? 'Ordem de compra' : 'Ordem de venda'}</Kicker>
          <T h style={{ fontSize: 34, lineHeight: 38 }}>{sh.qty} {sh.ticker}</T>
          <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8, marginTop: 4 }}>
            <PulseDot size={7} duration={900} />
            <T style={{ fontSize: 15, color: C.n800 }}>{sh.status}…</T>
          </View>
        </Anim>
        <InfoGrid items={[['Total estimado', brl(sh.total)], ['Ordem', sh.ordemId ? '#' + sh.ordemId.slice(-8).toUpperCase() : 'enviando']]} />
        <T style={{ fontSize: 12, color: C.n700 }}>A ordem passa pela negociação, pela sua conta e pela sua carteira antes de ser executada.</T>
      </View>
    );
  }

  sheetSuccess(sh) {
    return (
      <View style={{ gap: 16, paddingTop: 8 }}>
        <BigIcon d={IC.check} bg={C.a100} sw={1.6} />
        <Anim type="fadeUp" delay={200}>
          <Kicker>{sh.title}</Kicker>
          <T h num style={{ fontSize: 48, lineHeight: 52 }}>{sh.big}</T>
        </Anim>
        <Anim type="fadeUp" delay={300} style={{ borderTopWidth: 1, borderTopColor: C.divider }}>
          {sh.lines.map(([k, v]) => (
            <View key={k} style={{ flexDirection: 'row', justifyContent: 'space-between', gap: 12, paddingVertical: 10, borderBottomWidth: 1, borderBottomColor: C.divider }}>
              <T style={{ fontSize: 14, color: C.n700 }}>{k}</T>
              <T num style={{ fontSize: 14, textAlign: 'right', flexShrink: 1 }}>{v}</T>
            </View>
          ))}
        </Anim>
        <Cta label="Concluir" icon={IC.check} onPress={this.close} />
      </View>
    );
  }

  sheetLocked(u) {
    const max = (u && u.maxFails) || MAX_ATTEMPTS;
    const why = u && u.blocked ? (u.reason === 'pin' ? 'Detectamos ' + max + ' tentativas de PIN incorretas e bloqueamos a conta para proteger seu dinheiro.'
      : u.reason === 'admin' ? 'O gerente da sua conta aplicou um bloqueio preventivo.' : 'Você mesmo bloqueou a conta. Desbloqueie com o seu PIN quando quiser.') : '';
    const how = u && u.blocked && u.reason !== 'user' ? 'Desbloqueio pelo gerente: aba Banco → cliente → Desbloquear (código 0000).' : '';
    return (
      <View style={{ gap: 16, paddingTop: 8 }}>
        <BigIcon d={IC.lock} icon={46} bg={C.a900} color={C.bg} sw={1.4} />
        <Anim type="fadeUp" delay={150}>
          <Kicker>Segurança</Kicker>
          <T h style={{ fontSize: 32, lineHeight: 36, marginTop: 2, marginBottom: 6 }}>Conta bloqueada</T>
          <T style={{ fontSize: 15, color: C.n800 }}>{why}</T>
        </Anim>
        <Anim type="fadeUp" delay={250}><InfoGrid items={[['Suspenso', 'Saques e ordens na bolsa'], ['Liberado', 'Depósitos e consultas']]} /></Anim>
        {!!how && <T style={{ fontSize: 13, color: C.n700 }}>{how}</T>}
        {!!(u && u.blocked && u.reason === 'user') && <Cta label="Desbloquear com PIN" icon={IC.unlock} onPress={() => this.open({ kind: 'pin', purpose: 'unblock' })} />}
        <Plain label="Entendi" onPress={this.close} style={{ minHeight: 48, paddingHorizontal: 18 }} />
      </View>
    );
  }

  sheetStock(sh, d) {
    const s = this.state, st = d.SM[sh.ticker];
    if (!st) return null;
    const b = d.sv(st), e = EXM[st.ex] || { full: st.ex }, P = pts(st.hist, 316, 120, 8), last = P[P.length - 1], h = d.H[st.ticker], held = h ? h.qty - h.reserved : 0, buy = sh.mode === 'buy';
    const line = P.map(p => p.join(',')).join(' ');
    return (
      <Anim type="fadeUp" duration={350} style={{ gap: 14 }}>
        <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12 }}>
          <View style={{ flex: 1 }}>
            <Kicker>{e.full} · {st.sector}</Kicker>
            <T h style={{ fontSize: 34, lineHeight: 36, marginTop: 2 }}>{st.ticker}</T>
            <T style={{ fontSize: 14, color: C.n800 }}>{st.name}</T>
          </View>
          <View style={{ alignItems: 'flex-end' }}>
            <T h num style={{ fontSize: 28, lineHeight: 30, paddingHorizontal: 4, backgroundColor: b.flashBg }}>{b.priceStr}</T>
            <T num style={{ fontSize: 13, color: b.chgColor }}>{b.arrow} {b.chgStr} hoje</T>
          </View>
        </View>
        <Blueprint style={{ paddingTop: 10, paddingBottom: 4 }}>
          <View style={{ flexDirection: 'row', alignItems: 'center', gap: 6, paddingHorizontal: 10, paddingBottom: 4 }}>
            <PulseDot size={6} duration={1400} />
            <Kicker style={{ fontSize: 10 }}>Ao vivo · {st.hist.length} ticks</Kicker>
          </View>
          <View style={{ width: '100%', aspectRatio: 324 / 120 }}>
            <Svg width="100%" height="100%" viewBox="0 0 324 120">
              {[30, 60, 90].map(y => <Line key={y} x1={0} y1={y} x2={324} y2={y} stroke={C.divider} strokeDasharray="2 4" />)}
              <Polygon points={'0,120 ' + line + ' 316,120'} fill={C.a200} fillOpacity={0.7} />
              <Polyline points={line} fill="none" stroke={C.a700} strokeWidth={1.5} strokeLinejoin="round" />
              <Line x1={0} y1={last[1]} x2={324} y2={last[1]} stroke={C.a500} strokeDasharray="3 3" />
              <Rect x={last[0] - 4} y={last[1] - 4} width={8} height={8} fill={C.a800} />
            </Svg>
          </View>
        </Blueprint>
        <InfoGrid cols={3} pad={8} items={[['Abertura', money(st.hist[0], st.cur)], ['Máxima', money(Math.max(...st.hist), st.cur)], ['Mínima', money(Math.min(...st.hist), st.cur)]]} />
        <Seg options={[
          { label: 'Comprar', on: buy, pick: () => this.setState(x => ({ sheet: { ...x.sheet, mode: 'buy' }, err: '' })) },
          { label: 'Vender · ' + held + ' un.', on: !buy, pick: () => this.setState(x => ({ sheet: { ...x.sheet, mode: 'sell' }, err: '' })) },
        ]} />
        <View style={{ flexDirection: 'row', alignItems: 'center', borderWidth: 1, borderColor: C.divider, borderRadius: 14, overflow: 'hidden' }}>
          {[[IC.minus, () => this.setState(x => ({ qty: Math.max(1, x.qty - 1), err: '' }))], null, [IC.plus, () => this.setState(x => ({ qty: Math.min(9999, x.qty + 1), err: '' }))]].map((btn, i) => btn ? (
            <Pressable key={i} onPress={btn[1]} style={({ pressed }) => ({ width: 54, height: 54, alignItems: 'center', justifyContent: 'center', backgroundColor: pressed ? C.a200 : 'transparent', borderColor: C.divider, [i ? 'borderLeftWidth' : 'borderRightWidth']: 1 })}>
              <Icon d={btn[0]} size={20} />
            </Pressable>
          ) : (
            <View key={i} style={{ flex: 1, alignItems: 'center' }}>
              <T h num style={{ fontSize: 28, lineHeight: 30 }}>{s.qty}</T>
              <T style={{ fontSize: 11, color: C.n700 }}>ações</T>
            </View>
          ))}
        </View>
        <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
          <View>
            <T style={{ fontSize: 12, color: C.n700 }}>Total estimado</T>
            <T style={{ fontSize: 12, color: C.n700 }}>{st.cur === 'BRL' ? 'Liquidação em D+2' : 'Câmbio 1 ' + st.cur + ' = ' + brl(st.fx)}</T>
          </View>
          <T h num style={{ fontSize: 28 }}>{brl(s.qty * st.price * st.fx)}</T>
        </View>
        <ErrBox icon msg={s.err} />
        <Cta label={(buy ? 'Comprar ' : 'Vender ') + s.qty + ' ' + st.ticker} icon={buy ? IC.up : IC.down} h={54} onPress={this.tradeGo} />
        <T style={{ fontSize: 12, color: C.n700 }}>Saldo disponível {brl(d.u.balance)} · ordem confirmada com PIN</T>
      </Anim>
    );
  }

  sheetUser(sh) {
    const s = this.state, fv = s.form, tu = sh.id && s.clientesGerente.find(x => x.id === sh.id), isNew = sh.mode === 'new', isAdmin = sh.mode === 'admin';
    return (
      <Anim type="fadeUp" duration={350} style={{ gap: 12 }}>
        <SheetTitle kicker={sh.mode === 'self' ? 'Perfil' : 'Backoffice · cliente'} title={isNew ? 'Novo cliente' : sh.mode === 'self' ? 'Editar dados' : tu ? tu.name : ''} />
        {isAdmin && tu && <InfoGrid cols={3} pad={8} items={[['Saldo', brl(tu.balance)], ['Conta', tu.acct], ['Status', tu.blocked ? 'Bloqueada' : 'Ativa']]} />}
        <Field label="Nome completo"><Input value={fv.name || ''} onChangeText={this.fh.name} autoCapitalize="words" /></Field>
        <Field label="E-mail"><Input value={fv.email || ''} onChangeText={this.fh.email} keyboardType="email-address" autoCapitalize="none" /></Field>
        {sh.mode !== 'self' && <Field label="CPF"><Input value={fv.cpf || ''} onChangeText={this.fh.cpf} placeholder="000.000.000-00" keyboardType="number-pad" /></Field>}
        {isNew && (
          <View style={{ flexDirection: 'row', gap: 12 }}>
            <Field label="PIN inicial" style={{ flex: 1 }}><Input pin value={fv.pin || ''} onChangeText={this.fh.pin} placeholder="••••" /></Field>
            <Field label="Saldo inicial (R$)" style={{ flex: 1 }}><Input value={fv.balance || ''} onChangeText={this.fh.balance} placeholder="0,00" keyboardType="decimal-pad" /></Field>
          </View>
        )}
        <ErrBox msg={s.err} />
        <Cta label={isNew ? 'Cadastrar cliente' : 'Salvar alterações'} icon={IC.check} onPress={this.saveUser} style={{ marginTop: 4 }} />
        {isAdmin && tu && (
          <View style={{ flexDirection: 'row', gap: 8 }}>
            <Plain style={{ flex: 1 }} icon={tu.blocked ? IC.unlock : IC.lock} label={tu.blocked ? 'Desbloquear' : 'Bloquear'} onPress={() => this.alternarBloqueio(tu)} />
            <Plain style={{ flex: 1 }} icon={IC.trash} label="Excluir cliente" onPress={() => this.confirm({ d: IC.trash, title: 'Excluir ' + tu.name.split(' ')[0] + '?', body: 'O cliente, saldo de ' + brl(tu.balance) + ' e todo o histórico serão removidos do banco.', label: 'Excluir cliente', hasGo: true, cancel: 'Cancelar',
              go: () => this.removerCliente(tu) })} />
          </View>
        )}
      </Anim>
    );
  }

  sheetStockForm(sh) {
    const s = this.state, fv = s.form, cur = (EXM[fv.ex || 'B3'] || EX[0]).cur;
    const rows = [0, 4, 8].map(i => EX.slice(i, i + 4));
    return (
      <Anim type="fadeUp" duration={350} style={{ gap: 12 }}>
        <SheetTitle kicker="Listagem · backoffice" title={sh.edit ? 'Editar ' + fv.ticker : 'Cadastrar ação'} />
        <View style={{ flexDirection: 'row', gap: 12 }}>
          <Field label="Ticker" style={{ flex: 1 }}><Input value={fv.ticker || ''} onChangeText={this.fh.ticker} placeholder="ORBT3" autoCapitalize="characters" editable={!sh.edit} style={{ fontFamily: 'BarlowCondensed_600SemiBold', fontSize: 16, letterSpacing: 0.6 }} /></Field>
          <Field label={'Preço (' + cur + ')'} style={{ flex: 1 }}><Input value={fv.price || ''} onChangeText={this.fh.price} placeholder="10,00" keyboardType="decimal-pad" /></Field>
        </View>
        <Field label="Empresa"><Input value={fv.sname || ''} onChangeText={this.fh.sname} placeholder="Órbita Holding S.A." /></Field>
        <Field label="Setor"><Input value={fv.sector || ''} onChangeText={this.fh.sector} placeholder="Financeiro" /></Field>
        <Field label="Bolsa · aparece no globo">
          <View style={{ gap: 1, backgroundColor: C.divider, borderWidth: 1, borderColor: C.divider, borderRadius: 12, overflow: 'hidden' }}>
            {rows.map((r, i) => (
              <View key={i} style={{ flexDirection: 'row', gap: 1 }}>
                {r.map(e => {
                  const on = (fv.ex || 'B3') === e.code;
                  return (
                    <Pressable key={e.code} onPress={() => this.setState(st => ({ form: { ...st.form, ex: e.code } }))} style={{ flex: 1, paddingVertical: 9, alignItems: 'center', backgroundColor: on ? C.accent : C.bg }}>
                      <T h style={{ fontSize: 14, color: on ? C.bg : C.text }}>{e.code}</T>
                    </Pressable>
                  );
                })}
              </View>
            ))}
          </View>
        </Field>
        <ErrBox msg={s.err} />
        <Cta label={sh.edit ? 'Salvar ação' : 'Listar na bolsa'} icon={IC.check} onPress={this.saveStock} style={{ marginTop: 4 }} />
        {sh.edit && <Plain icon={IC.trash} label="Remover da bolsa" onPress={() => {
          const t = fv.ticker;
          this.confirm({ d: IC.trash, title: 'Remover ' + t + '?', body: 'A ação sai do mercado e do globo. Posições de clientes deixam de ser exibidas.', label: 'Remover ação', hasGo: true, cancel: 'Cancelar',
            go: () => this.removerAtivo(t) });
        }} />}
      </Anim>
    );
  }

  sheetChangePin() {
    const s = this.state, fv = s.form;
    return (
      <Anim type="fadeUp" duration={350} style={{ gap: 12 }}>
        <SheetTitle kicker="Segurança" title="Alterar PIN" />
        <Field label="PIN atual"><Input pin value={fv.pinOld || ''} onChangeText={this.fh.pinOld} placeholder="••••" /></Field>
        <View style={{ flexDirection: 'row', gap: 12 }}>
          <Field label="Novo PIN" style={{ flex: 1 }}><Input pin value={fv.pin || ''} onChangeText={this.fh.pin} placeholder="••••" /></Field>
          <Field label="Confirmar" style={{ flex: 1 }}><Input pin value={fv.pin2 || ''} onChangeText={this.fh.pin2} placeholder="••••" /></Field>
        </View>
        <ErrBox msg={s.err} />
        <Cta label="Salvar novo PIN" icon={IC.check} onPress={this.savePin} style={{ marginTop: 4 }} />
      </Anim>
    );
  }

  sheetConfirm(cf) {
    return (
      <Anim type="fadeUp" duration={350} style={{ gap: 14 }}>
        <BigIcon d={cf.d || IC.alert} size={64} icon={28} />
        <View>
          <T h style={{ fontSize: 30, lineHeight: 33, marginBottom: 6 }}>{cf.title}</T>
          <T style={{ fontSize: 15, color: C.n800 }}>{cf.body}</T>
        </View>
        {!!this.state.err && <ErrBox icon msg={this.state.err} />}
        {cf.hasGo && <Cta label={cf.label} onPress={cf.go} />}
        <Plain label={cf.cancel || 'Cancelar'} onPress={this.close} style={{ minHeight: 48, paddingHorizontal: 18 }} />
      </Anim>
    );
  }

  renderSheet(d) {
    const s = this.state, sh = s.sheet, u = d.u;
    let body = null;
    switch (sh.kind) {
      case 'amount': body = this.sheetAmount(sh, u); break;
      case 'pin': body = this.sheetPin(sh, u); break;
      case 'processing': body = this.sheetProcessing(sh); break;
      case 'success': body = this.sheetSuccess(sh); break;
      case 'locked': body = this.sheetLocked(u); break;
      case 'stock': body = u ? this.sheetStock(sh, d) : null; break;
      case 'userForm': body = this.sheetUser(sh); break;
      case 'stockForm': body = this.sheetStockForm(sh); break;
      case 'changePin': body = this.sheetChangePin(); break;
      case 'confirm': body = this.sheetConfirm(sh); break;
    }
    return (
      <Sheet closing={s.closing} onClose={this.close} bottom={this.props.insets.bottom}>
        <Shake n={s.shake}><View key={sh.kind + (sh.purpose || '')}>{body}</View></Shake>
      </Sheet>
    );
  }

  render() {
    const s = this.state, { insets } = this.props, d = this.derive(), u = d.u;
    const isApp = s.screen === 'app' && !!u;
    const tabs = { home: this.renderHome, market: this.renderMarket, globe: this.renderGlobe, bank: this.renderBank, profile: this.renderProfile };
    return (
      <View style={{ flex: 1, backgroundColor: C.bg }}>
        <StatusBar style={s.screen === 'splash' ? 'light' : 'dark'} />
        {(s.screen === 'login' || s.screen === 'signup') && (
          <KeyboardAvoidingView behavior="padding" style={{ flex: 1, paddingTop: insets.top }}>
            {s.screen === 'login' ? this.renderLogin() : this.renderSignup()}
          </KeyboardAvoidingView>
        )}
        {isApp && (
          <View style={{ flex: 1, paddingTop: insets.top }}>
            <GestureScrollView ref={this.scrollRef} keyboardShouldPersistTaps="handled" contentContainerStyle={{ paddingBottom: 104 + insets.bottom }}>
              <Anim key={s.tab} type="fadeUp">{tabs[s.tab].call(this, d)}</Anim>
            </GestureScrollView>
            <TabBar tab={s.tab} onGo={this.go} bottom={insets.bottom} />
          </View>
        )}
        {s.sheet && this.renderSheet(d)}
        {s.toast && (
          <Anim key={s.toast.id} type="drop" style={{ position: 'absolute', top: insets.top + 6, left: 16, right: 16, zIndex: 45, flexDirection: 'row', alignItems: 'center', gap: 10, paddingVertical: 12, paddingHorizontal: 14, backgroundColor: C.a900, elevation: 8 }}>
            <Icon d={IC.check} size={18} color={C.bg} />
            <T style={{ fontSize: 14, color: C.bg, flex: 1 }}>{s.toast.msg}</T>
          </Anim>
        )}
        {s.screen === 'splash' && <Splash />}
      </View>
    );
  }
}

function Root() {
  const insets = useSafeAreaInsets();
  const [loaded] = useFonts({ Barlow_400Regular, Barlow_500Medium, BarlowCondensed_600SemiBold });
  if (!loaded) return <View style={{ flex: 1, backgroundColor: C.a900 }} />;
  return <Orbita insets={insets} />;
}

export default function App() {
  return <GestureHandlerRootView style={{ flex: 1 }}><SafeAreaProvider><Root /></SafeAreaProvider></GestureHandlerRootView>;
}
