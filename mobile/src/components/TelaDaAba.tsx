import { useCallback, useRef, useState, type ReactNode, type RefObject } from 'react';
import { View } from 'react-native';
import { ScrollView as GestureScrollView } from 'react-native-gesture-handler';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useFocusEffect } from 'expo-router';
import { C } from '@/constants/tema';
import { Anim } from './ui';

export type RolagemDaAba = GestureScrollView;

interface TelaDaAbaProps {
  children: ReactNode;
  /** Ref opcional da rolagem, usada pelo globo para não disputar o gesto de arrastar. */
  rolagem?: RefObject<RolagemDaAba | null>;
}

/** Estrutura comum das abas: área segura, rolagem e animação de entrada a cada vez que a aba é aberta. */
export function TelaDaAba({ children, rolagem }: TelaDaAbaProps) {
  const insets = useSafeAreaInsets();
  const propria = useRef<RolagemDaAba>(null);
  const ref = rolagem ?? propria;
  const [entrada, setEntrada] = useState(0);
  const primeira = useRef(true);

  useFocusEffect(useCallback(() => {
    if (primeira.current) { primeira.current = false; return; }
    ref.current?.scrollTo({ y: 0, animated: false });
    setEntrada(n => n + 1);
  }, [ref]));

  return (
    <View style={{ flex: 1, paddingTop: insets.top, backgroundColor: C.bg }}>
      <GestureScrollView ref={ref} keyboardShouldPersistTaps="handled" contentContainerStyle={{ paddingBottom: 40 }}>
        <Anim key={entrada} type="fadeUp">{children}</Anim>
      </GestureScrollView>
    </View>
  );
}
