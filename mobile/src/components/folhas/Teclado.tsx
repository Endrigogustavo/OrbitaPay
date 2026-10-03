import { Pressable, View } from 'react-native';
import { IC } from '@/constants/icones';
import { C } from '@/constants/tema';
import { Icon, T } from '../ui';

const TECLAS = {
  pin: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '', '0', 'del'],
  valor: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '00', '0', 'del'],
};

export function Teclado({ tipo, aoPressionar }: { tipo: keyof typeof TECLAS; aoPressionar: (tecla: string) => void }) {
  const keys = TECLAS[tipo];
  const rows = [0, 3, 6, 9].map(i => keys.slice(i, i + 3));
  return (
    <View style={{ gap: 1, backgroundColor: C.divider, borderWidth: 1, borderColor: C.divider, borderRadius: 16, overflow: 'hidden', marginTop: 16 }}>
      {rows.map((r, i) => (
        <View key={i} style={{ flexDirection: 'row', gap: 1 }}>
          {r.map((k, j) => (
            <Pressable key={j} onPress={() => k && aoPressionar(k)} style={({ pressed }) => ({ flex: 1, height: 58, alignItems: 'center', justifyContent: 'center', backgroundColor: pressed && k ? C.a300 : C.bg })}>
              {k === 'del' ? <Icon d={IC.del} /> : <T h num style={{ fontSize: 26 }}>{k}</T>}
            </Pressable>
          ))}
        </View>
      ))}
    </View>
  );
}
