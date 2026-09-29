import React, { useEffect, useRef, useState } from 'react';
import { Animated, Easing, Pressable, Text, TextInput, View, StyleSheet } from 'react-native';
import Svg, { Path, Defs, LinearGradient, Stop, Rect } from 'react-native-svg';
import { C, F, TAB } from './theme';
import { IC } from './data';

const OUT = Easing.bezier(0.2, 0.9, 0.25, 1);

export function T({ style, h, m, num, ...p }) {
  return <Text {...p} style={[{ fontFamily: h ? F.h : m ? F.m : F.b, fontSize: 15, color: C.text }, num && { fontVariant: TAB }, style]} />;
}

export const Kicker = ({ children, style, color = C.a700 }) => (
  <T style={[{ fontSize: 11, letterSpacing: 1.5, textTransform: 'uppercase', color }, style]}>{children}</T>
);

export function Icon({ d, size = 24, color = C.text, sw = 1.5, style }) {
  return (
    <Svg viewBox="0 0 24 24" width={size} height={size} style={style}>
      <Path d={d} fill="none" stroke={color} strokeWidth={sw} strokeLinecap="round" strokeLinejoin="round" />
    </Svg>
  );
}

function Corner({ pos }) {
  const s = { position: 'absolute', width: 11, height: 11 };
  if (pos[0] === 't') s.top = -6; else s.bottom = -6;
  if (pos[1] === 'l') s.left = -6; else s.right = -6;
  return (
    <View pointerEvents="none" style={s}>
      <View style={{ position: 'absolute', left: 5, top: 0, width: 1, height: 11, backgroundColor: C.corner }} />
      <View style={{ position: 'absolute', top: 5, left: 0, height: 1, width: 11, backgroundColor: C.corner }} />
    </View>
  );
}

export function Blueprint({ style, children, anim, delay }) {
  const Wrap = anim ? Anim : View;
  return (
    <Wrap type={anim} delay={delay} style={[{ borderWidth: 1, borderColor: C.divider }, style]}>
      {children}
      <Corner pos="tl" /><Corner pos="tr" /><Corner pos="bl" /><Corner pos="br" />
    </Wrap>
  );
}

const FROM = {
  fadeUp: { o: 0, y: 14 }, rise: { o: 0, y: 26 }, fadeIn: { o: 0 }, drop: { o: 0, y: -18 },
  frame: { o: 0, s: 1.3 }, pop: { s: 0.6 }, letter: { o: 0, y: 32 }, none: {},
};

export function Anim({ type = 'fadeUp', delay = 0, duration, style, children, ...p }) {
  const v = useRef(new Animated.Value(0)).current;
  useEffect(() => {
    const dur = duration || (type === 'pop' ? 450 : type === 'frame' ? 550 : 450);
    Animated.timing(v, { toValue: 1, duration: dur, delay, easing: type === 'pop' ? Easing.linear : OUT, useNativeDriver: true }).start();
  }, []);
  const f = FROM[type] || FROM.fadeUp, tr = [];
  if (f.y != null) tr.push({ translateY: v.interpolate({ inputRange: [0, 1], outputRange: [f.y, 0] }) });
  if (type === 'pop') tr.push({ scale: v.interpolate({ inputRange: [0, 0.6, 1], outputRange: [0.6, 1.14, 1] }) });
  else if (f.s != null) tr.push({ scale: v.interpolate({ inputRange: [0, 1], outputRange: [f.s, 1] }) });
  const a = { transform: tr };
  if (f.o != null) a.opacity = v;
  return <Animated.View {...p} style={[style, a]}>{children}</Animated.View>;
}

export function Spin({ duration = 3000, reverse, style, children, easing = Easing.linear }) {
  const v = useRef(new Animated.Value(0)).current;
  useEffect(() => {
    const l = Animated.loop(Animated.timing(v, { toValue: 1, duration, easing, useNativeDriver: true }));
    l.start();
    return () => l.stop();
  }, []);
  const rotate = v.interpolate({ inputRange: [0, 1], outputRange: reverse ? ['360deg', '0deg'] : ['0deg', '360deg'] });
  return <Animated.View pointerEvents="none" style={[style, { transform: [{ rotate }] }]}>{children}</Animated.View>;
}

export function Logo({ size = 26, core = 8, sat = 5, color = C.a800, duration = 3000 }) {
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

export function PulseDot({ size = 6, color = C.accent, duration = 1600, active = true, style }) {
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

export function Shake({ n, style, children }) {
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

export function Btn({ kind = 'secondary', onPress, style, children, icon, iconColor, disabled }) {
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

export function Cta({ label, icon = IC.arrowRight, onPress, kind = 'primary', h = 52, size = 17, style }) {
  const fg = kind === 'primary' ? C.bg : C.text;
  return (
    <Btn kind={kind} onPress={onPress} style={[{ minHeight: h, justifyContent: 'space-between', paddingHorizontal: 18 }, style]}>
      <T h style={{ fontSize: size, color: fg }}>{label}</T>
      {icon && <Icon d={icon} size={20} color={fg} />}
    </Btn>
  );
}

export const Plain = ({ label, icon, onPress, style }) => (
  <Btn onPress={onPress} icon={icon} style={[{ minHeight: 46, justifyContent: 'flex-start', paddingHorizontal: 14, gap: 8 }, style]}>
    <T h style={{ fontSize: 16 }}>{label}</T>
  </Btn>
);

export function Tag({ kind = 'accent', children, style, dot, pulse }) {
  const bg = kind === 'accent' ? C.a100 : C.n100, fg = kind === 'accent' ? C.a800 : C.n800;
  return (
    <View style={[{ flexDirection: 'row', alignItems: 'center', gap: 6, alignSelf: 'flex-start', paddingVertical: 3, paddingHorizontal: 10, backgroundColor: bg }, style]}>
      {dot && <PulseDot size={6} color={fg} active={!!pulse} />}
      <T style={{ fontSize: 11, color: fg, letterSpacing: 0.2 }}>{children}</T>
    </View>
  );
}

export function Field({ label, children, style }) {
  return (
    <View style={style}>
      <T style={{ fontSize: 12, marginBottom: 5, color: C.label }}>{label}</T>
      {children}
    </View>
  );
}

export function Input({ style, pin, ...p }) {
  return (
    <TextInput
      placeholderTextColor={C.n500}
      selectionColor={C.accent}
      cursorColor={C.accent}
      autoCorrect={false}
      {...(pin ? { secureTextEntry: true, keyboardType: 'number-pad', maxLength: 4 } : null)}
      {...p}
      style={[{ minHeight: 44, paddingVertical: 6, paddingHorizontal: 10, fontFamily: F.b, fontSize: 15, color: C.text,
        backgroundColor: C.surface, borderWidth: 1, borderColor: C.divider, borderRadius: 12 }, pin && { fontSize: 18, letterSpacing: 5 }, style, p.editable === false && { opacity: 0.6 }]}
    />
  );
}

export function Seg({ options, style }) {
  return (
    <View style={[{ flexDirection: 'row', borderWidth: 1, borderColor: C.divider, borderRadius: 12, overflow: 'hidden' }, style]}>
      {options.map((o, i) => (
        <Pressable key={o.label} onPress={o.pick} style={({ pressed }) => [{ flex: 1, alignItems: 'center', paddingVertical: 9, paddingHorizontal: 12,
          backgroundColor: o.on ? C.accent : pressed ? 'rgba(29,31,32,0.07)' : 'transparent' }, i > 0 && { borderLeftWidth: 1, borderLeftColor: C.divider }]}>
          <T style={{ fontSize: 13, color: o.on ? C.bg : C.text }}>{o.label}</T>
        </Pressable>
      ))}
    </View>
  );
}

export function ErrBox({ msg, icon, soft }) {
  if (!msg) return null;
  return (
    <Anim type="fadeUp" duration={250} style={{ flexDirection: 'row', gap: 8, alignItems: 'center', backgroundColor: C.a100, borderRadius: 12, paddingVertical: 9, paddingHorizontal: 10,
      borderWidth: soft ? 0 : 1, borderColor: C.a300 }}>
      {icon && <Icon d={IC.alert} size={16} color={soft ? C.a800 : C.a900} />}
      <T style={{ flex: 1, fontSize: 13, color: soft ? C.a800 : C.a900 }}>{msg}</T>
    </Anim>
  );
}

export function InfoGrid({ items, cols = 2, style, pad = 10 }) {
  const rows = [];
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

export function Avatar({ text, size = 42, font = 16, bg = C.a100, border = true, style }) {
  return (
    <View style={[{ width: size, height: size, alignItems: 'center', justifyContent: 'center', backgroundColor: bg }, border && { borderWidth: 1, borderColor: C.divider }, style]}>
      <T h style={{ fontSize: font, color: C.a800 }}>{text}</T>
    </View>
  );
}

export const Row = ({ onPress, style, children, bg }) => (
  <Pressable onPress={onPress} style={({ pressed }) => [{ backgroundColor: pressed ? C.a100 : bg || 'transparent' }, style]}>{children}</Pressable>
);
