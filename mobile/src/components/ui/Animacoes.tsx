import { useEffect, useRef, useState, type ComponentProps, type ReactNode } from 'react';
import { Animated, Easing, StyleSheet, View, type EasingFunction, type StyleProp, type ViewStyle } from 'react-native';
import Svg, { Defs, LinearGradient, Rect, Stop } from 'react-native-svg';
import { C } from '@/constants/tema';

export const OUT = Easing.bezier(0.2, 0.9, 0.25, 1);

const FROM = {
  fadeUp: { o: 0, y: 14 }, rise: { o: 0, y: 26 }, fadeIn: { o: 0 }, drop: { o: 0, y: -18 },
  frame: { o: 0, s: 1.3 }, pop: { s: 0.6 }, letter: { o: 0, y: 32 }, none: {},
} satisfies Record<string, { o?: number; y?: number; s?: number }>;

export type TipoDeAnimacao = keyof typeof FROM;

type AnimProps = Omit<ComponentProps<typeof Animated.View>, 'style'> & {
  type?: TipoDeAnimacao;
  delay?: number;
  duration?: number;
  style?: StyleProp<ViewStyle>;
  children?: ReactNode;
};

export function Anim({ type = 'fadeUp', delay = 0, duration, style, children, ...p }: AnimProps) {
  const v = useRef(new Animated.Value(0)).current;
  useEffect(() => {
    const dur = duration || (type === 'pop' ? 450 : type === 'frame' ? 550 : 450);
    Animated.timing(v, { toValue: 1, duration: dur, delay, easing: type === 'pop' ? Easing.linear : OUT, useNativeDriver: true }).start();
  }, []);
  const f: { o?: number; y?: number; s?: number } = FROM[type] || FROM.fadeUp;
  const transform: ({ translateY: Animated.AnimatedInterpolation<number> } | { scale: Animated.AnimatedInterpolation<number> })[] = [];
  if (f.y != null) transform.push({ translateY: v.interpolate({ inputRange: [0, 1], outputRange: [f.y, 0] }) });
  if (type === 'pop') transform.push({ scale: v.interpolate({ inputRange: [0, 0.6, 1], outputRange: [0.6, 1.14, 1] }) });
  else if (f.s != null) transform.push({ scale: v.interpolate({ inputRange: [0, 1], outputRange: [f.s, 1] }) });
  return <Animated.View {...p} style={[style, { transform }, f.o != null && { opacity: v }]}>{children}</Animated.View>;
}

interface SpinProps {
  duration?: number;
  reverse?: boolean;
  style?: StyleProp<ViewStyle>;
  children?: ReactNode;
  easing?: EasingFunction;
}

export function Spin({ duration = 3000, reverse, style, children, easing = Easing.linear }: SpinProps) {
  const v = useRef(new Animated.Value(0)).current;
  useEffect(() => {
    const l = Animated.loop(Animated.timing(v, { toValue: 1, duration, easing, useNativeDriver: true }));
    l.start();
    return () => l.stop();
  }, []);
  const rotate = v.interpolate({ inputRange: [0, 1], outputRange: reverse ? ['360deg', '0deg'] : ['0deg', '360deg'] });
  return <Animated.View pointerEvents="none" style={[style, { transform: [{ rotate }] }]}>{children}</Animated.View>;
}

interface PulseDotProps {
  size?: number;
  color?: string;
  duration?: number;
  active?: boolean;
  style?: StyleProp<ViewStyle>;
}

export function PulseDot({ size = 6, color = C.accent, duration = 1600, active = true, style }: PulseDotProps) {
  const v = useRef(new Animated.Value(0)).current;
  useEffect(() => {
    if (!active) return;
    const l = Animated.loop(Animated.timing(v, { toValue: 1, duration, easing: Easing.out(Easing.quad), useNativeDriver: true }));
    l.start();
    return () => l.stop();
  }, [active]);
  return (
    <View style={[{ width: size, height: size }, style]}>
      {active && <Animated.View style={{ position: 'absolute', top: 0, left: 0, width: size, height: size, borderRadius: size, backgroundColor: color,
        opacity: v.interpolate({ inputRange: [0, 1], outputRange: [0.7, 0] }),
        transform: [{ scale: v.interpolate({ inputRange: [0, 1], outputRange: [1, (size + 18) / size] }) }] }} />}
      <View style={{ width: size, height: size, borderRadius: size, backgroundColor: color }} />
    </View>
  );
}

export function Scan() {
  const v = useRef(new Animated.Value(0)).current;
  const [h, setH] = useState(0);
  useEffect(() => {
    if (!h) return;
    const l = Animated.loop(Animated.timing(v, { toValue: 1, duration: 4500, easing: Easing.linear, useNativeDriver: true }));
    l.start();
    return () => l.stop();
  }, [h]);
  return (
    <View pointerEvents="none" onLayout={e => setH(e.nativeEvent.layout.height)} style={[StyleSheet.absoluteFill, { overflow: 'hidden' }]}>
      <Animated.View style={{ height: 14, transform: [{ translateY: v.interpolate({ inputRange: [0, 1], outputRange: [-17, h + 14] }) }] }}>
        <Svg width="100%" height={14}>
          <Defs><LinearGradient id="sc" x1="0" y1="0" x2="0" y2="1"><Stop offset="0" stopColor={C.accent} stopOpacity={0} /><Stop offset="1" stopColor={C.accent} stopOpacity={0.18} /></LinearGradient></Defs>
          <Rect x="0" y="0" width="100%" height={14} fill="url(#sc)" />
        </Svg>
      </Animated.View>
    </View>
  );
}

interface ShakeProps {
  n: number;
  style?: StyleProp<ViewStyle>;
  children?: ReactNode;
}

export function Shake({ n, style, children }: ShakeProps) {
  const v = useRef(new Animated.Value(0)).current;
  const first = useRef(true);
  useEffect(() => {
    if (first.current) { first.current = false; return; }
    v.setValue(0);
    Animated.timing(v, { toValue: 1, duration: 450, easing: Easing.linear, useNativeDriver: true }).start();
  }, [n]);
  const translateX = v.interpolate({ inputRange: [0, 0.2, 0.4, 0.6, 0.8, 1], outputRange: [0, -10, 8, -5, 3, 0] });
  return <Animated.View style={[style, { transform: [{ translateX }] }]}>{children}</Animated.View>;
}

export function Blink() {
  const [on, setOn] = useState(true);
  useEffect(() => { const iv = setInterval(() => setOn(v => !v), 500); return () => clearInterval(iv); }, []);
  return <View style={{ width: 2, height: 44, backgroundColor: C.accent, alignSelf: 'center', opacity: on ? 1 : 0 }} />;
}
