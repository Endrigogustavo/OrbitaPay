import type { ReactNode } from 'react';
import { Pressable, View, type StyleProp, type ViewStyle } from 'react-native';
import { C } from '@/constants/tema';
import { PulseDot } from './Animacoes';
import { T } from './Texto';

interface TagProps {
  kind?: 'accent' | 'neutral';
  children?: ReactNode;
  style?: StyleProp<ViewStyle>;
  dot?: boolean;
  pulse?: boolean;
}

export function Tag({ kind = 'accent', children, style, dot, pulse }: TagProps) {
  const bg = kind === 'accent' ? C.a100 : C.n100, fg = kind === 'accent' ? C.a800 : C.n800;
  return (
    <View style={[{ flexDirection: 'row', alignItems: 'center', gap: 6, alignSelf: 'flex-start', paddingVertical: 3, paddingHorizontal: 10, backgroundColor: bg }, style]}>
      {dot && <PulseDot size={6} color={fg} active={!!pulse} />}
      <T style={{ fontSize: 11, color: fg, letterSpacing: 0.2 }}>{children}</T>
    </View>
  );
}

interface InfoGridProps {
  items: [string, string][];
  cols?: number;
  style?: StyleProp<ViewStyle>;
  pad?: number;
}

export function InfoGrid({ items, cols = 2, style, pad = 10 }: InfoGridProps) {
  const rows: [string, string][][] = [];
  for (let i = 0; i < items.length; i += cols) rows.push(items.slice(i, i + cols));
  return (
    <View style={[{ gap: 1, backgroundColor: C.divider, borderWidth: 1, borderColor: C.divider }, style]}>
      {rows.map((r, i) => (
        <View key={i} style={{ flexDirection: 'row', gap: 1 }}>
          {r.map(([k, v]) => (
            <View key={k} style={{ flex: 1, backgroundColor: C.bg, paddingVertical: pad - 2, paddingHorizontal: pad + 2 }}>
              <T style={{ fontSize: 11, color: C.n700 }}>{k}</T>
              <T num style={{ fontSize: 14 }}>{v}</T>
            </View>
          ))}
        </View>
      ))}
    </View>
  );
}

interface AvatarProps {
  text: string;
  size?: number;
  font?: number;
  bg?: string;
  border?: boolean;
  style?: StyleProp<ViewStyle>;
}

export function Avatar({ text, size = 42, font = 16, bg = C.a100, border = true, style }: AvatarProps) {
  return (
    <View style={[{ width: size, height: size, alignItems: 'center', justifyContent: 'center', backgroundColor: bg }, border && { borderWidth: 1, borderColor: C.divider }, style]}>
      <T h style={{ fontSize: font, color: C.a800 }}>{text}</T>
    </View>
  );
}

interface RowProps {
  onPress?: () => void;
  style?: StyleProp<ViewStyle>;
  children?: ReactNode;
  bg?: string;
}

export const Row = ({ onPress, style, children, bg }: RowProps) => (
  <Pressable onPress={onPress} style={({ pressed }) => [{ backgroundColor: pressed ? C.a100 : bg || 'transparent' }, style]}>{children}</Pressable>
);
