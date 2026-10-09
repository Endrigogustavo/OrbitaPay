import type { Ferramentas } from './ferramentas';

export function criarOperacoesDoPerfil(f: Ferramentas) {
  const { api, carregarCliente, encerrarSessao } = f.sessao;

  function salvarPin() {
    const form = f.form;
    if ((form.pin || '').length !== 4) return f.falhar('Novo PIN com 4 dígitos');
    if (form.pin !== form.pin2) return f.falhar('Os PINs não conferem');
    f.ocupar(async () => {
      await api.autenticacao.alterarPin(form.pinOld ?? '', form.pin ?? '');
      f.fechar();
      f.avisar('PIN alterado');
    });
  }

  function bloquearMinhaConta() {
    f.ocupar(async () => {
      await api.clientes.bloquearMinhaConta();
      await carregarCliente();
      f.fechar();
      f.avisar('Conta bloqueada');
    });
  }

  function encerrarMinhaConta() {
    f.ocupar(async () => {
      await api.clientes.encerrarMinhaConta();
      encerrarSessao('Conta encerrada');
    });
  }

  async function desbloquearComPin(pin: string) {
    try {
      const assinatura = (await api.autenticacao.assinar(pin)).token;
      await api.clientes.desbloquearMinhaConta(assinatura);
      await carregarCliente();
      f.abrir({
        kind: 'success',
        title: 'Conta desbloqueada',
        big: 'Tudo certo',
        lines: [['Saques', 'Liberados'], ['Ordens na bolsa', 'Liberadas']],
      });
    } catch (falha) {
      f.erroDePin(falha);
    }
  }

  return { salvarPin, bloquearMinhaConta, encerrarMinhaConta, desbloquearComPin };
}
