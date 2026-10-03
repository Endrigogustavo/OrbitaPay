import { useEffect } from 'react';
import { BackHandler, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import type { Folha as FolhaAtual } from '@/@types/orbita';
import { useOperacoes } from '@/context/OperacoesContext';
import { useSessao } from '@/context/SessaoContext';
import { Folha } from '../Folha';
import { Shake } from '../ui';
import { FolhaDePin } from './FolhaDePin';
import { FolhaDeValor } from './FolhaDeValor';
import { FolhaDoAtivo } from './FolhaDoAtivo';
import { FolhaAlterarPin, FolhaCadastroDeAtivo, FolhaDoCliente } from './FolhasDeFormulario';
import { FolhaContaBloqueada, FolhaDeConfirmacao, FolhaDeSucesso, FolhaProcessando } from './FolhasDeResultado';

function Conteudo({ folha }: { folha: FolhaAtual }) {
  switch (folha.kind) {
    case 'amount': return <FolhaDeValor folha={folha} />;
    case 'pin': return <FolhaDePin folha={folha} />;
    case 'processing': return <FolhaProcessando folha={folha} />;
    case 'success': return <FolhaDeSucesso folha={folha} />;
    case 'locked': return <FolhaContaBloqueada />;
    case 'stock': return <FolhaDoAtivo folha={folha} />;
    case 'userForm': return <FolhaDoCliente folha={folha} />;
    case 'stockForm': return <FolhaCadastroDeAtivo folha={folha} />;
    case 'changePin': return <FolhaAlterarPin />;
    case 'confirm': return <FolhaDeConfirmacao folha={folha} />;
  }
}

export function CamadaDeFolhas() {
  const { folha, fechando, fechar, tremor } = useOperacoes();
  const { usuario } = useSessao();
  const insets = useSafeAreaInsets();

  useEffect(() => {
    if (!folha) return;
    const assinatura = BackHandler.addEventListener('hardwareBackPress', () => { fechar(); return true; });
    return () => assinatura.remove();
  }, [folha, fechar]);

  if (!folha || (folha.kind === 'stock' && !usuario)) return null;
  const chave = folha.kind + (folha.kind === 'pin' ? folha.purpose : '');
  return (
    <Folha closing={fechando} onClose={fechar} bottom={insets.bottom}>
      <Shake n={tremor}><View key={chave}><Conteudo folha={folha} /></View></Shake>
    </Folha>
  );
}
