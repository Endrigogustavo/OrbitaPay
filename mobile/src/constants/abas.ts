import type { NomeDoIcone } from './icones';

export interface Aba {
  /** Nome do arquivo em src/app/(tabs). */
  rota: 'index' | 'mercado' | 'globo' | 'banco' | 'perfil';
  rotulo: string;
  icone: NomeDoIcone;
}

/** Abas na ordem da barra inferior. */
export const ABAS: Aba[] = [
  { rota: 'index', rotulo: 'Início', icone: 'home' },
  { rota: 'mercado', rotulo: 'Mercado', icone: 'chart' },
  { rota: 'globo', rotulo: 'Globo', icone: 'globe' },
  { rota: 'banco', rotulo: 'Banco', icone: 'bank' },
  { rota: 'perfil', rotulo: 'Perfil', icone: 'user' },
];
