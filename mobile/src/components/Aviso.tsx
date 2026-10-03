import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { IC } from '@/constants/icones';
import { C } from '@/constants/tema';
import { useToast } from '@/context/ToastContext';
import { Anim, Icon, T } from './ui';

export function Aviso() {
  const { aviso } = useToast();
  const insets = useSafeAreaInsets();
  if (!aviso) return null;
  return (
    <Anim key={aviso.id} type="drop" style={{ position: 'absolute', top: insets.top + 6, left: 16, right: 16, zIndex: 45, flexDirection: 'row', alignItems: 'center', gap: 10, paddingVertical: 12, paddingHorizontal: 14, backgroundColor: C.a900, elevation: 8 }}>
      <Icon d={IC.check} size={18} color={C.bg} />
      <T style={{ fontSize: 14, color: C.bg, flex: 1 }}>{aviso.msg}</T>
    </Anim>
  );
}
