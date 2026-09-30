import type { MetodoDeDeposito } from './api';

// Modelos usados pelas telas, já convertidos a partir dos DTOs do gateway (ver integration/mapeadores).

export type MotivoBloqueio = 'pin' | 'admin' | 'user';
export type TipoMovimentacao = 'sys' | 'dep' | 'saq' | 'buy' | 'sell';
export type Direcao = 'up' | 'down';
export type ModoDeOrdem = 'buy' | 'sell';

export interface Acao {
  ticker: string;
  name: string;
  sector: string;
  ex: string;
  cur: string;
  fx: number;
  price: number;
  hist: number[];
  issued: number;
  flash: Direcao | null;
}

export interface Movimentacao {
  id: string;
  type: TipoMovimentacao;
  amt: number;
  desc: string;
  ts: number;
}

export interface Posicao {
  qty: number;
  reserved: number;
  avg: number;
}

export type Posicoes = Record<string, Posicao>;

export interface Usuario {
  id: string;
  name: string;
  email: string;
  cpf: string;
  blocked: boolean;
  reason: MotivoBloqueio | null;
  fails: number;
  maxFails: number;
  since: string;
  acct: string;
  agency: string;
  balance: number;
  tx: Movimentacao[];
  holdings: Posicoes;
}

export interface ClienteDoBackoffice {
  id: string;
  name: string;
  email: string;
  cpf: string;
  blocked: boolean;
  reason: MotivoBloqueio | null;
  acct: string;
  balance: number;
}

export interface Bolsa {
  code: string;
  full: string;
  city: string;
  lat: number;
  lon: number;
  tz: string;
  off: number;
  cur: string;
  fx: number;
  o: number;
  c: number;
  side: 1 | -1;
}

export interface InfoDaBolsa {
  time: string;
  open: boolean;
}

export interface SessaoSalva {
  token: string;
  clienteId: string;
}

/** Ação já formatada para exibição em listas, letreiro e folha de negociação. */
export interface AcaoExibida {
  ticker: string;
  name: string;
  sector: string;
  exCode: string;
  priceStr: string;
  chgStr: string;
  arrow: string;
  chgColor: string;
  spark: string;
  sparkColor: string;
  flashBg: string | undefined;
  held: boolean;
  on: () => void;
  edit: () => void;
}

export interface PosicaoExibida {
  ticker: string;
  qty: string;
  val: string;
  pl: string;
  on: () => void;
}

export type LinhaDeComprovante = [string, string];

export type Folha =
  | { kind: 'amount'; op: 'dep' | 'saq'; method?: MetodoDeDeposito }
  | { kind: 'pin'; purpose: 'withdraw'; v: number }
  | { kind: 'pin'; purpose: 'trade'; mode: ModoDeOrdem; ticker: string; qty: number; price: number; total: number }
  | { kind: 'pin'; purpose: 'manager' | 'unblock' }
  | { kind: 'processing'; ticker: string; mode: ModoDeOrdem; qty: number; total: number; status: string; ordemId?: string }
  | { kind: 'success'; title: string; big: string; lines: LinhaDeComprovante[] }
  | { kind: 'locked' }
  | { kind: 'stock'; ticker: string; mode: ModoDeOrdem }
  | { kind: 'userForm'; mode: 'self' | 'new' | 'admin'; id?: string }
  | { kind: 'stockForm'; edit: boolean }
  | { kind: 'changePin' }
  | Confirmacao;

export interface Confirmacao {
  kind: 'confirm';
  d?: string;
  title: string;
  body: string;
  hasGo: boolean;
  label?: string;
  cancel?: string;
  go?: () => void;
}

export type TipoDeFolha = Folha['kind'];
export type FolhaDo<K extends TipoDeFolha> = Extract<Folha, { kind: K }>;

export interface Formulario {
  name?: string;
  email?: string;
  cpf?: string;
  pin?: string;
  pin2?: string;
  pinOld?: string;
  balance?: string;
  ticker?: string;
  sname?: string;
  sector?: string;
  price?: string;
  ex?: string;
}

export type CampoDoFormulario = keyof Formulario;
