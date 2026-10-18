import { Platform } from 'react-native';

const ENDERECO_PADRAO = Platform.OS === 'android' ? 'http://10.0.2.2:9000' : 'http://localhost:9000';

export const URL_DO_GATEWAY = (process.env.EXPO_PUBLIC_API_URL || ENDERECO_PADRAO).replace(/\/$/, '');
