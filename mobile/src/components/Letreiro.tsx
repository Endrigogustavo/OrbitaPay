import { useEffect, useRef, useState } from 'react';
import { Animated, Easing, Pressable, ScrollView, View } from 'react-native';
import type { AcaoExibida } from '@/@types/orbita';
import { C } from '@/constants/tema';
import { T } from './ui';

/** Letreiro de cotações rolando continuamente. Recebe a lista duplicada para o laço não ter emenda. */
export function Letreiro({ items }: { items: AcaoExibida[] }) {
  const v = useRef(new Animated.Value(0)).current;
  const [w, setW] = useState(0);
  useEffect(() => {
    if (!w) return;
    const l = Animated.loop(Animated.timing(v, { toValue: 1, duration: 46000, easing: Easing.linear, useNativeDriver: true }));
    l.start();
    return () => l.stop();
  }, [w]);
  return (
    <View style={{ marginHorizontal: -20, borderTopWidth: 1, borderBottomWidth: 1, borderColor: C.divider, backgroundColor: C.surface }}>
      <ScrollView horizontal scrollEnabled={false} showsHorizontalScrollIndicator={false}>
        <Animated.View onLayout={e => { if (!w) setW(e.nativeEvent.layout.width); }}
          style={{ flexDirection: 'row', transform: [{ translateX: v.interpolate({ inputRange: [0, 1], outputRange: [0, -w / 2 || 0] }) }] }}>
          {items.map((t, i) => (
            <Pressable key={i} onPress={t.on} style={{ flexDirection: 'row', alignItems: 'baseline', gap: 6, paddingVertical: 9, paddingHorizontal: 16, borderRightWidth: 1, borderRightColor: C.divider }}>
              <T h style={{ fontSize: 14 }}>{t.ticker}</T>
              <T num style={{ fontSize: 13 }}>{t.priceStr}</T>
              <T num style={{ fontSize: 12, color: t.chgColor }}>{t.arrow} {t.chgStr}</T>
            </Pressable>
          ))}
        </Animated.View>
      </ScrollView>
    </View>
  );
}
