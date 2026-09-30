import type { ReactNode } from 'react';
import { View, type StyleProp, type ViewStyle } from 'react-native';
import { C } from '@/constants/tema';
import { Anim, type TipoDeAnimacao } from './Animacoes';

function Corner({ pos }: { pos: 'tl' | 'tr' | 'bl' | 'br' }) {
  const s: ViewStyle = { position: 'absolute', width: 11, height: 11 };
  if (pos[0] === 't') s.top = -6; else s.bottom = -6;
  if (pos[1] === 'l') s.left = -6; else s.right = -6;
  return (
    <View pointerEvents="none" style={s}>
      <View style={{ position: 'absolute', left: 5, top: 0, width: 1, height: 11, backgroundColor: C.corner }} />
      <View style={{ position: 'absolute', top: 5, left: 0, height: 1, width: 11, backgroundColor: C.corner }} />
    </View>
  );
}

interface BlueprintProps {
  style?: StyleProp<ViewStyle>;
  children?: ReactNode;
  anim?: TipoDeAnimacao;
  delay?: number;
}

/** Moldura com marcas de canto, no estilo de planta técnica. */
export function Blueprint({ style, children, anim, delay }: BlueprintProps) {
  const moldura = [{ borderWidth: 1, borderColor: C.divider }, style];
  const cantos = <><Corner pos="tl" /><Corner pos="tr" /><Corner pos="bl" /><Corner pos="br" /></>;
  if (anim) return <Anim type={anim} delay={delay} style={moldura}>{children}{cantos}</Anim>;
  return <View style={moldura}>{children}{cantos}</View>;
}
