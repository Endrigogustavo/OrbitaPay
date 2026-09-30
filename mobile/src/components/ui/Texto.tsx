import type { ReactNode } from 'react';
import { Text, type StyleProp, type TextProps, type TextStyle } from 'react-native';
import { C, F } from '@/constants/tema';

export interface TextoProps extends TextProps {
  /** Título: Barlow Condensed. */
  h?: boolean;
  /** Peso médio. */
  m?: boolean;
  /** Números tabulares, para valores alinhados. */
  num?: boolean;
}

export function T({ style, h, m, num, ...p }: TextoProps) {
  return <Text {...p} style={[{ fontFamily: h ? F.h : m ? F.m : F.b, fontSize: 15, color: C.text }, num && { fontVariant: ['tabular-nums'] }, style]} />;
}

interface KickerProps {
  children?: ReactNode;
  style?: StyleProp<TextStyle>;
  color?: string;
}

export const Kicker = ({ children, style, color = C.a700 }: KickerProps) => (
  <T style={[{ fontSize: 11, letterSpacing: 1.5, textTransform: 'uppercase', color }, style]}>{children}</T>
);
