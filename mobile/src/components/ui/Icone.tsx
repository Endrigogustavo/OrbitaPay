import type { StyleProp, ViewStyle } from 'react-native';
import Svg, { Path } from 'react-native-svg';
import { C } from '@/constants/tema';

interface IconProps {
  /** Traçado SVG de constants/icones. */
  d: string;
  size?: number;
  color?: string;
  sw?: number;
  style?: StyleProp<ViewStyle>;
}

export function Icon({ d, size = 24, color = C.text, sw = 1.5, style }: IconProps) {
  return (
    <Svg viewBox="0 0 24 24" width={size} height={size} style={style}>
      <Path d={d} fill="none" stroke={color} strokeWidth={sw} strokeLinecap="round" strokeLinejoin="round" />
    </Svg>
  );
}
