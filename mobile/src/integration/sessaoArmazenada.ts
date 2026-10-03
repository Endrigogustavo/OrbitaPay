import AsyncStorage from '@react-native-async-storage/async-storage';
import type { SessaoSalva } from '@/@types/orbita';

const CHAVE = 'orbita-sessao-v2';

export async function lerSessao(): Promise<SessaoSalva | null> {
  try {
    const bruto = await AsyncStorage.getItem(CHAVE);
    return bruto ? (JSON.parse(bruto) as SessaoSalva) : null;
  } catch {
    return null;
  }
}

export async function salvarSessao(sessao: SessaoSalva | null): Promise<void> {
  try {
    if (sessao) await AsyncStorage.setItem(CHAVE, JSON.stringify(sessao));
    else await AsyncStorage.removeItem(CHAVE);
  } catch {
  }
}
