import type { MotivoBloqueio } from '@/@types/orbita';

export const MAX_ATTEMPTS = 3;

export const DEMO = [
  { nome: 'Ana', email: 'ana@orbita.com', pin: '1234' },
  { nome: 'Bruno', email: 'bruno@orbita.com', pin: '4321' },
  { nome: 'Carla', email: 'carla@orbita.com', pin: '1111' },
  { nome: 'Gerente', email: 'gerente@orbita.com', pin: '0000' },
  { nome: 'Admin', email: 'admin@orbita.com', pin: '9999' },
];

export const REASON: Record<MotivoBloqueio, string> = {
  pin: 'Bloqueada após tentativas de PIN incorretas.',
  admin: 'Bloqueada pelo gerente do banco.',
  user: 'Você bloqueou a conta por segurança.',
};
