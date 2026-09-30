import { useState } from 'react';
import { ScrollView, View } from 'react-native';
import { CampoDeBusca, Chip, LinhaDeAtivo } from '@/components/ListaDeAtivos';
import { TelaDaAba } from '@/components/TelaDaAba';
import { Cabecalho } from '@/components/Titulos';
import { Blueprint, Kicker, Row, T } from '@/components/ui';
import { EX } from '@/constants/bolsas';
import { brl, pct } from '@/constants/formatacao';
import { C, corDaVariacao } from '@/constants/tema';
import { useMercado } from '@/context/MercadoContext';
import { useOperacoes } from '@/context/OperacoesContext';
import { usePainel } from '@/context/usePainel';

const FILTROS = ['Todas', 'Carteira', ...EX.map(e => e.code)];

export default function Mercado() {
  const { posicoes, valorDaCarteira: pv, custoDaCarteira: pc, listaDePosicoes, exibir } = usePainel();
  const { acoes } = useMercado();
  const { ocultarValores } = useOperacoes();
  const [busca, setBusca] = useState('');
  const [filtro, setFiltro] = useState('Todas');

  const q = busca.trim().toLowerCase();
  let lista = acoes;
  if (filtro === 'Carteira') lista = lista.filter(x => posicoes[x.ticker]?.qty);
  else if (filtro !== 'Todas') lista = lista.filter(x => x.ex === filtro);
  if (q) lista = lista.filter(x => x.ticker.toLowerCase().includes(q) || x.name.toLowerCase().includes(q));

  return (
    <TelaDaAba>
      <View style={{ gap: 16, paddingHorizontal: 20, paddingTop: 10 }}>
        <Cabecalho dot kicker="Mercado · ao vivo" title="Bolsa global" />
        <Blueprint anim="rise" style={{ padding: 16 }}>
          <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'baseline' }}>
            <Kicker>Sua carteira</Kicker>
            {!!pc && <T num style={{ fontSize: 13, color: corDaVariacao(pv >= pc) }}>{(pv >= pc ? '▲ ' : '▼ ') + pct((pv / pc - 1) * 100) + ' · ' + brl(pv - pc)}</T>}
          </View>
          <T h num style={{ fontSize: 38, lineHeight: 42, marginTop: 6 }}>{ocultarValores ? 'R$ ••••' : brl(pv)}</T>
          {listaDePosicoes.length > 0 ? (
            <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ gap: 8, marginTop: 12 }}>
              {listaDePosicoes.map(h => (
                <Row key={h.ticker} onPress={h.on} style={{ gap: 2, paddingVertical: 8, paddingHorizontal: 12, borderRadius: 12, borderWidth: 1, borderColor: C.divider }}>
                  <T h style={{ fontSize: 15 }}>{h.ticker} <T style={{ fontSize: 12, color: C.n700 }}>{h.qty}</T></T>
                  <T num style={{ fontSize: 12 }}>{h.val} · {h.pl}</T>
                </Row>
              ))}
            </ScrollView>
          ) : <T style={{ fontSize: 13, color: C.n700, marginTop: 6 }}>Nenhuma ação ainda. Toque em um ativo para comprar.</T>}
        </Blueprint>
        <CampoDeBusca valor={busca} aoMudar={setBusca} placeholder="Buscar ticker ou empresa" h={44} />
        <ScrollView horizontal showsHorizontalScrollIndicator={false} style={{ marginHorizontal: -20 }} contentContainerStyle={{ gap: 6, paddingHorizontal: 20 }}>
          {FILTROS.map(f => <Chip key={f} label={f} on={filtro === f} onPress={() => setFiltro(f)} />)}
        </ScrollView>
        <View style={{ borderTopWidth: 1, borderTopColor: C.divider }}>
          {lista.map((st, i) => <LinhaDeAtivo key={st.ticker} acao={exibir(st)} indice={i} />)}
          {lista.length === 0 && <T style={{ paddingVertical: 28, fontSize: 14, color: C.n700 }}>{acoes.length ? 'Nenhum ativo encontrado.' : 'Carregando cotações…'}</T>}
        </View>
      </View>
    </TelaDaAba>
  );
}
