import { View } from 'react-native';
import type { MetodoDeDeposito } from '@/@types/api';
import type { FolhaDo } from '@/@types/orbita';
import { brl } from '@/constants/formatacao';
import { C } from '@/constants/tema';
import { useOperacoes } from '@/context/OperacoesContext';
import { useSessao } from '@/context/SessaoContext';
import { TituloDaFolha } from '../Titulos';
import { Anim, Blink, Btn, Cta, ErrBox, Seg, T } from '../ui';
import { Teclado } from './Teclado';

const METODOS: MetodoDeDeposito[] = ['PIX', 'TED', 'Boleto'];

export function FolhaDeValor({ folha }: { folha: FolhaDo<'amount'> }) {
  const { usuario } = useSessao();
  const op = useOperacoes();
  const v = parseInt(op.valor || '0', 10) / 100, dep = folha.op === 'dep';
  return (
    <>
      <Anim type="fadeUp" duration={350} style={{ gap: 14 }}>
        <TituloDaFolha kicker={dep ? 'Entrada' : 'Saída · requer PIN'} title={dep ? 'Depositar' : 'Sacar'} />
        {dep && <Seg options={METODOS.map(m => ({ label: m, on: folha.method === m, pick: () => op.definirMetodo(m) }))} />}
        <View style={{ flexDirection: 'row', alignItems: 'baseline', gap: 8, borderBottomWidth: 1, borderBottomColor: C.divider, paddingTop: 8, paddingBottom: 12 }}>
          <T h style={{ fontSize: 24, color: C.n600 }}>R$</T>
          <T h num style={{ fontSize: 56, lineHeight: 60, color: v > 0 ? C.text : C.n500 }}>{brl(v).replace(/^R\$\s?/, '')}</T>
          <Blink />
        </View>
        <View style={{ flexDirection: 'row', justifyContent: 'space-between' }}>
          <T style={{ fontSize: 13, color: C.n700 }}>Saldo {brl(usuario ? usuario.balance : 0)}</T>
          <T style={{ fontSize: 13, color: C.n700 }}>{dep ? 'Máx. R$ 50 mil' : 'Máx. R$ 5 mil / saque'}</T>
        </View>
        <View style={{ flexDirection: 'row', gap: 6 }}>
          {[50, 100, 500, 1000].map(q => <Btn key={q} onPress={() => op.somarAoValor(q)} style={{ flex: 1 }}>{'+' + q}</Btn>)}
        </View>
      </Anim>
      {!!op.erro && <View style={{ marginTop: 12 }}><ErrBox icon msg={op.erro} /></View>}
      <Teclado tipo="valor" aoPressionar={op.pressionar} />
      <Cta label={op.ocupado ? 'Processando…' : dep ? 'Depositar ' + brl(v) : 'Continuar'} h={54} onPress={op.confirmarValor} style={{ marginTop: 16 }} />
    </>
  );
}
