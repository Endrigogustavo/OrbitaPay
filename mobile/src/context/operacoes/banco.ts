import type { MetodoDeDeposito, PagamentoDto } from '@/@types/api';
import { brl, esperar } from '@/constants/formatacao';
import type { Ferramentas } from './ferramentas';

const LIMITE_DO_SAQUE = 5000;
const TENTATIVAS_DE_CONFIRMACAO_DO_DEPOSITO = 30;
const ROTULO_DO_CODIGO: Record<MetodoDeDeposito, string> = {
  PIX: 'Pix copia e cola',
  Boleto: 'Linha digitável',
  TED: 'Dados para TED',
};

export function criarOperacoesDoBanco(f: Ferramentas) {
  const { api, usuario, carregarCliente } = f.sessao;

  function confirmarValor() {
    const folha = f.folha;
    const valor = parseInt(f.valor || '0', 10) / 100;
    if (!usuario || !folha || folha.kind !== 'amount') return;
    if (valor <= 0) return f.falhar('Digite um valor maior que zero');
    if (folha.op === 'dep') return depositar(valor, folha.method || 'PIX');
    if (usuario.blocked) return f.abrir({ kind: 'locked' });
    if (valor > usuario.balance) return f.falhar('Saldo insuficiente · disponível ' + brl(usuario.balance));
    if (valor > LIMITE_DO_SAQUE) return f.falhar('Limite por saque: R$ 5.000,00');
    f.abrir({ kind: 'pin', purpose: 'withdraw', v: valor });
  }

  function depositar(valor: number, metodo: MetodoDeDeposito) {
    return f.ocupar(async () => {
      const cobranca = await api.pagamentos.solicitar(valor, metodo);
      const codigo = cobranca.instrucoes.codigo;
      const codigoResumido = codigo.length > 28 ? codigo.slice(0, 28) + '…' : codigo;
      f.abrir({
        kind: 'success',
        title: 'Cobrança gerada',
        big: brl(valor),
        lines: [
          ['Método', metodo],
          [ROTULO_DO_CODIGO[metodo], codigoResumido],
          ['Situação', 'Aguardando pagamento'],
          ['Protocolo', '#' + cobranca.id.slice(-8).toUpperCase()],
        ],
      }, { amt: '' });
      acompanharDeposito(cobranca).catch(() => {});
    });
  }

  async function acompanharDeposito(cobranca: PagamentoDto) {
    for (let tentativa = 0; tentativa < TENTATIVAS_DE_CONFIRMACAO_DO_DEPOSITO; tentativa++) {
      await esperar(1500);
      const atual = await api.pagamentos.buscar(cobranca.id).catch(() => null);
      if (!atual || atual.status === 'PENDENTE') continue;
      if (atual.status === 'CONFIRMADO') {
        await esperar(600);
        await carregarCliente().catch(() => {});
        f.avisar('Depósito de ' + brl(Number(atual.valorPago ?? atual.valor)) + ' confirmado');
      } else {
        f.avisar('A cobrança expirou sem pagamento');
      }
      return;
    }
  }

  function sacar(valor: number, assinatura: string) {
    return f.ocupar(async () => {
      const comprovante = await api.contas.sacar(valor, assinatura);
      await carregarCliente();
      f.abrir({
        kind: 'success',
        title: 'Saque autorizado',
        big: brl(valor),
        lines: [
          ['Código de retirada', comprovante.protocolo],
          ['Válido por', '30 minutos'],
          ['Novo saldo', brl(Number(comprovante.novoSaldo))],
        ],
      }, { amt: '' });
    });
  }

  return { confirmarValor, sacar };
}
