import { useState } from 'react';
import { View } from 'react-native';
import { CampoDeBusca } from '@/components/ListaDeAtivos';
import { TelaDaAba } from '@/components/TelaDaAba';
import { Cabecalho, IconeGrande } from '@/components/Titulos';
import { Anim, Avatar, Blueprint, Btn, Cta, Icon, Kicker, Row, Seg, T, Tag } from '@/components/ui';
import { brl, ini } from '@/constants/formatacao';
import { IC } from '@/constants/icones';
import { C } from '@/constants/tema';
import { useGerente } from '@/context/GerenteContext';
import { useMercado } from '@/context/MercadoContext';
import { useOperacoes } from '@/context/OperacoesContext';
import { useSessao } from '@/context/SessaoContext';
import { usePainel } from '@/context/usePainel';

type Visao = 'clientes' | 'acoes';

export default function Banco() {
  const gerente = useGerente();
  const { sessao } = useSessao();
  const { acoes } = useMercado();
  const { exibir } = usePainel();
  const op = useOperacoes();
  const [visao, setVisao] = useState<Visao>('clientes');
  const [busca, setBusca] = useState('');

  if (!gerente.ativo) {
    return (
      <TelaDaAba>
        <View style={{ gap: 18, paddingHorizontal: 20, paddingTop: 50 }}>
          <IconeGrande d={IC.shield} icon={40} color={C.a700} sw={1.2} />
          <View>
            <Kicker>Área restrita</Kicker>
            <T h style={{ fontSize: 36, lineHeight: 40, marginTop: 4, marginBottom: 6 }}>Painel do gerente</T>
            <T style={{ fontSize: 15, color: C.n800 }}>Cadastre, edite, bloqueie e exclua clientes. Liste novas ações nas bolsas do globo.</T>
          </View>
          <Cta label="Entrar com código" onPress={() => op.abrir({ kind: 'pin', purpose: 'manager' })} />
          <T style={{ fontSize: 12, color: C.n700 }}>Código de demonstração: 0000</T>
        </View>
      </TelaDaAba>
    );
  }

  const q = busca.trim().toLowerCase();
  const todos = gerente.clientes;
  const clientes = q ? todos.filter(x => x.name.toLowerCase().includes(q) || x.email.includes(q)) : todos;
  const indicadores: [string, string][] = [
    ['Clientes', String(todos.length)],
    ['Custódia', brl(todos.reduce((a, x) => a + x.balance, 0)).replace(/,\d\d$/, '')],
    ['Bloqueadas', String(todos.filter(x => x.blocked).length)],
  ];
  const trocarVisao = (nova: Visao) => {
    setVisao(nova);
    setBusca('');
    if (nova === 'clientes') gerente.carregarBackoffice();
  };

  return (
    <TelaDaAba>
      <View style={{ gap: 16, paddingHorizontal: 20, paddingTop: 10 }}>
        <Cabecalho kicker="Backoffice · gerente" title="Banco Órbita" right={
          <Btn icon={IC.logout} onPress={() => gerente.sair('Modo gerente encerrado')}>Sair</Btn>} />
        <Blueprint style={{ flexDirection: 'row', gap: 1, backgroundColor: C.divider }}>
          {indicadores.map(([k, v], i) => (
            <Anim key={k} type="rise" delay={i * 70} duration={500} style={{ flex: 1, backgroundColor: C.bg, padding: 12 }}>
              <T style={{ fontSize: 11, color: C.n700 }}>{k}</T>
              <T h num numberOfLines={1} adjustsFontSizeToFit style={{ fontSize: 22, lineHeight: 26 }}>{v}</T>
            </Anim>
          ))}
        </Blueprint>
        <Seg options={[
          { label: 'Clientes', on: visao === 'clientes', pick: () => trocarVisao('clientes') },
          { label: 'Ações', on: visao === 'acoes', pick: () => trocarVisao('acoes') },
        ]} />
        {visao === 'clientes' ? (
          <Anim key="c" type="fadeUp" duration={350} style={{ gap: 12 }}>
            <View style={{ flexDirection: 'row', gap: 8 }}>
              <CampoDeBusca valor={busca} aoMudar={setBusca} placeholder="Buscar cliente" h={40} />
              <Btn kind="primary" icon={IC.plus} onPress={() => op.abrir({ kind: 'userForm', mode: 'new' }, { form: {} })}>Novo</Btn>
            </View>
            <View style={{ borderTopWidth: 1, borderTopColor: C.divider }}>
              {clientes.map((x, i) => (
                <Anim key={x.id} type="fadeUp" delay={i * 40} duration={350}>
                  <Row onPress={() => op.abrir({ kind: 'userForm', mode: 'admin', id: x.id }, { form: { name: x.name, email: x.email, cpf: x.cpf } })}
                    style={{ flexDirection: 'row', alignItems: 'center', gap: 12, paddingVertical: 12, paddingHorizontal: 4, borderBottomWidth: 1, borderBottomColor: C.divider }}>
                    <Avatar text={ini(x.name)} size={38} font={14} bg="transparent" />
                    <View style={{ flex: 1 }}>
                      <T m style={{ fontSize: 15 }}>{x.name}{x.id === sessao?.clienteId ? ' · você' : ''}</T>
                      <T numberOfLines={1} style={{ fontSize: 12, color: C.n700 }}>{x.email}</T>
                    </View>
                    <View style={{ alignItems: 'flex-end', gap: 2 }}>
                      <T h num style={{ fontSize: 15 }}>{brl(x.balance)}</T>
                      <Tag kind={x.blocked ? 'neutral' : 'accent'} style={{ paddingHorizontal: 7, paddingVertical: 1 }}>{x.blocked ? 'Bloqueada' : 'Ativa'}</Tag>
                    </View>
                  </Row>
                </Anim>
              ))}
            </View>
          </Anim>
        ) : (
          <Anim key="s" type="fadeUp" duration={350} style={{ gap: 12 }}>
            <Cta label="Cadastrar nova ação" icon={IC.plus} h={48} size={16} onPress={() => op.abrir({ kind: 'stockForm', edit: false }, { form: { ex: 'B3' } })} />
            <View style={{ borderTopWidth: 1, borderTopColor: C.divider }}>
              {acoes.map(st => {
                const x = exibir(st);
                return (
                  <Row key={x.ticker} onPress={x.edit} style={{ flexDirection: 'row', alignItems: 'center', gap: 12, paddingVertical: 11, paddingHorizontal: 4, borderBottomWidth: 1, borderBottomColor: C.divider }}>
                    <View style={{ flex: 1 }}>
                      <T h style={{ fontSize: 17 }}>{x.ticker} <T style={{ fontSize: 11, color: C.n700 }}>{x.exCode} · {x.sector}</T></T>
                      <T numberOfLines={1} style={{ fontSize: 12, color: C.n700 }}>{x.name}</T>
                    </View>
                    <T h num style={{ fontSize: 15 }}>{x.priceStr}</T>
                    <Icon d={IC.pencil} size={16} color={C.n600} />
                  </Row>
                );
              })}
            </View>
          </Anim>
        )}
      </View>
    </TelaDaAba>
  );
}
