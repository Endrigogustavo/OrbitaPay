import { Pressable, View } from 'react-native';
import Svg, { Line, Polygon, Polyline, Rect } from 'react-native-svg';
import type { FolhaDo } from '@/@types/orbita';
import { EXM } from '@/constants/bolsas';
import { brl, money, pts } from '@/constants/formatacao';
import { IC } from '@/constants/icones';
import { C } from '@/constants/tema';
import { useMercado } from '@/context/MercadoContext';
import { useOperacoes } from '@/context/OperacoesContext';
import { usePainel } from '@/context/usePainel';
import { Anim, Blueprint, Cta, ErrBox, Icon, InfoGrid, Kicker, PulseDot, Seg, T } from '../ui';

export function FolhaDoAtivo({ folha }: { folha: FolhaDo<'stock'> }) {
  const { porTicker } = useMercado();
  const { usuario, posicoes, exibir } = usePainel();
  const op = useOperacoes();
  const st = porTicker[folha.ticker];
  if (!st || !usuario) return null;

  const b = exibir(st), e = EXM[st.ex], P = pts(st.hist, 316, 120, 8), last = P[P.length - 1];
  const h = posicoes[st.ticker], held = h ? h.qty - h.reserved : 0, buy = folha.mode === 'buy';
  const line = P.map(p => p.join(',')).join(' ');
  const botoes: [string, () => void][] = [[IC.minus, () => op.alterarQuantidade(-1)], [IC.plus, () => op.alterarQuantidade(1)]];

  return (
    <Anim type="fadeUp" duration={350} style={{ gap: 14 }}>
      <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12 }}>
        <View style={{ flex: 1 }}>
          <Kicker>{e?.full ?? st.ex} · {st.sector}</Kicker>
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
        { label: 'Comprar', on: buy, pick: () => op.definirModo('buy') },
        { label: 'Vender · ' + held + ' un.', on: !buy, pick: () => op.definirModo('sell') },
      ]} />
      <View style={{ flexDirection: 'row', alignItems: 'center', borderWidth: 1, borderColor: C.divider, borderRadius: 14, overflow: 'hidden' }}>
        <Pressable onPress={botoes[0][1]} style={({ pressed }) => ({ width: 54, height: 54, alignItems: 'center', justifyContent: 'center', backgroundColor: pressed ? C.a200 : 'transparent', borderColor: C.divider, borderRightWidth: 1 })}>
          <Icon d={botoes[0][0]} size={20} />
        </Pressable>
        <View style={{ flex: 1, alignItems: 'center' }}>
          <T h num style={{ fontSize: 28, lineHeight: 30 }}>{op.quantidade}</T>
          <T style={{ fontSize: 11, color: C.n700 }}>ações</T>
        </View>
        <Pressable onPress={botoes[1][1]} style={({ pressed }) => ({ width: 54, height: 54, alignItems: 'center', justifyContent: 'center', backgroundColor: pressed ? C.a200 : 'transparent', borderColor: C.divider, borderLeftWidth: 1 })}>
          <Icon d={botoes[1][0]} size={20} />
        </Pressable>
      </View>
      <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
        <View>
          <T style={{ fontSize: 12, color: C.n700 }}>Total estimado</T>
          <T style={{ fontSize: 12, color: C.n700 }}>{st.cur === 'BRL' ? 'Liquidação em D+2' : 'Câmbio 1 ' + st.cur + ' = ' + brl(st.fx)}</T>
        </View>
        <T h num style={{ fontSize: 28 }}>{brl(op.quantidade * st.price * st.fx)}</T>
      </View>
      <ErrBox icon msg={op.erro} />
      <Cta label={(buy ? 'Comprar ' : 'Vender ') + op.quantidade + ' ' + st.ticker} icon={buy ? IC.up : IC.down} h={54} onPress={op.confirmarOrdem} />
      <T style={{ fontSize: 12, color: C.n700 }}>Saldo disponível {brl(usuario.balance)} · ordem confirmada com PIN</T>
    </Anim>
  );
}
