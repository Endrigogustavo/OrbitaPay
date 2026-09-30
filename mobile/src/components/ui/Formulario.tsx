import type { ReactNode } from 'react';
import { Pressable, TextInput, View, type StyleProp, type TextInputProps, type ViewStyle } from 'react-native';
import { IC } from '@/constants/icones';
import { C, F } from '@/constants/tema';
import { Anim } from './Animacoes';
import { Icon } from './Icone';
import { T } from './Texto';

interface FieldProps {
  label: string;
  children?: ReactNode;
  style?: StyleProp<ViewStyle>;
}

export function Field({ label, children, style }: FieldProps) {
  return (
    <View style={style}>
      <T style={{ fontSize: 12, marginBottom: 5, color: C.label }}>{label}</T>
      {children}
    </View>
  );
}

interface InputProps extends TextInputProps {
  /** Campo de PIN: numérico, oculto e com 4 dígitos. */
  pin?: boolean;
}

export function Input({ style, pin, ...p }: InputProps) {
  return (
    <TextInput
      placeholderTextColor={C.n500}
      selectionColor={C.accent}
      cursorColor={C.accent}
      autoCorrect={false}
      {...(pin ? { secureTextEntry: true, keyboardType: 'number-pad', maxLength: 4 } as const : null)}
      {...p}
      style={[{ minHeight: 44, paddingVertical: 6, paddingHorizontal: 10, fontFamily: F.b, fontSize: 15, color: C.text,
        backgroundColor: C.surface, borderWidth: 1, borderColor: C.divider, borderRadius: 12 }, pin && { fontSize: 18, letterSpacing: 5 }, style, p.editable === false && { opacity: 0.6 }]}
    />
  );
}

export interface OpcaoDoSeg {
  label: string;
  on: boolean;
  pick: () => void;
}

/** Controle segmentado. */
export function Seg({ options, style }: { options: OpcaoDoSeg[]; style?: StyleProp<ViewStyle> }) {
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

interface ErrBoxProps {
  msg?: string;
  icon?: boolean;
  soft?: boolean;
}

export function ErrBox({ msg, icon, soft }: ErrBoxProps) {
  if (!msg) return null;
  return (
    <Anim type="fadeUp" duration={250} style={{ flexDirection: 'row', gap: 8, alignItems: 'center', backgroundColor: C.a100, borderRadius: 12, paddingVertical: 9, paddingHorizontal: 10,
      borderWidth: soft ? 0 : 1, borderColor: C.a300 }}>
      {icon && <Icon d={IC.alert} size={16} color={soft ? C.a800 : C.a900} />}
      <T style={{ flex: 1, fontSize: 13, color: soft ? C.a800 : C.a900 }}>{msg}</T>
    </Anim>
  );
}
