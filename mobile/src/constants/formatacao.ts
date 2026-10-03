import type { Bolsa, InfoDaBolsa } from '@/@types/orbita';

const SYM: Record<string, string> = { BRL: 'R$', USD: 'US$', CAD: 'CA$', GBP: '£', EUR: '€', ZAR: 'ZAR', INR: '₹', CNY: 'CN¥', HKD: 'HK$', JPY: 'JP¥', AUD: 'AU$' };

const group = (n: number, dec: number): string => {
  const [i, f] = Math.abs(n).toFixed(dec).split('.');
  return i.replace(/\B(?=(\d{3})+(?!\d))/g, '.') + (dec ? ',' + f : '');
};

export const money = (n: number, c: string): string => (n < 0 ? '-' : '') + (SYM[c] || c) + ' ' + group(n, c === 'JPY' ? 0 : 2);
export const brl = (n: number | null | undefined): string => money(n || 0, 'BRL');
export const pct = (n: number): string => (n >= 0 ? '+' : '') + n.toFixed(2).replace('.', ',') + '%';
export const uid = (): string => Math.random().toString(36).slice(2, 9);
export const esperar = (ms: number): Promise<void> => new Promise(resolve => setTimeout(resolve, ms));

export const ini = (n: string | null | undefined): string => {
  const p = (n || '?').trim().split(/\s+/);
  return ((p[0] || '')[0] + ((p.length > 1 ? p[p.length - 1] : '')[0] || '')).toUpperCase();
};

export const maskCpf = (v: string): string => {
  const d = v.replace(/\D/g, '').slice(0, 11);
  let o = d.slice(0, 3);
  if (d.length > 3) o += '.' + d.slice(3, 6);
  if (d.length > 6) o += '.' + d.slice(6, 9);
  if (d.length > 9) o += '-' + d.slice(9);
  return o;
};

export const parseNum = (v: string | number | null | undefined): number => parseFloat(String(v || '').replace(/\./g, '').replace(',', '.'));

const p2 = (n: number): string => String(n).padStart(2, '0');
export const hhmm = (d: Date): string => p2(d.getHours()) + ':' + p2(d.getMinutes());

const MON = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez'];
export const fmtT = (ts: number): string => {
  const d = new Date(ts), n = new Date();
  if (d.toDateString() === n.toDateString()) return 'Hoje, ' + hhmm(d);
  const y = new Date(n.getTime() - 864e5);
  if (d.toDateString() === y.toDateString()) return 'Ontem, ' + hhmm(d);
  return p2(d.getDate()) + ' de ' + MON[d.getMonth()] + '. · ' + hhmm(d);
};

export function pts(h: number[], w: number, hh: number, pad = 2): [number, number][] {
  const mn = Math.min(...h), mx = Math.max(...h), r = mx - mn || 1;
  return h.map((v, i) => [+((i / (h.length - 1)) * w).toFixed(1), +(pad + (1 - (v - mn) / r) * (hh - pad * 2)).toFixed(1)]);
}

const TF: Record<string, Intl.DateTimeFormat> = {};
const WD = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

function tzParts(e: Bolsa, now: Date): { h: number; m: number; wd: string } {
  try {
    const f = TF[e.tz] || (TF[e.tz] = new Intl.DateTimeFormat('en-GB', { timeZone: e.tz, weekday: 'short', hour: '2-digit', minute: '2-digit', hour12: false }));
    const p = f.formatToParts(now), g = (t: Intl.DateTimeFormatPartTypes) => p.find(x => x.type === t)?.value;
    const h = +(g('hour') ?? NaN) % 24, m = +(g('minute') ?? NaN), wd = g('weekday');
    if (!isNaN(h) && !isNaN(m) && wd) return { h, m, wd };
  } catch {
  }
  const d = new Date(now.getTime() + e.off * 36e5);
  return { h: d.getUTCHours(), m: d.getUTCMinutes(), wd: WD[d.getUTCDay()] };
}

export function localInfo(e: Bolsa, now: Date): InfoDaBolsa {
  const { h, m, wd } = tzParts(e, now), hr = h + m / 60;
  return { time: p2(h) + ':' + p2(m), open: !['Sat', 'Sun'].includes(wd) && hr >= e.o && hr < e.c };
}

export const fh2 = (n: number): string => {
  const h = Math.floor(n), m = Math.round((n - h) * 60);
  return h + 'h' + (m ? p2(m) : '');
};
