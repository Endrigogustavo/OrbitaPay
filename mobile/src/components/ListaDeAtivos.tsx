import { Pressable, View } from 'react-native';
import Svg, { Polyline } from 'react-native-svg';
import type { AcaoExibida } from '@/@types/orbita';
import { IC } from '@/constants/icones';
import { C } from '@/constants/tema';
import { Anim, Icon, Input, Row, T } from './ui';

export const Spark = ({ points, color }: { points: string; color: string }) => (
  <Svg width={64} height={26} viewBox="0 0 64 26"><Polyline points={points} fill="none" stroke={color} strokeWidth={1.3} strokeLinejoin="round" /></Svg>
);

interface LinhaDeAtivoProps {
  acao: AcaoExibida;
  indice: number;
  compacta?: boolean;
}

export function LinhaDeAtivo({ acao: x, indice, compacta }: LinhaDeAtivoProps) {
  return (
    <Anim type="fadeUp" delay={compacta ? 0 : Math.min(indice, 12) * 30} duration={350}>
      <Row onPress={x.on} bg={x.flashBg} style={[{ flexDirection: 'row', alignItems: 'center', gap: 10, paddingVertical: compacta ? 10 : 12, paddingHorizontal: compacta ? 0 : 6, borderColor: C.divider },
        compacta ? { borderTopWidth: 1 } : { borderBottomWidth: 1 }]}>
        <View style={{ flex: 1 }}>
          <View style={{ flexDirection: 'row', alignItems: 'center', gap: 6 }}>
            <T h style={{ fontSize: compacta ? 17 : 18 }}>{x.ticker}</T>
            {!compacta && <View style={{ paddingHorizontal: 5, paddingVertical: 1, borderWidth: 1, borderColor: C.divider }}><T style={{ fontSize: 10, letterSpacing: 0.6, color: C.n700 }}>{x.exCode}</T></View>}
            {!compacta && x.held && <View style={{ width: 6, height: 6, borderRadius: 3, backgroundColor: C.accent }} />}
          </View>
          <T numberOfLines={1} style={{ fontSize: 12, color: C.n700 }}>{x.name}</T>
        </View>
        <Spark points={x.spark} color={x.sparkColor} />
        <View style={{ minWidth: 96, alignItems: 'flex-end' }}>
          <T h num style={{ fontSize: compacta ? 15 : 16 }}>{x.priceStr}</T>
          <T num style={{ fontSize: 12, color: x.chgColor }}>{x.arrow} {x.chgStr}</T>
        </View>
      </Row>
    </Anim>
  );
}

interface CampoDeBuscaProps {
  valor: string;
  aoMudar: (texto: string) => void;
  placeholder: string;
  h: 40 | 44;
}

export function CampoDeBusca({ valor, aoMudar, placeholder, h }: CampoDeBuscaProps) {
  const compacto = h === 40;
  return (
    <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8, borderWidth: 1, borderColor: C.divider, borderRadius: 12, paddingHorizontal: 12, backgroundColor: C.surface, flex: compacto ? 1 : undefined }}>
      <Icon d={IC.search} size={compacto ? 16 : 18} color={C.n600} />
      <Input value={valor} onChangeText={aoMudar} placeholder={placeholder} autoCapitalize="none"
        style={{ flex: 1, minHeight: h, borderWidth: 0, backgroundColor: 'transparent', paddingHorizontal: 0, fontSize: compacto ? 14 : 15 }} />
    </View>
  );
}

interface ChipProps {
  label: string;
  on: boolean;
  onPress: () => void;
  dot?: boolean;
}

export function Chip({ label, on, onPress, dot }: ChipProps) {
  const fg = on ? C.bg : C.text;
  return (
    <Pressable onPress={onPress} style={{ flexDirection: 'row', alignItems: 'center', gap: 7, paddingVertical: dot === undefined ? 6 : 7, paddingHorizontal: dot === undefined ? 14 : 13,
      borderRadius: 999, borderWidth: 1, borderColor: on ? C.accent : C.divider, backgroundColor: on ? C.accent : 'transparent' }}>
      {dot !== undefined && <View style={{ width: 7, height: 7, borderRadius: 4, borderWidth: 1, borderColor: fg, backgroundColor: dot ? fg : 'transparent' }} />}
      <T h style={{ fontSize: 14, color: fg }}>{label}</T>
    </Pressable>
  );
}
