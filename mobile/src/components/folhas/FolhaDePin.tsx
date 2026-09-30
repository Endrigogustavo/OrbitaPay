import { View } from 'react-native';
import type { FolhaDo } from '@/@types/orbita';
import { MAX_ATTEMPTS } from '@/constants/demonstracao';
import { brl } from '@/constants/formatacao';
import { C } from '@/constants/tema';
import { useOperacoes } from '@/context/OperacoesContext';
import { useSessao } from '@/context/SessaoContext';
import { TituloDaFolha } from '../Titulos';
import { Anim, ErrBox, T } from '../ui';
import { Teclado } from './Teclado';

/** Título, kicker e subtítulo conforme o que o PIN vai autorizar. */
function textos(folha: FolhaDo<'pin'>): [string, string, string] {
  switch (folha.purpose) {
    case 'withdraw': return ['Autorizar saque', 'Confirme com seu PIN', 'Saque de ' + brl(folha.v)];
    case 'trade': return ['Confirmar ordem', 'Assinatura digital', (folha.mode === 'buy' ? 'Compra' : 'Venda') + ' de ' + folha.qty + ' ' + folha.ticker + ' · ' + brl(folha.total)];
    case 'manager': return ['Código do gerente', 'Acesso restrito', 'Digite o código de 4 dígitos · demo 0000'];
    case 'unblock': return ['Desbloquear conta', 'Segurança', 'Confirme seu PIN para reativar saques e ordens'];
  }
}

export function FolhaDePin({ folha }: { folha: FolhaDo<'pin'> }) {
  const { usuario } = useSessao();
  const op = useOperacoes();
  const fails = usuario?.fails || 0, max = usuario?.maxFails || MAX_ATTEMPTS;
  const [titulo, kicker, sub] = textos(folha);
  return (
    <>
      <Anim type="fadeUp" duration={350} style={{ gap: 16 }}>
        <TituloDaFolha kicker={kicker} title={titulo} sub={sub} />
        <View style={{ flexDirection: 'row', gap: 10 }}>
          {[0, 1, 2, 3].map(i => {
            const filled = i < op.pin.length;
            return (
              <View key={i} style={{ flex: 1, height: 62, alignItems: 'center', justifyContent: 'center', borderRadius: 14, borderWidth: 1,
                borderColor: i === op.pin.length ? C.accent : C.divider, backgroundColor: filled ? C.a100 : 'transparent' }}>
                {filled && <Anim type="pop" duration={300}><View style={{ width: 14, height: 14, borderRadius: 7, backgroundColor: C.a800 }} /></Anim>}
              </View>
            );
          })}
        </View>
        {folha.purpose !== 'manager' && (
          <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
            <T style={{ fontSize: 12, color: C.n700 }}>Tentativas</T>
            {Array.from({ length: max }, (_, i) => <View key={i} style={{ width: 10, height: 10, borderWidth: 1, borderColor: C.a700, backgroundColor: i < fails ? C.a700 : 'transparent' }} />)}
          </View>
        )}
      </Anim>
      {!!op.erro && <View style={{ marginTop: 12 }}><ErrBox icon msg={op.erro} /></View>}
      <Teclado tipo="pin" aoPressionar={op.pressionar} />
    </>
  );
}
