export const MAX_ATTEMPTS = 3;

export const EX = [
  { code: 'B3', full: 'B3 · Brasil, Bolsa, Balcão', city: 'São Paulo', lat: -23.55, lon: -46.63, tz: 'America/Sao_Paulo', off: -3, cur: 'BRL', fx: 1, o: 10, c: 17, side: 1 },
  { code: 'NYC', full: 'NYSE · Nasdaq', city: 'Nova York', lat: 40.71, lon: -74.01, tz: 'America/New_York', off: -4, cur: 'USD', fx: 5.35, o: 9.5, c: 16, side: 1 },
  { code: 'TSX', full: 'Toronto Stock Exchange', city: 'Toronto', lat: 43.65, lon: -79.38, tz: 'America/Toronto', off: -4, cur: 'CAD', fx: 3.9, o: 9.5, c: 16, side: -1 },
  { code: 'LSE', full: 'London Stock Exchange', city: 'Londres', lat: 51.51, lon: -0.09, tz: 'Europe/London', off: 1, cur: 'GBP', fx: 7.2, o: 8, c: 16.5, side: -1 },
  { code: 'EPA', full: 'Euronext Paris', city: 'Paris', lat: 48.86, lon: 2.35, tz: 'Europe/Paris', off: 2, cur: 'EUR', fx: 6.25, o: 9, c: 17.5, side: -1 },
  { code: 'FRA', full: 'Deutsche Börse', city: 'Frankfurt', lat: 50.11, lon: 8.68, tz: 'Europe/Berlin', off: 2, cur: 'EUR', fx: 6.25, o: 9, c: 17.5, side: 1 },
  { code: 'JSE', full: 'Johannesburg Stock Exchange', city: 'Joanesburgo', lat: -26.2, lon: 28.04, tz: 'Africa/Johannesburg', off: 2, cur: 'ZAR', fx: 0.3, o: 9, c: 17, side: 1 },
  { code: 'NSE', full: 'National Stock Exchange', city: 'Mumbai', lat: 19.07, lon: 72.87, tz: 'Asia/Kolkata', off: 5.5, cur: 'INR', fx: 0.064, o: 9.25, c: 15.5, side: 1 },
  { code: 'SSE', full: 'Shanghai Stock Exchange', city: 'Xangai', lat: 31.23, lon: 121.47, tz: 'Asia/Shanghai', off: 8, cur: 'CNY', fx: 0.75, o: 9.5, c: 15, side: 1 },
  { code: 'HKEX', full: 'Hong Kong Exchanges', city: 'Hong Kong', lat: 22.28, lon: 114.16, tz: 'Asia/Hong_Kong', off: 8, cur: 'HKD', fx: 0.69, o: 9.5, c: 16, side: -1 },
  { code: 'TSE', full: 'Tokyo Stock Exchange', city: 'Tóquio', lat: 35.68, lon: 139.77, tz: 'Asia/Tokyo', off: 9, cur: 'JPY', fx: 0.036, o: 9, c: 15, side: 1 },
  { code: 'ASX', full: 'Australian Securities Exchange', city: 'Sydney', lat: -33.87, lon: 151.21, tz: 'Australia/Sydney', off: 10, cur: 'AUD', fx: 3.55, o: 10, c: 16, side: 1 },
];
export const EXM = Object.fromEntries(EX.map(e => [e.code, e]));

export const IC = {
  home: 'M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8 M3 10a2 2 0 0 1 .709-1.528l7-5.999a2 2 0 0 1 2.582 0l7 5.999A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z',
  chart: 'M9 5v4 M7 9h4v6H7z M9 15v2 M17 3v2 M15 5h4v8h-4z M17 13v3 M3 3v16a2 2 0 0 0 2 2h16',
  globe: 'M12 12m-10 0a10 10 0 1 0 20 0a10 10 0 1 0 -20 0 M12 2a14.5 14.5 0 0 0 0 20 14.5 14.5 0 0 0 0-20 M2 12h20',
  bank: 'M10 18v-7 M11.12 2.198a2 2 0 0 1 1.76.006l7.866 3.847c.476.233.31.949-.22.949H3.474c-.53 0-.695-.716-.22-.949z M14 18v-7 M18 18v-7 M3 22h18 M6 18v-7',
  user: 'M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2 M12 7m-4 0a4 4 0 1 0 8 0a4 4 0 1 0 -8 0',
  dep: 'M12 17V3 M6 11l6 6 6-6 M19 21H5', saq: 'M18 9l-6-6-6 6 M12 3v14 M5 21h14',
  lock: 'M5 11h14a2 2 0 0 1 2 2v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2z M7 11V7a5 5 0 0 1 10 0v4',
  lockBody: 'M5 11h14a2 2 0 0 1 2 2v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2z M12 15v3', lockShackle: 'M7 11V7a5 5 0 0 1 10 0v4',
  unlock: 'M5 11h14a2 2 0 0 1 2 2v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-7a2 2 0 0 1 2-2z M7 11V7a5 5 0 0 1 9.9-1',
  plus: 'M5 12h14 M12 5v14', minus: 'M5 12h14', x: 'M18 6 6 18 M6 6l12 12', check: 'M20 6 9 17l-5-5',
  chevronLeft: 'M15 18l-6-6 6-6', chevronRight: 'M9 18l6-6-6-6', arrowRight: 'M5 12h14 M12 5l7 7-7 7',
  eye: 'M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0 M12 12m-3 0a3 3 0 1 0 6 0a3 3 0 1 0 -6 0',
  eyeOff: 'M10.733 5.076a10.744 10.744 0 0 1 11.205 6.575 1 1 0 0 1 0 .696 10.747 10.747 0 0 1-1.444 2.49 M14.084 14.158a3 3 0 0 1-4.242-4.242 M17.479 17.499a10.75 10.75 0 0 1-15.417-5.151 1 1 0 0 1 0-.696 10.75 10.75 0 0 1 4.446-5.143 M2 2l20 20',
  trash: 'M3 6h18 M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6 M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2',
  pencil: 'M21.174 6.812a1 1 0 0 0-3.986-3.987L3.842 16.174a2 2 0 0 0-.5.83l-1.321 4.352a.5.5 0 0 0 .623.622l4.353-1.32a2 2 0 0 0 .83-.497z',
  logout: 'M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4 M16 17l5-5-5-5 M21 12H9',
  up: 'M22 7l-8.5 8.5-5-5L2 17 M16 7h6v6', down: 'M22 17l-8.5-8.5-5 5L2 7 M16 17h6v-6',
  search: 'M11 11m-8 0a8 8 0 1 0 16 0a8 8 0 1 0 -16 0 M21 21l-4.3-4.3',
  del: 'M10 5a2 2 0 0 0-1.344.519l-6.328 5.74a1 1 0 0 0 0 1.481l6.328 5.741A2 2 0 0 0 10 19h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2z M12 9l6 6 M18 9l-6 6',
  shield: 'M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z',
  key: 'M2.586 17.414A2 2 0 0 0 2 18.828V21a1 1 0 0 0 1 1h3a1 1 0 0 0 1-1v-1a1 1 0 0 1 1-1h1a1 1 0 0 0 1-1v-1a1 1 0 0 1 1-1h.172a2 2 0 0 0 1.414-.586l.814-.814a6.5 6.5 0 1 0-4-4z M16.5 7.5m-.5 0a.5 .5 0 1 0 1 0a.5 .5 0 1 0 -1 0',
  alert: 'M12 12m-10 0a10 10 0 1 0 20 0a10 10 0 1 0 -20 0 M12 8v4 M12 16h.01',
  spark: 'M12 3v18 M3 12h18',
};

export const TABS = [
  { k: 'home', l: 'Início', i: 'home' }, { k: 'market', l: 'Mercado', i: 'chart' }, { k: 'globe', l: 'Globo', i: 'globe' },
  { k: 'bank', l: 'Banco', i: 'bank' }, { k: 'profile', l: 'Perfil', i: 'user' },
];

const SYM = { BRL: 'R$', USD: 'US$', CAD: 'CA$', GBP: '£', EUR: '€', ZAR: 'ZAR', INR: '₹', CNY: 'CN¥', HKD: 'HK$', JPY: 'JP¥', AUD: 'AU$' };
const group = (n, dec) => {
  const [i, f] = Math.abs(n).toFixed(dec).split('.');
  return i.replace(/\B(?=(\d{3})+(?!\d))/g, '.') + (dec ? ',' + f : '');
};
export const money = (n, c) => (n < 0 ? '-' : '') + (SYM[c] || c) + ' ' + group(n, c === 'JPY' ? 0 : 2);
export const brl = n => money(n || 0, 'BRL');
export const pct = n => (n >= 0 ? '+' : '') + n.toFixed(2).replace('.', ',') + '%';
export const uid = () => Math.random().toString(36).slice(2, 9);
export const ini = n => { const p = (n || '?').trim().split(/\s+/); return ((p[0] || '')[0] + ((p.length > 1 ? p[p.length - 1] : '')[0] || '')).toUpperCase(); };
export const maskCpf = v => { const d = v.replace(/\D/g, '').slice(0, 11); let o = d.slice(0, 3); if (d.length > 3) o += '.' + d.slice(3, 6); if (d.length > 6) o += '.' + d.slice(6, 9); if (d.length > 9) o += '-' + d.slice(9); return o; };
export const parseNum = v => parseFloat(String(v || '').replace(/\./g, '').replace(',', '.'));
const p2 = n => String(n).padStart(2, '0');
export const hhmm = d => p2(d.getHours()) + ':' + p2(d.getMinutes());
const MON = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez'];
export const fmtT = ts => {
  const d = new Date(ts), n = new Date();
  if (d.toDateString() === n.toDateString()) return 'Hoje, ' + hhmm(d);
  const y = new Date(n - 864e5);
  if (d.toDateString() === y.toDateString()) return 'Ontem, ' + hhmm(d);
  return p2(d.getDate()) + ' de ' + MON[d.getMonth()] + '. · ' + hhmm(d);
};
export function pts(h, w, hh, pad) { pad = pad || 2; const mn = Math.min(...h), mx = Math.max(...h), r = mx - mn || 1; return h.map((v, i) => [+(i / (h.length - 1) * w).toFixed(1), +(pad + (1 - (v - mn) / r) * (hh - pad * 2)).toFixed(1)]); }

const TF = {};
const WD = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
function tzParts(e, now) {
  try {
    const f = TF[e.tz] || (TF[e.tz] = new Intl.DateTimeFormat('en-GB', { timeZone: e.tz, weekday: 'short', hour: '2-digit', minute: '2-digit', hour12: false }));
    const p = f.formatToParts(now), g = t => (p.find(x => x.type === t) || {}).value;
    const h = (+g('hour')) % 24, m = +g('minute'), wd = g('weekday');
    if (!isNaN(h) && !isNaN(m) && wd) return { h, m, wd };
  } catch (err) {  }
  const d = new Date(now.getTime() + e.off * 36e5);
  return { h: d.getUTCHours(), m: d.getUTCMinutes(), wd: WD[d.getUTCDay()] };
}
export function localInfo(e, now) {
  const { h, m, wd } = tzParts(e, now), hr = h + m / 60;
  return { time: p2(h) + ':' + p2(m), open: !['Sat', 'Sun'].includes(wd) && hr >= e.o && hr < e.c };
}
export const fh2 = n => { const h = Math.floor(n), m = Math.round((n - h) * 60); return h + 'h' + (m ? p2(m) : ''); };

export const DEMO = [
  { nome: 'Ana', email: 'ana@orbita.com', pin: '1234' },
  { nome: 'Bruno', email: 'bruno@orbita.com', pin: '4321' },
  { nome: 'Carla', email: 'carla@orbita.com', pin: '1111' },
];
