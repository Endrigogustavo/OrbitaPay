import { useEffect, useRef, type ReactNode } from 'react';
import { Animated, Dimensions, Easing, KeyboardAvoidingView, Pressable, ScrollView, StyleSheet, View } from 'react-native';
import { IC } from '@/constants/icones';
import { C } from '@/constants/tema';
import { Btn, Icon, OUT } from './ui';

const ABS = StyleSheet.absoluteFill;

interface FolhaProps {
  closing: boolean;
  onClose: () => void;
  bottom: number;
  children?: ReactNode;
}

export function Folha({ closing, onClose, bottom, children }: FolhaProps) {
  const H = Dimensions.get('window').height;
  const y = useRef(new Animated.Value(H)).current, o = useRef(new Animated.Value(0)).current;
  useEffect(() => {
    Animated.parallel(closing
      ? [Animated.timing(y, { toValue: H, duration: 260, easing: Easing.in(Easing.quad), useNativeDriver: true }), Animated.timing(o, { toValue: 0, duration: 260, useNativeDriver: true })]
      : [Animated.timing(y, { toValue: 0, duration: 450, easing: OUT, useNativeDriver: true }), Animated.timing(o, { toValue: 1, duration: 300, useNativeDriver: true })]).start();
  }, [closing]);
  return (
    <View style={[ABS, { zIndex: 30 }]}>
      <Animated.View style={[ABS, { backgroundColor: C.backdrop, opacity: o }]}><Pressable style={{ flex: 1 }} onPress={onClose} /></Animated.View>
      <KeyboardAvoidingView behavior="padding" style={{ flex: 1, justifyContent: 'flex-end' }} pointerEvents="box-none">
        <Animated.View style={{ maxHeight: '90%', backgroundColor: C.bg, borderTopWidth: 1, borderTopColor: C.divider, transform: [{ translateY: y }], elevation: 16 }}>
          <ScrollView keyboardShouldPersistTaps="handled" contentContainerStyle={{ paddingTop: 10, paddingHorizontal: 22, paddingBottom: 36 + bottom }}>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
              <View style={{ width: 40, height: 3, backgroundColor: C.n400 }} />
              <Btn kind="ghost" onPress={onClose} style={{ width: 36, height: 36, paddingHorizontal: 0, paddingVertical: 0, borderRadius: 18 }}><Icon d={IC.x} size={20} /></Btn>
            </View>
            {children}
          </ScrollView>
        </Animated.View>
      </KeyboardAvoidingView>
    </View>
  );
}
