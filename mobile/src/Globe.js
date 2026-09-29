import React, { memo, useEffect, useMemo } from 'react';
import { View, Pressable, PixelRatio } from 'react-native';
import {
  Canvas, Path, Circle, Group, Text, Rect, DashPathEffect, TwoPointConicalGradient, Shader, ImageShader,
  Skia, useFont, useImage, matchFont, vec, FilterMode, MipmapMode,
} from '@shopify/react-native-skia';
import { useSharedValue, useDerivedValue, useFrameCallback } from 'react-native-reanimated';
import { runOnJS } from 'react-native-worklets';
import { Gesture, GestureDetector } from 'react-native-gesture-handler';
import { EX, IC } from './data';
import { C } from './theme';
import { T, Icon } from './ui';

const H = 370;
const RAD = Math.PI / 180;
const TILT = -0.38 * 180 / Math.PI;
const MAX_SCALE = 2.6, MIN_SCALE = 0.8;
const TEX = [2048, 1024];
const PX = 1 / PixelRatio.get();

const rgba = hex => [parseInt(hex.slice(1, 3), 16) / 255, parseInt(hex.slice(3, 5), 16) / 255, parseInt(hex.slice(5, 7), 16) / 255, 1];
const COLORS = { cGrat: rgba(C.a300), cLand: rgba(C.a400), cCoast: rgba(C.a700) };

const EARTH = Skia.RuntimeEffect.Make(`
uniform shader land;
uniform float2 c;
uniform float R;
uniform float4 rot;
uniform float2 tex;
uniform float px;
uniform half4 cGrat;
uniform half4 cLand;
uniform half4 cCoast;

const float PI = 3.14159265;

float2 geo(float2 p) {
  float u = (p.x - c.x) / R;
  float v = (c.y - p.y) / R;
  float d = sqrt(max(0.0, 1.0 - u * u - v * v));
  float x1 = d * rot.z + v * rot.w;
  float z0 = v * rot.z - d * rot.w;
  float x0 = x1 * rot.x + u * rot.y;
  float y0 = u * rot.x - x1 * rot.y;
  return float2(atan(y0, x0), asin(clamp(z0, -1.0, 1.0)));
}

float mask(float2 g) {
  return land.eval(float2((g.x + PI) / (2.0 * PI) * tex.x, (0.5 * PI - g.y) / PI * tex.y)).r;
}

float wrapd(float a) { return a - 2.0 * PI * floor((a + PI) / (2.0 * PI)); }

half4 over(half4 dst, half4 col, float a) { return col * half(a) + dst * half(1.0 - a); }

half4 main(float2 p) {
  float cov = clamp((R - length(p - c)) / px + 0.5, 0.0, 1.0);
  if (cov <= 0.0) return half4(0);
  float2 g = geo(p);
  float2 gx = geo(p + float2(px, 0.0));
  float2 gy = geo(p + float2(0.0, px));
  half4 o = half4(0);

  float stp = PI / 18.0;
  float gl = max(length(float2(wrapd(gx.x - g.x), wrapd(gy.x - g.x))), 1e-6) / px;
  float gp = max(length(float2(gx.y - g.y, gy.y - g.y)), 1e-6) / px;
  float kl = floor(g.x / stp + 0.5);
  float mer = (mod(kl, 9.0) == 0.0 || abs(g.y) <= 80.0 * PI / 180.0)
    ? clamp((0.3 - abs(wrapd(g.x - kl * stp)) / gl) / px + 0.5, 0.0, 1.0) : 0.0;
  float kp = floor(g.y / stp + 0.5);
  float par = abs(kp) <= 8.0 ? clamp((0.3 - abs(g.y - kp * stp) / gp) / px + 0.5, 0.0, 1.0) : 0.0;
  o = over(o, cGrat, max(mer, par));

  float m = mask(g);
  float gm = max(length(float2(mask(gx) - m, mask(gy) - m)), 1e-4) / px;
  float sd = (m - 0.5) / gm;
  o = over(o, cLand, 0.55 * clamp(sd / px + 0.5, 0.0, 1.0));
  o = over(o, cCoast, clamp((0.35 - abs(sd)) / px + 0.5, 0.0, 1.0));
  return o * half(cov);
}`);

const xyz = (lon, lat) => { const l = lon * RAD, p = lat * RAD, c = Math.cos(p); return [c * Math.cos(l), c * Math.sin(l), Math.sin(p)]; };
const EXV = EX.map(e => xyz(e.lon, e.lat));
const EXF = EXV.flat();

function slerp(a, b, f) {
  const d = Math.acos(Math.max(-1, Math.min(1, a[0] * b[0] + a[1] * b[1] + a[2] * b[2])));
  if (d < 1e-6) return a;
  const s = Math.sin(d), k0 = Math.sin((1 - f) * d) / s, k1 = Math.sin(f * d) / s;
  return [a[0] * k0 + b[0] * k1, a[1] * k0 + b[1] * k1, a[2] * k0 + b[2] * k1];
}

function arcsFrom(si) {
  const a = EXV[si], p = [], l = [];
  EX.forEach((_, i) => {
    if (i === si) return;
    l.push(p.length / 3, 33);
    for (let k = 0; k <= 32; k++) p.push(...slerp(a, EXV[i], k / 32));
  });
  return { p, l };
}

function projectAll(src, g) {
  'worklet';
  const n = src.length / 3, out = new Array(n * 3);
  for (let i = 0; i < n; i++) {
    const x0 = src[i * 3], y0 = src[i * 3 + 1], z0 = src[i * 3 + 2];
    const x1 = x0 * g.cL - y0 * g.sL, y1 = x0 * g.sL + y0 * g.cL;
    out[i * 3] = y1;
    out[i * 3 + 1] = z0 * g.cP + x1 * g.sP;
    out[i * 3 + 2] = x1 * g.cP - z0 * g.sP;
  }
  return out;
}

function limb(q, a, b, g) {
  'worklet';
  const da = q[a * 3 + 2], db = q[b * 3 + 2], t = da / (da - db);
  const y = q[a * 3] + (q[b * 3] - q[a * 3]) * t, z = q[a * 3 + 1] + (q[b * 3 + 1] - q[a * 3 + 1]) * t, len = Math.hypot(y, z) || 1;
  return [g.cx + g.R * y / len, g.cy - g.R * z / len];
}

function strokeLines(path, data, g) {
  'worklet';
  const q = projectAll(data.p, g), L = data.l;
  for (let k = 0; k < L.length; k += 2) {
    const s = L[k], n = L[k + 1];
    let pen = false;
    for (let i = s; i < s + n; i++) {
      if (q[i * 3 + 2] > 0) {
        const x = g.cx + g.R * q[i * 3], y = g.cy - g.R * q[i * 3 + 1];
        if (!pen) {
          if (i > s) { const c = limb(q, i, i - 1, g); path.moveTo(c[0], c[1]); path.lineTo(x, y); } else path.moveTo(x, y);
          pen = true;
        } else path.lineTo(x, y);
      } else if (pen) {
        const c = limb(q, i - 1, i, g); path.lineTo(c[0], c[1]); pen = false;
      }
    }
  }
}

const Marker = memo(function Marker({ i, marks, sel, open, font, tw }) {
  const e = EX[i], side = e.side;
  const transform = useDerivedValue(() => [{ translateX: marks.value[i * 4] }, { translateY: marks.value[i * 4 + 1] }]);
  const opacity = useDerivedValue(() => marks.value[i * 4 + 2]);
  const pr = useDerivedValue(() => 3 + marks.value[i * 4 + 3] * (sel ? 20 : 13));
  const po = useDerivedValue(() => (1 - marks.value[i * 4 + 3]) * 0.85);
  const lx = side > 0 ? 10 : -10 - tw;
  const brackets = useMemo(() => {
    const p = Skia.Path.Make();
    [[-1, -1], [1, -1], [-1, 1], [1, 1]].forEach(([a, b]) => { const X = a * 15, Y = b * 15; p.moveTo(X - 4, Y); p.lineTo(X + 4, Y); p.moveTo(X, Y - 4); p.lineTo(X, Y + 4); });
    return p;
  }, []);
  return (
    <Group transform={transform} opacity={opacity}>
      <Circle cx={0} cy={0} r={pr} color={C.a700} style="stroke" strokeWidth={1} opacity={po} />
      {open
        ? <Rect x={-3.5} y={-3.5} width={7} height={7} color={C.a800} />
        : <Group><Rect x={-3.5} y={-3.5} width={7} height={7} color={C.bg} /><Rect x={-3.5} y={-3.5} width={7} height={7} color={C.a800} style="stroke" strokeWidth={1} /></Group>}
      <Rect x={lx - 2} y={-8} width={tw + 4} height={16} color={C.bg} opacity={0.85} />
      {font && <Text x={lx} y={4.5} text={e.code} font={font} color={sel ? C.a900 : C.text} />}
      {sel && (
        <Group>
          <Path path={brackets} color={C.a900} style="stroke" strokeWidth={1} />
          <Rect x={-15} y={-15} width={30} height={30} color={C.a700} style="stroke" strokeWidth={1}><DashPathEffect intervals={[2, 3]} /></Rect>
        </Group>
      )}
    </Group>
  );
});

function Globe({ selEx, exN, openKey, onSelect, scrollRef, autoRotate = true }) {
  const selIdx = Math.max(0, EX.findIndex(e => e.code === selEx));
  const e0 = EX[selIdx];

  const size = useSharedValue({ width: 0, height: 0 });
  const t = useSharedValue(0);
  const rotL = useSharedValue(-e0.lon + 40), rotP = useSharedValue(-12);
  const velL = useSharedValue(0), velP = useSharedValue(0);
  const scale = useSharedValue(0.6), scaleT = useSharedValue(1), pinch0 = useSharedValue(1);
  const last = useSharedValue(0), dragging = useSharedValue(false);

  const pendL = useSharedValue(0), pendP = useSharedValue(0);
  const anim = useSharedValue(null), fly = useSharedValue({ n: exN, lon: e0.lon, lat: e0.lat }), flyDone = useSharedValue(exN);
  const arcsSV = useSharedValue(arcsFrom(selIdx));
  const selSV = useSharedValue(selIdx);

  useEffect(() => { selSV.value = selIdx; arcsSV.value = arcsFrom(selIdx); }, [selIdx]);
  useEffect(() => { fly.value = { n: exN, lon: e0.lon, lat: e0.lat }; }, [exN]);

  useFrameCallback(fi => {
    const now = fi.timestamp, k = Math.min(4, (fi.timeSincePreviousFrame || 16.7) / 16.7);
    t.value = now;
    const f = fly.value;
    if (f.n !== flyDone.value) {
      flyDone.value = f.n;
      const d = ((-f.lon - rotL.value) % 360 + 540) % 360 - 180;
      anim.value = { l0: rotL.value, p0: rotP.value, l1: rotL.value + d, p1: Math.max(-60, Math.min(60, -f.lat * 0.85)), t0: now };
      velL.value = 0; velP.value = 0;
    }
    const a = anim.value;
    if (dragging.value) {
      const dl = pendL.value, dp = pendP.value;
      pendL.value = 0; pendP.value = 0;
      rotL.value += dl;
      rotP.value = Math.max(-75, Math.min(75, rotP.value + dp));
      velL.value = dl / k; velP.value = dp / k;
    } else if (a) {
      const p = Math.min(1, (now - a.t0) / 1200), e = p < 0.5 ? 4 * p * p * p : 1 - Math.pow(-2 * p + 2, 3) / 2;
      rotL.value = a.l0 + (a.l1 - a.l0) * e; rotP.value = a.p0 + (a.p1 - a.p0) * e;
      if (p >= 1) { anim.value = null; last.value = now; }
    } else {
      rotL.value += velL.value * k;
      rotP.value = Math.max(-75, Math.min(75, rotP.value + velP.value * k));
      const decay = Math.pow(0.94, k);
      velL.value *= decay; velP.value *= decay;
      if (autoRotate && now - last.value > 3000 && Math.abs(velL.value) < 0.05) rotL.value += 0.09 * k;
    }
    scale.value += (scaleT.value - scale.value) * (1 - Math.pow(0.92, k));
  });

  const geo = useDerivedValue(() => {
    const w = size.value.width, h = size.value.height || H, l = rotL.value * RAD, p = rotP.value * RAD;
    return { cx: w / 2, cy: h / 2 + 4, R: Math.min(w, h) / 2 * 0.78 * scale.value, cL: Math.cos(l), sL: Math.sin(l), cP: Math.cos(p), sP: Math.sin(p), w };
  });

  const cx = useDerivedValue(() => geo.value.cx), cy = useDerivedValue(() => geo.value.cy), R = useDerivedValue(() => geo.value.R);
  const R1 = useDerivedValue(() => geo.value.R + 1);
  const ringR = useDerivedValue(() => geo.value.R + 14);
  const ringPhase = useDerivedValue(() => t.value / 60);
  const gStart = useDerivedValue(() => vec(geo.value.cx - geo.value.R * 0.35, geo.value.cy - geo.value.R * 0.4));
  const gStartR = useDerivedValue(() => geo.value.R * 0.05);
  const gEnd = useDerivedValue(() => vec(geo.value.cx, geo.value.cy));

  const uniforms = useDerivedValue(() => {
    const g = geo.value;
    return { c: [g.cx, g.cy], R: Math.max(g.R, 1), rot: [g.cL, g.sL, g.cP, g.sP], tex: TEX, px: PX, ...COLORS };
  });

  const ticks = useDerivedValue(() => {
    const g = geo.value, major = Skia.Path.Make(), minor = Skia.Path.Make();
    for (let i = 0; i < 72; i++) {
      const a = i / 72 * Math.PI * 2, l = i % 6 === 0 ? 7 : 3, c = Math.cos(a), s = Math.sin(a), p = i % 6 === 0 ? major : minor;
      p.moveTo(g.cx + c * (g.R + 22), g.cy + s * (g.R + 22)); p.lineTo(g.cx + c * (g.R + 22 + l), g.cy + s * (g.R + 22 + l));
    }
    return [major, minor];
  });
  const ticksMajor = useDerivedValue(() => ticks.value[0]), ticksMinor = useDerivedValue(() => ticks.value[1]);

  const orbitT = useDerivedValue(() => [{ translateX: geo.value.cx }, { translateY: geo.value.cy }, { rotate: TILT * RAD }]);
  const orbitHalves = useDerivedValue(() => {
    const orb = geo.value.R + 34, ry = orb * 0.26, oval = { x: -orb, y: -ry, width: orb * 2, height: ry * 2 };
    const back = Skia.Path.Make(), front = Skia.Path.Make();
    back.addArc(oval, 180, 180); front.addArc(oval, 0, 180);
    return [back, front];
  });
  const orbitBack = useDerivedValue(() => orbitHalves.value[0]), orbitFront = useDerivedValue(() => orbitHalves.value[1]);
  const sat = useDerivedValue(() => {
    const orb = geo.value.R + 34, sa = t.value / 2600, sx = Math.cos(sa) * orb, sy = Math.sin(sa) * orb * 0.26;
    const p = Skia.Path.Make(), line = Skia.Path.Make();
    p.addRect({ x: sx - 3.5, y: sy - 3.5, width: 7, height: 7 });
    line.moveTo(sx - 9, sy); line.lineTo(sx + 9, sy);
    return { p, line, sy };
  });
  const satPath = useDerivedValue(() => sat.value.p), satLine = useDerivedValue(() => sat.value.line);
  const satBack = useDerivedValue(() => (sat.value.sy < 0 ? 1 : 0)), satFront = useDerivedValue(() => (sat.value.sy >= 0 ? 1 : 0));

  const scanPath = useDerivedValue(() => {
    const L = (((t.value / 45) % 360) - 180 - rotL.value) * RAD, pts = [];
    for (let la = -89; la <= 89; la += 4) { const pa = la * RAD, c = Math.cos(pa); pts.push(c * Math.cos(L), c * Math.sin(L), Math.sin(pa)); }
    const p = Skia.Path.Make();
    if (geo.value.w) strokeLines(p, { p: pts, l: [0, pts.length / 3] }, geo.value);
    return p;
  });
  const arcsPath = useDerivedValue(() => { const p = Skia.Path.Make(); if (geo.value.w) strokeLines(p, arcsSV.value, geo.value); return p; });
  const arcPhase = useDerivedValue(() => t.value / 35);

  const dotsPath = useDerivedValue(() => {
    const g = geo.value, p = Skia.Path.Make(), si = selSV.value, a = EXV[si];
    if (!g.w) return p;
    for (let i = 0; i < EXV.length; i++) {
      if (i === si) continue;
      const b = EXV[i], f = ((t.value / 2800) + i * 0.19) % 1;
      const d = Math.acos(Math.max(-1, Math.min(1, a[0] * b[0] + a[1] * b[1] + a[2] * b[2]))), s = Math.sin(d);
      if (s < 1e-6) continue;
      const k0 = Math.sin((1 - f) * d) / s, k1 = Math.sin(f * d) / s;
      const q = projectAll([a[0] * k0 + b[0] * k1, a[1] * k0 + b[1] * k1, a[2] * k0 + b[2] * k1], g);
      if (q[2] > 0.07) p.addRect({ x: g.cx + g.R * q[0] - 1.8, y: g.cy - g.R * q[1] - 1.8, width: 3.6, height: 3.6 });
    }
    return p;
  });

  const marks = useDerivedValue(() => {
    const g = geo.value, q = projectAll(EXF, g), out = new Array(EXV.length * 4);
    for (let i = 0; i < EXV.length; i++) {
      out[i * 4] = g.cx + g.R * q[i * 3];
      out[i * 4 + 1] = g.cy - g.R * q[i * 3 + 1];
      out[i * 4 + 2] = g.w && q[i * 3 + 2] > 0.07 ? 1 : 0;
      out[i * 4 + 3] = ((t.value / 1500) + i * 0.23) % 1;
    }
    return out;
  });

  const coords = useDerivedValue(() => {
    const lo = ((((-rotL.value) % 360) + 540) % 360) - 180, la = -rotP.value;
    return 'λ ' + (lo >= 0 ? '+' : '') + lo.toFixed(1) + '°   φ ' + (la >= 0 ? '+' : '') + la.toFixed(1) + '°   ×' + scale.value.toFixed(2);
  });

  const mask = useImage(require('../assets/land-mask.png'));
  const font = useFont(require('@expo-google-fonts/barlow-condensed/600SemiBold/BarlowCondensed_600SemiBold.ttf'), 12);

  const small = useMemo(() => matchFont({ fontFamily: 'sans-serif', fontSize: 11 }), []);
  const widths = useMemo(() => EX.map(e => (font ? font.measureText(e.code).width : e.code.length * 6.2)), [font]);

  const gesture = useMemo(() => {
    const pan = Gesture.Pan().minDistance(3)
      .onStart(() => { pendL.value = 0; pendP.value = 0; dragging.value = true; anim.value = null; velL.value = 0; velP.value = 0; })
      .onChange(e => {
        const k = 0.3 / scale.value;
        pendL.value += e.changeX * k;
        pendP.value -= e.changeY * k;
      })
      .onFinalize(() => { dragging.value = false; last.value = t.value; });
    if (scrollRef) pan.blocksExternalGesture(scrollRef);
    const pinch = Gesture.Pinch()
      .onStart(() => { pinch0.value = scaleT.value; })
      .onUpdate(e => { scaleT.value = Math.max(MIN_SCALE, Math.min(MAX_SCALE, pinch0.value * e.scale)); });
    const tap = Gesture.Tap().maxDistance(8).onEnd((e, ok) => {
      if (!ok) return;
      const m = marks.value;
      let best = -1, bd = 28;
      for (let i = 0; i < EX.length; i++) {
        if (!m[i * 4 + 2]) continue;
        const d = Math.hypot(m[i * 4] - e.x, m[i * 4 + 1] - e.y);
        if (d < bd) { bd = d; best = i; }
      }
      if (best >= 0) runOnJS(onSelect)(EX[best].code);
    });
    return Gesture.Race(Gesture.Simultaneous(pan, pinch), tap);
  }, [onSelect, scrollRef]);

  const zoom = f => { scaleT.value = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scaleT.value * f)); };

  return (
    <View style={{ height: H }}>
      <GestureDetector gesture={gesture}>
        <Canvas style={{ position: 'absolute', top: 0, left: 0, right: 0, bottom: 0 }} onSize={size}>
          <Circle cx={cx} cy={cy} r={ringR} color={C.a400} style="stroke" strokeWidth={1}><DashPathEffect intervals={[2, 6]} phase={ringPhase} /></Circle>
          <Path path={ticksMinor} color={C.a300} style="stroke" strokeWidth={1} />
          <Path path={ticksMajor} color={C.a600} style="stroke" strokeWidth={1} />
          <Group transform={orbitT}>
            <Path path={orbitBack} color={C.a500} style="stroke" strokeWidth={0.8} />
            <Group opacity={satBack}><Path path={satPath} color={C.a800} /><Path path={satLine} color={C.a600} style="stroke" strokeWidth={1} /></Group>
          </Group>
          <Circle cx={cx} cy={cy} r={R}>
            <TwoPointConicalGradient start={gStart} startR={gStartR} end={gEnd} endR={R} colors={[C.a100, C.a200]} />
          </Circle>
          {mask && EARTH && (
            <Circle cx={cx} cy={cy} r={R1}>
              <Shader source={EARTH} uniforms={uniforms}>
                <ImageShader image={mask} tx="repeat" ty="clamp" fit="fill" rect={{ x: 0, y: 0, width: TEX[0], height: TEX[1] }}
                  sampling={{ filter: FilterMode.Linear, mipmap: MipmapMode.None }} />
              </Shader>
            </Circle>
          )}
          <Path path={scanPath} color={C.a600} style="stroke" strokeWidth={1.4} opacity={0.55} />
          <Circle cx={cx} cy={cy} r={R} color={C.a700} style="stroke" strokeWidth={1} />
          <Path path={arcsPath} color={C.a600} style="stroke" strokeWidth={0.9} opacity={0.75}><DashPathEffect intervals={[3, 4]} phase={arcPhase} /></Path>
          <Path path={dotsPath} color={C.a800} />
          {EX.map((e, i) => <Marker key={e.code} i={i} marks={marks} sel={i === selIdx} open={openKey[i] === '1'} font={font} tw={widths[i]} />)}
          <Group transform={orbitT}>
            <Path path={orbitFront} color={C.a500} style="stroke" strokeWidth={0.8} />
            <Group opacity={satFront}><Path path={satPath} color={C.a800} /><Path path={satLine} color={C.a600} style="stroke" strokeWidth={1} /></Group>
          </Group>
          <Text x={20} y={21} text={coords} font={small} color={C.a800} />
        </Canvas>
      </GestureDetector>
      <T pointerEvents="none" style={{ position: 'absolute', left: 20, bottom: 10, fontSize: 11, letterSpacing: 0.9, textTransform: 'uppercase', color: C.n700 }}>Arraste para girar · toque num ponto</T>
      <View style={{ position: 'absolute', right: 16, top: 10, borderWidth: 1, borderColor: C.divider, borderRadius: 12, overflow: 'hidden', backgroundColor: C.bg }}>
        {[[IC.plus, 1.25], [IC.minus, 1 / 1.25]].map(([d, f], i) => (
          <Pressable key={i} onPress={() => zoom(f)} style={({ pressed }) => [{ width: 40, height: 40, alignItems: 'center', justifyContent: 'center', backgroundColor: pressed ? C.a100 : 'transparent' }, i === 0 && { borderBottomWidth: 1, borderBottomColor: C.divider }]}>
            <Icon d={d} size={18} />
          </Pressable>
        ))}
      </View>
    </View>
  );
}

export default memo(Globe);
