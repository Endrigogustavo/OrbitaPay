import type { ReactNode } from 'react';
import { Pressable, type StyleProp, type ViewStyle } from 'react-native';
import { IC } from '@/constants/icones';
import { C } from '@/constants/tema';
import { Icon } from './Icone';
import { T } from './Texto';

type TipoDeBotao = 'primary' | 'secondary' | 'ghost';

interface BtnProps {
  kind?: TipoDeBotao;
  onPress?: () => void;
  style?: StyleProp<ViewStyle>;
  children?: ReactNode;
  icon?: string;
  iconColor?: string;
  disabled?: boolean;
}

export function Btn({ kind = 'secondary', onPress, style, children, icon, iconColor, disabled }: BtnProps) {
  const primary = kind === 'primary', ghost = kind === 'ghost';
  const fg = primary ? C.bg : ghost ? C.a700 : C.text;
  const content = typeof children === 'string' || typeof children === 'number'
    ? <T h style={{ fontSize: 14, color: fg }}>{children}</T> : children;
  return (
    <Pressable disabled={disabled} onPress={onPress} style={({ pressed }) => [{
      flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 6,
      paddingVertical: 7, paddingHorizontal: 12, borderRadius: 14, borderWidth: 1,
      borderColor: primary ? C.accent : ghost ? 'transparent' : C.divider,
      backgroundColor: primary ? (pressed ? C.a700 : C.accent) : pressed ? (ghost ? 'rgba(89,128,166,0.18)' : 'rgba(29,31,32,0.1)') : 'transparent',
      opacity: disabled ? 0.45 : 1,
    }, style]}>
      {icon && <Icon d={icon} size={16} color={iconColor || fg} />}
      {content}
    </Pressable>
  );
}

interface CtaProps {
  label: string;
  icon?: string | null;
  onPress?: () => void;
  kind?: TipoDeBotao;
  h?: number;
  size?: number;
  style?: StyleProp<ViewStyle>;
}

export function Cta({ label, icon = IC.arrowRight, onPress, kind = 'primary', h = 52, size = 17, style }: CtaProps) {
  const fg = kind === 'primary' ? C.bg : C.text;
  return (
    <Btn kind={kind} onPress={onPress} style={[{ minHeight: h, justifyContent: 'space-between', paddingHorizontal: 18 }, style]}>
      <T h style={{ fontSize: size, color: fg }}>{label}</T>
      {icon && <Icon d={icon} size={20} color={fg} />}
    </Btn>
  );
}

interface PlainProps {
  label: string;
  icon?: string;
  onPress?: () => void;
  style?: StyleProp<ViewStyle>;
}

export const Plain = ({ label, icon, onPress, style }: PlainProps) => (
  <Btn onPress={onPress} icon={icon} style={[{ minHeight: 46, justifyContent: 'flex-start', paddingHorizontal: 14, gap: 8 }, style]}>
    <T h style={{ fontSize: 16 }}>{label}</T>
  </Btn>
);
