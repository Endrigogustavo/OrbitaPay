import { View } from 'react-native';
import { C } from '@/constants/tema';
import { Spin } from './Animacoes';

interface LogoProps {
  size?: number;
  core?: number;
  sat?: number;
  color?: string;
  duration?: number;
}

export function Logo({ size = 26, core = 8, sat = 5, color = C.a800, duration = 3000 }: LogoProps) {
  const out = Math.round(sat * 0.6);
  return (
    <View style={{ width: size, height: size }}>
      <View style={{ position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, borderWidth: 1, borderColor: color, borderRadius: size }} />
      <View style={{ position: 'absolute', top: core, left: core, right: core, bottom: core, backgroundColor: color, borderRadius: size }} />
      <Spin duration={duration} style={{ position: 'absolute', top: -out, left: -out, right: -out, bottom: -out }}>
        <View style={{ position: 'absolute', top: 0, left: '50%', width: sat, height: sat, marginLeft: -sat / 2, backgroundColor: color }} />
      </Spin>
    </View>
  );
}
