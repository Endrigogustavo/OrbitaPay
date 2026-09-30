import { Tabs } from 'expo-router';
import { BarraDeAbas } from '@/components/BarraDeAbas';
import { ABAS } from '@/constants/abas';
import { C } from '@/constants/tema';

export default function AbasLayout() {
  return (
    <Tabs backBehavior="firstRoute" screenOptions={{ headerShown: false, sceneStyle: { backgroundColor: C.bg } }} tabBar={props => <BarraDeAbas {...props} />}>
      {ABAS.map(aba => <Tabs.Screen key={aba.rota} name={aba.rota} options={{ title: aba.rotulo }} />)}
    </Tabs>
  );
}
