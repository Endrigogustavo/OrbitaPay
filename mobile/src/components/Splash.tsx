import { useEffect, useRef } from 'react';
import { Animated, Easing, StyleSheet, View } from 'react-native';
import Svg, { Circle } from 'react-native-svg';
import { C } from '@/constants/tema';
import { Anim, Kicker, Spin, T } from './ui';

const ABS = StyleSheet.absoluteFill;

/** Abertura animada, exibida enquanto a sessão salva é validada no gateway. */
export function Splash() {
  const grow = useRef(new Animated.Value(0)).current;
  useEffect(() => { Animated.timing(grow, { toValue: 1, duration: 1900, easing: Easing.bezier(0.6, 0, 0.2, 1), useNativeDriver: true }).start(); }, []);
  return (
    <Anim type="fadeIn" duration={300} style={[ABS, { zIndex: 40, backgroundColor: C.a900, justifyContent: 'space-between', paddingTop: 120, paddingHorizontal: 34, paddingBottom: 64 }]}>
      <View style={{ width: 170, height: 170 }}>
        <Spin duration={18000} style={ABS}>
          <Svg width={170} height={170}><Circle cx={85} cy={85} r={84.5} fill="none" stroke={C.a300} strokeWidth={1} strokeDasharray="3 3" /></Svg>
        </Spin>
        <Anim type="frame" duration={800} style={{ position: 'absolute', top: 28, left: 28, right: 28, bottom: 28, borderWidth: 1, borderColor: C.a300, borderRadius: 200 }} />
        <Anim type="pop" delay={200} duration={700} style={{ position: 'absolute', top: 62, left: 62, right: 62, bottom: 62, backgroundColor: C.bg, borderRadius: 200 }} />
        <Spin duration={2200} easing={Easing.bezier(0.5, 0, 0.5, 1)} style={{ position: 'absolute', top: -2, left: -2, right: -2, bottom: -2 }}>
          <View style={{ position: 'absolute', top: 0, left: '50%', width: 10, height: 10, marginLeft: -5, backgroundColor: C.bg }} />
        </Spin>
        <Spin duration={3400} reverse style={{ position: 'absolute', top: 26, left: 26, right: 26, bottom: 26 }}>
          <View style={{ position: 'absolute', bottom: 0, left: '50%', width: 6, height: 6, marginLeft: -3, backgroundColor: C.a300 }} />
        </Spin>
      </View>
      <View style={{ gap: 14 }}>
        <View style={{ flexDirection: 'row', overflow: 'hidden' }}>
          {'ÓRBITA'.split('').map((c, i) => (
            <Anim key={i} type="letter" delay={350 + i * 70} duration={600}>
              <T h style={{ fontSize: 76, lineHeight: 86, letterSpacing: 3, color: C.bg }}>{c}</T>
            </Anim>
          ))}
        </View>
        <Anim type="fadeUp" delay={800} duration={600}><T style={{ fontSize: 16, color: C.a200 }}>Banco, bolsa e o mundo em órbita.</T></Anim>
        <View style={{ height: 1, backgroundColor: 'rgba(242,242,243,0.25)', marginTop: 18 }}>
          <Animated.View style={{ height: 1, backgroundColor: C.bg, transformOrigin: 'left', transform: [{ scaleX: grow }] }} />
        </View>
        <View style={{ flexDirection: 'row', justifyContent: 'space-between' }}>
          <Kicker color={C.a300} style={{ letterSpacing: 1.5 }}>Conectando ao gateway</Kicker>
          <Kicker color={C.a300}>v2.0</Kicker>
        </View>
      </View>
    </Anim>
  );
}
