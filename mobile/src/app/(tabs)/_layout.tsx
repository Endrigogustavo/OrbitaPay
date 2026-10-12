import { Tabs } from 'expo-router';
import { BarraDeAbas } from '@/components/BarraDeAbas';
import { ABAS, abaVisivel } from '@/constants/abas';
import { C } from '@/constants/tema';
import { useSessao } from '@/context/SessaoContext';

export default function AbasLayout() {
  const { equipe } = useSessao();
  return (
    <Tabs backBehavior="firstRoute" screenOptions={{ headerShown: false, sceneStyle: { backgroundColor: C.bg } }} tabBar={props => <BarraDeAbas {...props} />}>
      {ABAS.map(aba => (
        <Tabs.Protected key={aba.rota} guard={abaVisivel(aba, equipe)}>
          <Tabs.Screen name={aba.rota} options={{ title: aba.rotulo }} />
        </Tabs.Protected>
      ))}
    </Tabs>
  );
}
