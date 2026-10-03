import type { ReactNode } from 'react';
import { GerenteProvider } from './GerenteContext';
import { MercadoProvider } from './MercadoContext';
import { OperacoesProvider } from './OperacoesContext';
import { SessaoProvider } from './SessaoContext';
import { ToastProvider } from './ToastContext';

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
