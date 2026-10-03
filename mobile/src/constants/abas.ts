import type { NomeDoIcone } from './icones';

export interface Aba {
  rota: 'index' | 'mercado' | 'globo' | 'banco' | 'perfil';
  rotulo: string;
  icone: NomeDoIcone;
}

export const ABAS: Aba[] = [
  { rota: 'index', rotulo: 'Início', icone: 'home' },
  { rota: 'mercado', rotulo: 'Mercado', icone: 'chart' },
  { rota: 'globo', rotulo: 'Globo', icone: 'globe' },
  { rota: 'banco', rotulo: 'Banco', icone: 'bank' },
  { rota: 'perfil', rotulo: 'Perfil', icone: 'user' },
];
