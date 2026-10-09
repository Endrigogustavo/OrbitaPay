import type { Acao, ClienteDoBackoffice } from '@/@types/orbita';
import { esperar, parseNum } from '@/constants/formatacao';
import { comoErroDaApi } from '@/integration/http';
import type { Ferramentas } from './ferramentas';

export function criarOperacoesDeGerencia(f: Ferramentas) {
  const { api, sessao, carregarCliente, encerrarSessao } = f.sessao;

  function ehOProprioCliente(clienteId: string) {
    return clienteId === sessao?.clienteId;
  }

  function salvarCliente() {
    const folha = f.folha;
    const form = f.form;
    if (!folha || folha.kind !== 'userForm') return;
    f.ocupar(async () => {
      if (folha.mode === 'self') {
        await api.clientes.atualizarMeu({ nome: form.name ?? '', email: form.email ?? '' });
        await carregarCliente();
        f.fechar();
        return f.avisar('Dados atualizados');
      }
      if (folha.mode === 'new') {
        if ((form.pin || '').length !== 4) return f.falhar('PIN inicial de 4 dígitos');
        const deposito = form.balance ? parseNum(form.balance) : 0;
        if (isNaN(deposito) || deposito < 0) return f.falhar('Saldo inicial inválido');
        const cliente = await api.clientes.cadastrarPeloGerente({
          nome: form.name ?? '', email: form.email ?? '', cpf: form.cpf ?? '', pin: form.pin ?? '',
          depositoInicial: deposito,
        });
        f.fechar();
        f.avisar('Cliente ' + cliente.nome.split(' ')[0] + ' cadastrado');
        await esperar(700);
        return f.gerente.carregarBackoffice();
      }
      if (!folha.id) return;
      await api.clientes.atualizar(folha.id, { nome: form.name ?? '', email: form.email ?? '', cpf: form.cpf ?? '' });
      f.fechar();
      f.avisar('Dados atualizados');
      await f.gerente.carregarBackoffice();
      if (ehOProprioCliente(folha.id)) carregarCliente().catch(() => {});
    });
  }

  function alternarBloqueio(cliente: ClienteDoBackoffice) {
    f.ocupar(async () => {
      if (cliente.blocked) await api.clientes.desbloquear(cliente.id);
      else await api.clientes.bloquear(cliente.id);
      f.avisar(cliente.name.split(' ')[0] + (cliente.blocked ? ' desbloqueada(o)' : ' bloqueada(o)'));
      await f.gerente.carregarBackoffice();
      if (ehOProprioCliente(cliente.id)) carregarCliente().catch(() => {});
    });
  }

  function removerCliente(cliente: ClienteDoBackoffice) {
    f.ocupar(async () => {
      await api.clientes.remover(cliente.id);
      if (ehOProprioCliente(cliente.id)) return encerrarSessao('Cliente excluído');
      f.atualizarFolha(() => null);
      f.avisar('Cliente excluído');
      await f.gerente.carregarBackoffice();
    });
  }

  function editarAtivo(acao: Acao) {
    f.abrir({ kind: 'stockForm', edit: true }, {
      form: { ticker: acao.ticker, sname: acao.name, sector: acao.sector, price: String(acao.price).replace('.', ','), ex: acao.ex },
    });
  }

  function salvarAtivo() {
    const folha = f.folha;
    const form = f.form;
    const ticker = (form.ticker || '').trim();
    const preco = parseNum(form.price);
    if (!folha || folha.kind !== 'stockForm') return;
    if (ticker.length < 2) return f.falhar('Ticker com pelo menos 2 caracteres');
    if (!(form.sname || '').trim()) return f.falhar('Informe o nome da empresa');
    if (!(preco > 0)) return f.falhar('Preço precisa ser maior que zero');
    const dados = {
      ticker, nome: (form.sname || '').trim(), setor: (form.sector || '').trim(), bolsa: form.ex || 'B3', cotacao: preco,
    };
    f.ocupar(async () => {
      if (folha.edit) await api.ativos.atualizar(ticker, dados);
      else await api.ativos.listarNaBolsa(dados);
      await f.mercado.atualizarMercado();
      if (!folha.edit) f.mercado.mostrarBolsa(dados.bolsa);
      f.fechar();
      f.avisar(folha.edit ? ticker + ' atualizada' : ticker + ' listada na ' + dados.bolsa);
    });
  }

  function removerAtivo(ticker: string) {
    f.ocupar(async () => {
      await api.ativos.remover(ticker);
      await f.mercado.atualizarMercado();
      f.fechar();
      f.avisar(ticker + ' removida');
    });
  }

  async function entrarComoGerente(codigo: string) {
    try {
      await f.gerente.entrar(codigo);
      f.fechar();
    } catch (falha) {
      const erro = comoErroDaApi(falha);
      f.falhar(erro.status === 0 ? erro.message : 'Código de gerente inválido');
    }
  }

  return { salvarCliente, alternarBloqueio, removerCliente, editarAtivo, salvarAtivo, removerAtivo, entrarComoGerente };
}
