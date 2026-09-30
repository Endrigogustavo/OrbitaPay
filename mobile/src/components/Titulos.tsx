import type { ReactNode } from 'react';
import { View } from 'react-native';
import { C } from '@/constants/tema';
import { Blueprint, Icon, Kicker, PulseDot, T, type TipoDeAnimacao } from './ui';

interface CabecalhoProps {
  kicker: string;
  title: string;
  size?: number;
  right?: ReactNode;
  /** Ponto pulsante de "ao vivo" antes do kicker. */
  dot?: boolean;
}

/** Cabeçalho das abas. */
export const Cabecalho = ({ kicker, title, size = 36, right, dot }: CabecalhoProps) => (
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

interface TituloDaFolhaProps {
  kicker?: string;
  title: string;
  sub?: string;
  size?: number;
}

export const TituloDaFolha = ({ kicker, title, sub, size = 30 }: TituloDaFolhaProps) => (
  <View>
    {!!kicker && <Kicker>{kicker}</Kicker>}
    <T h style={{ fontSize: size, lineHeight: size * 1.12, marginTop: 2 }}>{title}</T>
    {!!sub && <T style={{ fontSize: 14, color: C.n800 }}>{sub}</T>}
  </View>
);

interface IconeGrandeProps {
  d: string;
  size?: number;
  icon?: number;
  bg?: string;
  color?: string;
  sw?: number;
  anim?: TipoDeAnimacao;
}

/** Ícone destacado dentro de uma moldura, no topo das folhas de resultado. */
export const IconeGrande = ({ d, size = 96, icon = 48, bg, color = C.a800, sw = 1.5, anim = 'frame' }: IconeGrandeProps) => (
  <Blueprint anim={anim} style={{ width: size, height: size, alignItems: 'center', justifyContent: 'center', backgroundColor: bg }}>
    <Icon d={d} size={icon} color={color} sw={sw} />
  </Blueprint>
);
