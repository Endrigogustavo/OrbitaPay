import type { ReactNode } from 'react';
import { GerenteProvider } from './GerenteContext';
import { MercadoProvider } from './MercadoContext';
import { OperacoesProvider } from './OperacoesContext';
import { SessaoProvider } from './SessaoContext';
import { ToastProvider } from './ToastContext';

/** Todos os contextos do app, na ordem de dependência (cada um pode usar os de fora). */
export function OrbitaProvider({ children }: { children: ReactNode }) {
  return (
    <ToastProvider>
      <SessaoProvider>
        <MercadoProvider>
          <GerenteProvider>
            <OperacoesProvider>{children}</OperacoesProvider>
          </GerenteProvider>
        </MercadoProvider>
      </SessaoProvider>
    </ToastProvider>
  );
}
