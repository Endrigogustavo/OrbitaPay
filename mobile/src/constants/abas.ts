import type { NomeDoIcone } from './icones';

export type PublicoDaAba = 'cliente' | 'equipe' | 'todos';

export interface Aba {
  rota: 'index' | 'mercado' | 'globo' | 'banco' | 'perfil';
  rotulo: string;
  icone: NomeDoIcone;
  publico: PublicoDaAba;
}

export const ABAS: Aba[] = [
  { rota: 'index', rotulo: 'Início', icone: 'home', publico: 'cliente' },
  { rota: 'banco', rotulo: 'Banco', icone: 'bank', publico: 'equipe' },
  { rota: 'mercado', rotulo: 'Mercado', icone: 'chart', publico: 'todos' },
  { rota: 'globo', rotulo: 'Globo', icone: 'globe', publico: 'todos' },
  { rota: 'perfil', rotulo: 'Perfil', icone: 'user', publico: 'cliente' },
];

export const abaVisivel = (aba: Aba, equipe: boolean) =>
  aba.publico === 'todos' || aba.publico === (equipe ? 'equipe' : 'cliente');
