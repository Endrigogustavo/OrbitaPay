import { useEffect, useRef, useState, type ComponentProps } from 'react';
import { Animated, Pressable, View } from 'react-native';
import type { Tabs } from 'expo-router';
import { ABAS } from '@/constants/abas';
import { IC } from '@/constants/icones';
import { C } from '@/constants/tema';
import { Anim, Icon, OUT, T } from './ui';

export type PropsDaBarraDeAbas = Parameters<NonNullable<ComponentProps<typeof Tabs>['tabBar']>>[0];

/** Barra inferior das abas, com indicador deslizante sobre a aba ativa. */
export function BarraDeAbas({ state, navigation, insets }: PropsDaBarraDeAbas) {
  const x = useRef(new Animated.Value(0)).current;
  const [w, setW] = useState(0);
  const idx = state.index, n = state.routes.length, bottom = insets.bottom;
  useEffect(() => { Animated.timing(x, { toValue: idx, duration: 400, easing: OUT, useNativeDriver: true }).start(); }, [idx]);
  return (
    <View onLayout={e => setW(e.nativeEvent.layout.width)} style={{ height: 64 + bottom, paddingBottom: bottom, backgroundColor: C.bg, borderTopWidth: 1, borderTopColor: C.divider, flexDirection: 'row' }}>
      <Animated.View style={{ position: 'absolute', top: -1, left: 0, width: w / n, height: 2, backgroundColor: C.accent,
        transform: [{ translateX: x.interpolate({ inputRange: [0, n - 1], outputRange: [0, (w * (n - 1)) / n] }) }] }} />
      {state.routes.map((route, i) => {
        const aba = ABAS.find(a => a.rota === route.name);
        if (!aba) return null;
        const on = i === idx, col = on ? C.a700 : C.n600;
        const irPara = () => {
          const evento = navigation.emit({ type: 'tabPress', target: route.key, canPreventDefault: true });
          if (!on && !evento.defaultPrevented) navigation.navigate(route.name, route.params);
        };
        return (
          <Pressable key={route.key} onPress={irPara} accessibilityRole="tab" accessibilityState={{ selected: on }} style={{ flex: 1, alignItems: 'center', justifyContent: 'center', gap: 4 }}>
            <Anim key={on ? 'on' : 'off'} type={on ? 'pop' : 'none'} duration={450}><Icon d={IC[aba.icone]} size={24} color={col} /></Anim>
            <T m style={{ fontSize: 11, letterSpacing: 0.2, color: col }}>{aba.rotulo}</T>
          </Pressable>
        );
      })}
    </View>
  );
}
