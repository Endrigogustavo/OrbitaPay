import { View } from 'react-native';
import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { useFonts } from 'expo-font';
import { Barlow_400Regular } from '@expo-google-fonts/barlow/400Regular';
import { Barlow_500Medium } from '@expo-google-fonts/barlow/500Medium';
import { BarlowCondensed_600SemiBold } from '@expo-google-fonts/barlow-condensed/600SemiBold';
import { Aviso } from '@/components/Aviso';
import { CamadaDeFolhas } from '@/components/folhas/CamadaDeFolhas';
import { Splash } from '@/components/Splash';
import { C } from '@/constants/tema';
import { OrbitaProvider } from '@/context/OrbitaProvider';
import { useSessao } from '@/context/SessaoContext';

export default function RootLayout() {
  const [carregadas] = useFonts({ Barlow_400Regular, Barlow_500Medium, BarlowCondensed_600SemiBold });
  if (!carregadas) return <View style={{ flex: 1, backgroundColor: C.a900 }} />;
  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <OrbitaProvider>
          <Navegacao />
        </OrbitaProvider>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}

/**
 * As rotas protegidas trocam sozinhas conforme a sessão: sem cliente autenticado só login e cadastro
 * ficam acessíveis; com cliente, só as abas. Folhas, avisos e splash ficam por cima de qualquer rota.
 */
function Navegacao() {
  const { fase, usuario } = useSessao();
  const logado = fase === 'app' && !!usuario;
  return (
    <View style={{ flex: 1, backgroundColor: C.bg }}>
      <StatusBar style={fase === 'splash' ? 'light' : 'dark'} />
      <Stack screenOptions={{ headerShown: false, contentStyle: { backgroundColor: C.bg }, animation: 'fade' }}>
        <Stack.Protected guard={logado}>
          <Stack.Screen name="(tabs)" />
        </Stack.Protected>
        <Stack.Protected guard={!logado}>
          <Stack.Screen name="login" />
          <Stack.Screen name="cadastro" options={{ animation: 'slide_from_right' }} />
        </Stack.Protected>
      </Stack>
      <CamadaDeFolhas />
      <Aviso />
      {fase === 'splash' && <Splash />}
    </View>
  );
}
