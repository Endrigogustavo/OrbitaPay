import { useRef } from 'react';
import { ScrollView, View } from 'react-native';
import Globo from '@/components/Globo';
import { Chip, LinhaDeAtivo } from '@/components/ListaDeAtivos';
import { TelaDaAba, type RolagemDaAba } from '@/components/TelaDaAba';
import { Cabecalho } from '@/components/Titulos';
import { Blueprint, Kicker, T, Tag } from '@/components/ui';
import { EX, EXM } from '@/constants/bolsas';
import { brl, fh2 } from '@/constants/formatacao';
import { C } from '@/constants/tema';
import { useMercado } from '@/context/MercadoContext';
import { usePainel } from '@/context/usePainel';

export default function GloboDosMercados() {
  const { acoes, infos, bolsaSelecionada, voo, selecionarBolsa } = useMercado();
  const { exibir } = usePainel();
  const rolagem = useRef<RolagemDaAba>(null);

  const selE = EXM[bolsaSelecionada] || EX[0], selI = infos[selE.code];
  const daBolsa = acoes.filter(x => x.ex === selE.code);
  const abertas = EX.filter(e => infos[e.code].open).length;

  return (
    <TelaDaAba rolagem={rolagem}>
      <View style={{ paddingHorizontal: 20, paddingTop: 10 }}>
        <Cabecalho kicker="Terminal global" title="Mercados no mundo" right={
          <View style={{ alignItems: 'flex-end' }}>
            <T h num style={{ fontSize: 20 }}>{abertas + '/' + EX.length}</T>
            <T style={{ fontSize: 12, color: C.n700 }}>bolsas abertas</T>
          </View>} />
      </View>
      <View style={{ marginTop: 4 }}>
        <Globo selEx={bolsaSelecionada} exN={voo} openKey={EX.map(e => (infos[e.code].open ? '1' : '0')).join('')} onSelect={selecionarBolsa} scrollRef={rolagem} />
      </View>
      <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ gap: 6, paddingHorizontal: 20, paddingTop: 4, paddingBottom: 14 }}>
        {EX.map(e => <Chip key={e.code} label={e.code} on={e.code === bolsaSelecionada} onPress={() => selecionarBolsa(e.code)} dot={!!infos[e.code].open} />)}
      </ScrollView>
      <View style={{ paddingHorizontal: 20 }}>
        <Blueprint key={voo} anim={voo % 2 ? 'fadeUp' : 'rise'} style={{ padding: 16 }}>
          <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start', gap: 12 }}>
            <View style={{ flex: 1 }}>
              <Kicker>{selE.city} · {selE.cur}</Kicker>
              <T h style={{ fontSize: 24, lineHeight: 27 }}>{selE.full}</T>
            </View>
            <View style={{ alignItems: 'flex-end' }}>
              <T h num style={{ fontSize: 24, lineHeight: 27 }}>{selI.time}</T>
              <Tag kind={selI.open ? 'accent' : 'neutral'}>{selI.open ? 'Aberta' : 'Fechada'}</Tag>
            </View>
          </View>
          <View style={{ flexDirection: 'row', flexWrap: 'wrap', columnGap: 16, marginTop: 10, marginBottom: 4 }}>
            <T num style={{ fontSize: 12, color: C.n700 }}>{Math.abs(selE.lat).toFixed(2) + '°' + (selE.lat < 0 ? 'S' : 'N') + ' ' + Math.abs(selE.lon).toFixed(2) + '°' + (selE.lon < 0 ? 'W' : 'E')}</T>
            <T num style={{ fontSize: 12, color: C.n700 }}>{fh2(selE.o) + '–' + fh2(selE.c) + ' local'}</T>
            <T num style={{ fontSize: 12, color: C.n700 }}>{selE.cur === 'BRL' ? 'Moeda local' : '1 ' + selE.cur + ' = ' + brl(daBolsa.length ? daBolsa[0].fx : selE.fx)}</T>
          </View>
          {daBolsa.map((st, i) => <LinhaDeAtivo key={st.ticker} acao={exibir(st)} indice={i} compacta />)}
          {daBolsa.length === 0 && <T style={{ borderTopWidth: 1, borderTopColor: C.divider, paddingTop: 10, fontSize: 13, color: C.n700 }}>Nenhuma ação listada nesta bolsa. Cadastre uma na aba Banco.</T>}
        </Blueprint>
      </View>
    </TelaDaAba>
  );
}
