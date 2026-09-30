import { Platform } from 'react-native';

const ENDERECO_PADRAO = Platform.OS === 'android' ? 'http://10.0.2.2:8080' : 'http://localhost:8080';

export const URL_DO_GATEWAY = (process.env.EXPO_PUBLIC_API_URL || ENDERECO_PADRAO).replace(/\/$/, '');
