import AsyncStorage from '@react-native-async-storage/async-storage';

const CHAVE = 'orbita-sessao-v2';

export async function lerSessao() {
  try {
    const bruto = await AsyncStorage.getItem(CHAVE);
    return bruto ? JSON.parse(bruto) : null;
  } catch (erro) {
    return null;
  }
}

export async function salvarSessao(sessao) {
  try {
    if (sessao) await AsyncStorage.setItem(CHAVE, JSON.stringify(sessao));
    else await AsyncStorage.removeItem(CHAVE);
  } catch (erro) {
    return;
  }
}
