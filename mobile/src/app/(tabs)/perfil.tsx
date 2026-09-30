import { View } from 'react-native';
import { TelaDaAba } from '@/components/TelaDaAba';
import { Anim, Blueprint, Icon, InfoGrid, Kicker, Row, T, Tag } from '@/components/ui';
import { MAX_ATTEMPTS, REASON } from '@/constants/demonstracao';
import { brl, ini } from '@/constants/formatacao';
import { IC } from '@/constants/icones';
import { C } from '@/constants/tema';
import { useOperacoes } from '@/context/OperacoesContext';
import { useSessao } from '@/context/SessaoContext';
import { usePainel } from '@/context/usePainel';

export default function Perfil() {
  const { usuario: u, bloqueada, listaDePosicoes } = usePainel();
  const { encerrarSessao } = useSessao();
  const op = useOperacoes();
  if (!u) return null;

  const encerrarConta = () => {
    const temAtivos = u.balance >= 0.01 || listaDePosicoes.length > 0;
    op.confirmar(temAtivos
      ? { d: IC.alert, title: 'Ainda há saldo', body: 'Para encerrar, saque ' + brl(u.balance) + (listaDePosicoes.length ? ' e venda suas ' + listaDePosicoes.length + ' posições em ações' : '') + ' primeiro.', hasGo: false, cancel: 'Entendi' }
      : { d: IC.trash, title: 'Encerrar conta?', body: 'Sua conta e histórico serão excluídos. Essa ação não pode ser desfeita.', label: 'Encerrar definitivamente', hasGo: true, cancel: 'Manter conta',
        go: op.encerrarMinhaConta });
  };

  const menu = [
    { d: IC.pencil, label: 'Editar dados', sub: 'Nome e e-mail', on: () => op.abrir({ kind: 'userForm', mode: 'self', id: u.id }, { form: { name: u.name, email: u.email } }) },
    { d: IC.key, label: 'Alterar PIN', sub: 'Usado em saques e ordens', on: () => op.abrir({ kind: 'changePin' }, { form: {} }) },
    bloqueada
      ? { d: IC.unlock, label: 'Desbloquear conta', sub: u.reason ? REASON[u.reason] : '', on: () => op.abrir({ kind: 'locked' }) }
      : { d: IC.lock, label: 'Bloquear minha conta', sub: 'Suspende saques e ordens na hora', on: () => op.confirmar({ d: IC.lock, title: 'Bloquear conta?', body: 'Saques e ordens na bolsa ficam suspensos até você desbloquear com seu PIN. Depósitos continuam liberados.', label: 'Bloquear agora', hasGo: true, cancel: 'Cancelar',
        go: op.bloquearMinhaConta }) },
    { d: IC.trash, label: 'Encerrar conta', sub: 'Exclui seus dados da Órbita', on: encerrarConta },
    { d: IC.logout, label: 'Sair', sub: 'Voltar para a tela de acesso', on: () => encerrarSessao() },
  ];

  return (
    <TelaDaAba>
      <View style={{ gap: 22, paddingHorizontal: 20, paddingTop: 20 }}>
        <View style={{ flexDirection: 'row', gap: 16, alignItems: 'center' }}>
          <Blueprint anim="frame" style={{ width: 76, height: 76, alignItems: 'center', justifyContent: 'center', backgroundColor: C.a100 }}>
            <T h style={{ fontSize: 30, color: C.a800 }}>{ini(u.name)}</T>
          </Blueprint>
          <View style={{ flex: 1 }}>
            <T h style={{ fontSize: 30, lineHeight: 32 }}>{u.name}</T>
            <T style={{ fontSize: 13, color: C.n700 }}>{u.email}</T>
            <Tag kind={bloqueada ? 'neutral' : 'accent'} style={{ marginTop: 6 }}>{bloqueada ? 'Bloqueada' : 'Ativa'}</Tag>
          </View>
        </View>
        <InfoGrid items={[['CPF', '•••.' + (u.cpf || '').slice(4, 11) + '-••'], ['Conta', u.agency + ' · ' + u.acct], ['Cliente desde', u.since], ['Tentativas de PIN', (u.fails || 0) + ' de ' + (u.maxFails || MAX_ATTEMPTS)]]} />
        <View style={{ borderTopWidth: 1, borderTopColor: C.divider }}>
          {menu.map((m, i) => (
            <Anim key={m.label} type="fadeUp" delay={i * 50} duration={350}>
              <Row onPress={m.on} style={{ flexDirection: 'row', alignItems: 'center', gap: 14, paddingVertical: 14, paddingHorizontal: 4, borderBottomWidth: 1, borderBottomColor: C.divider }}>
                <Icon d={m.d} size={20} color={C.a700} />
                <View style={{ flex: 1 }}>
                  <T m style={{ fontSize: 15 }}>{m.label}</T>
                  <T style={{ fontSize: 12, color: C.n700 }}>{m.sub}</T>
                </View>
                <Icon d={IC.chevronRight} size={16} color={C.n600} />
              </Row>
            </Anim>
          ))}
        </View>
        <Kicker color={C.n600} style={{ letterSpacing: 1.3 }}>Órbita · microserviços v2.0</Kicker>
      </View>
    </TelaDaAba>
  );
}
