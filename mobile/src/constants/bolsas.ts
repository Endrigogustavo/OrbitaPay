import type { Bolsa } from '@/@types/orbita';

export const EX: Bolsa[] = [
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

export const EXM: Record<string, Bolsa | undefined> = Object.fromEntries(EX.map(e => [e.code, e]));
