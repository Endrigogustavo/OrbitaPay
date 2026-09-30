import { Pressable, View } from 'react-native';
import { router } from 'expo-router';
import { Letreiro } from '@/components/Letreiro';
import { Saldo } from '@/components/Saldo';
import { TelaDaAba } from '@/components/TelaDaAba';
import { Anim, Avatar, Blueprint, Btn, Icon, Kicker, Scan, T, Tag } from '@/components/ui';
import { REASON } from '@/constants/demonstracao';
import { brl, fmtT, ini } from '@/constants/formatacao';
import { IC } from '@/constants/icones';
import { C } from '@/constants/tema';
import { useMercado } from '@/context/MercadoContext';
import { useOperacoes } from '@/context/OperacoesContext';
import { usePainel } from '@/context/usePainel';

const ICONE_DA_MOVIMENTACAO = { sell: IC.down, buy: IC.up, sys: IC.check, dep: IC.dep, saq: IC.saq } as const;

export default function Inicio() {
  const { usuario: u, bloqueada, valorDaCarteira: pv, exibir } = usePainel();
  const { acoes } = useMercado();
  const op = useOperacoes();
  if (!u) return null;

  const hide = op.ocultarValores;
  const atalhos = [
    { label: 'Depositar', d: IC.dep, locked: false, on: () => op.abrir({ kind: 'amount', op: 'dep', method: 'PIX' }, { amt: '' }) },
    { label: 'Sacar', d: IC.saq, locked: bloqueada, on: () => (bloqueada ? op.abrir({ kind: 'locked' }) : op.abrir({ kind: 'amount', op: 'saq' }, { amt: '' })) },
    { label: 'Investir', d: IC.chart, locked: bloqueada, on: () => router.navigate('/mercado') },
    { label: 'Globo', d: IC.globe, locked: false, on: () => router.navigate('/globo') },
  ];

  return (
    <TelaDaAba>
      <View style={{ gap: 20, paddingHorizontal: 20, paddingTop: 10 }}>
        <View style={{ flexDirection: 'row', alignItems: 'center', gap: 12 }}>
          <Avatar text={ini(u.name)} />
          <View style={{ flex: 1 }}>
            <T h style={{ fontSize: 22, lineHeight: 24 }}>Olá, {u.name.split(' ')[0]}</T>
            <T num style={{ fontSize: 12, color: C.n700 }}>Ag {u.agency} · CC {u.acct}</T>
          </View>
          <Btn onPress={op.alternarOcultarValores} style={{ width: 40, height: 40, borderRadius: 20, paddingHorizontal: 0 }}><Icon d={hide ? IC.eyeOff : IC.eye} size={20} /></Btn>
        </View>

        <Blueprint anim="rise" style={{ paddingTop: 18, paddingHorizontal: 18, paddingBottom: 16 }}>
          <Scan />
          <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
            <Kicker>Saldo disponível</Kicker>
            <Tag kind={bloqueada ? 'neutral' : 'accent'} dot pulse={!bloqueada}>{bloqueada ? 'Bloqueada' : 'Ativa'}</Tag>
          </View>
          <Saldo value={u.balance} id={u.id} hide={hide} />
          <View style={{ flexDirection: 'row', borderTopWidth: 1, borderTopColor: C.divider, paddingTop: 12, gap: 12 }}>
            <View style={{ flex: 1 }}><T style={{ fontSize: 11, color: C.n700 }}>Carteira de ações</T><T h num style={{ fontSize: 18 }}>{hide ? 'R$ ••••' : brl(pv)}</T></View>
            <View style={{ flex: 1 }}><T style={{ fontSize: 11, color: C.n700 }}>Patrimônio total</T><T h num style={{ fontSize: 18 }}>{hide ? 'R$ ••••' : brl(pv + u.balance)}</T></View>
          </View>
        </Blueprint>

        {bloqueada && (
          <Anim type="fadeUp" duration={400}>
            <Pressable onPress={() => op.abrir({ kind: 'locked' })} style={{ flexDirection: 'row', alignItems: 'center', gap: 12, padding: 14, backgroundColor: C.a900 }}>
              <Icon d={IC.lock} color={C.bg} />
              <View style={{ flex: 1 }}>
                <T h style={{ fontSize: 18, lineHeight: 20, color: C.bg }}>Conta bloqueada</T>
                <T style={{ fontSize: 13, color: C.a200 }}>{(u.reason ? REASON[u.reason] : '') + ' Toque para detalhes.'}</T>
              </View>
              <Icon d={IC.chevronRight} size={18} color={C.bg} />
            </Pressable>
          </Anim>
        )}

        <Blueprint style={{ flexDirection: 'row', gap: 1, backgroundColor: C.divider }}>
          {atalhos.map((a, i) => (
            <Anim key={a.label} type="rise" delay={100 + i * 60} duration={500} style={{ flex: 1 }}>
              <Pressable onPress={a.on} style={({ pressed }) => ({ gap: 18, paddingTop: 14, paddingHorizontal: 11, paddingBottom: 12, backgroundColor: pressed ? C.a200 : C.bg })}>
                <Icon d={a.d} color={C.a700} />
                <T h style={{ fontSize: 15, lineHeight: 16 }}>{a.label}</T>
                {a.locked && <Icon d={IC.lock} size={13} color={C.n700} style={{ position: 'absolute', top: 10, right: 9 }} />}
              </Pressable>
            </Anim>
          ))}
        </Blueprint>

        {acoes.length > 0 && <Letreiro items={[...acoes, ...acoes].map(exibir)} />}

        <View>
          <View style={{ flexDirection: 'row', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 4 }}>
            <T h style={{ fontSize: 22 }}>Movimentações</T>
            <T style={{ fontSize: 12, color: C.n700 }}>{u.tx.length} registros</T>
          </View>
          {u.tx.length === 0 && <T style={{ paddingVertical: 14, fontSize: 13, color: C.n700 }}>Abrindo sua conta…</T>}
          {u.tx.slice(0, 8).map((t, i) => (
            <Anim key={t.id} type="fadeUp" delay={150 + i * 50} duration={400} style={{ flexDirection: 'row', alignItems: 'center', gap: 12, paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: C.divider }}>
              <View style={{ width: 38, height: 38, alignItems: 'center', justifyContent: 'center', borderWidth: 1, borderColor: C.divider }}>
                <Icon d={ICONE_DA_MOVIMENTACAO[t.type]} size={18} color={C.a700} />
              </View>
              <View style={{ flex: 1 }}>
                <T m numberOfLines={1} style={{ fontSize: 14 }}>{t.desc}</T>
                <T style={{ fontSize: 12, color: C.n700 }}>{fmtT(t.ts)}</T>
              </View>
              <T h num style={{ fontSize: 16, color: t.amt > 0 ? C.a700 : C.text }}>{t.amt === 0 ? '—' : (t.amt > 0 ? '+ ' : '− ') + brl(Math.abs(t.amt))}</T>
            </Anim>
          ))}
        </View>
      </View>
    </TelaDaAba>
  );
}
