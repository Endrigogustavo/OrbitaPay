import { View } from 'react-native';
import type { Confirmacao, FolhaDo } from '@/@types/orbita';
import { MAX_ATTEMPTS } from '@/constants/demonstracao';
import { brl } from '@/constants/formatacao';
import { IC } from '@/constants/icones';
import { C } from '@/constants/tema';
import { useOperacoes } from '@/context/OperacoesContext';
import { useSessao } from '@/context/SessaoContext';
import { IconeGrande } from '../Titulos';
import { Anim, Blueprint, Cta, ErrBox, InfoGrid, Kicker, Logo, Plain, PulseDot, T } from '../ui';

export function FolhaProcessando({ folha }: { folha: FolhaDo<'processing'> }) {
  return (
    <View style={{ gap: 16, paddingTop: 8 }}>
      <Blueprint anim="frame" style={{ width: 96, height: 96, alignItems: 'center', justifyContent: 'center', backgroundColor: C.a100 }}>
        <Logo size={48} core={15} sat={8} duration={1100} />
      </Blueprint>
      <Anim type="fadeUp" delay={150}>
        <Kicker>{folha.mode === 'buy' ? 'Ordem de compra' : 'Ordem de venda'}</Kicker>
        <T h style={{ fontSize: 34, lineHeight: 38 }}>{folha.qty} {folha.ticker}</T>
        <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8, marginTop: 4 }}>
          <PulseDot size={7} duration={900} />
          <T style={{ fontSize: 15, color: C.n800 }}>{folha.status}…</T>
        </View>
      </Anim>
      <InfoGrid items={[['Total estimado', brl(folha.total)], ['Ordem', folha.ordemId ? '#' + folha.ordemId.slice(-8).toUpperCase() : 'enviando']]} />
      <T style={{ fontSize: 12, color: C.n700 }}>A ordem passa pela negociação, pela sua conta e pela sua carteira antes de ser executada.</T>
    </View>
  );
}

export function FolhaDeSucesso({ folha }: { folha: FolhaDo<'success'> }) {
  const { fechar } = useOperacoes();
  return (
    <View style={{ gap: 16, paddingTop: 8 }}>
      <IconeGrande d={IC.check} bg={C.a100} sw={1.6} />
      <Anim type="fadeUp" delay={200}>
        <Kicker>{folha.title}</Kicker>
        <T h num style={{ fontSize: 48, lineHeight: 52 }}>{folha.big}</T>
      </Anim>
      <Anim type="fadeUp" delay={300} style={{ borderTopWidth: 1, borderTopColor: C.divider }}>
        {folha.lines.map(([k, v]) => (
          <View key={k} style={{ flexDirection: 'row', justifyContent: 'space-between', gap: 12, paddingVertical: 10, borderBottomWidth: 1, borderBottomColor: C.divider }}>
            <T style={{ fontSize: 14, color: C.n700 }}>{k}</T>
            <T num style={{ fontSize: 14, textAlign: 'right', flexShrink: 1 }}>{v}</T>
          </View>
        ))}
      </Anim>
      <Cta label="Concluir" icon={IC.check} onPress={fechar} />
    </View>
  );
}

export function FolhaContaBloqueada() {
  const { usuario: u } = useSessao();
  const { abrir, fechar } = useOperacoes();
  const max = u?.maxFails || MAX_ATTEMPTS;
  const why = u?.blocked ? (u.reason === 'pin' ? 'Detectamos ' + max + ' tentativas de PIN incorretas e bloqueamos a conta para proteger seu dinheiro.'
    : u.reason === 'admin' ? 'O gerente da sua conta aplicou um bloqueio preventivo.' : 'Você mesmo bloqueou a conta. Desbloqueie com o seu PIN quando quiser.') : '';
  const how = u?.blocked && u.reason !== 'user' ? 'Desbloqueio pelo gerente: aba Banco → cliente → Desbloquear (código 0000).' : '';
  return (
    <View style={{ gap: 16, paddingTop: 8 }}>
      <IconeGrande d={IC.lock} icon={46} bg={C.a900} color={C.bg} sw={1.4} />
      <Anim type="fadeUp" delay={150}>
        <Kicker>Segurança</Kicker>
        <T h style={{ fontSize: 32, lineHeight: 36, marginTop: 2, marginBottom: 6 }}>Conta bloqueada</T>
        <T style={{ fontSize: 15, color: C.n800 }}>{why}</T>
      </Anim>
      <Anim type="fadeUp" delay={250}><InfoGrid items={[['Suspenso', 'Saques e ordens na bolsa'], ['Liberado', 'Depósitos e consultas']]} /></Anim>
      {!!how && <T style={{ fontSize: 13, color: C.n700 }}>{how}</T>}
      {!!(u?.blocked && u.reason === 'user') && <Cta label="Desbloquear com PIN" icon={IC.unlock} onPress={() => abrir({ kind: 'pin', purpose: 'unblock' })} />}
      <Plain label="Entendi" onPress={fechar} style={{ minHeight: 48, paddingHorizontal: 18 }} />
    </View>
  );
}

export function FolhaDeConfirmacao({ folha }: { folha: Confirmacao }) {
  const { erro, fechar } = useOperacoes();
  return (
    <Anim type="fadeUp" duration={350} style={{ gap: 14 }}>
      <IconeGrande d={folha.d || IC.alert} size={64} icon={28} />
      <View>
        <T h style={{ fontSize: 30, lineHeight: 33, marginBottom: 6 }}>{folha.title}</T>
        <T style={{ fontSize: 15, color: C.n800 }}>{folha.body}</T>
      </View>
      {!!erro && <ErrBox icon msg={erro} />}
      {folha.hasGo && <Cta label={folha.label || 'Confirmar'} onPress={folha.go} />}
      <Plain label={folha.cancel || 'Cancelar'} onPress={fechar} style={{ minHeight: 48, paddingHorizontal: 18 }} />
    </Anim>
  );
}
