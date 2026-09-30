import { Pressable, View } from 'react-native';
import type { FolhaDo } from '@/@types/orbita';
import { EX, EXM } from '@/constants/bolsas';
import { brl } from '@/constants/formatacao';
import { IC } from '@/constants/icones';
import { C } from '@/constants/tema';
import { useGerente } from '@/context/GerenteContext';
import { useOperacoes } from '@/context/OperacoesContext';
import { TituloDaFolha } from '../Titulos';
import { Anim, Cta, ErrBox, Field, InfoGrid, Input, Plain, T } from '../ui';

/** Edição dos próprios dados, cadastro de cliente pelo gerente ou edição de cliente no backoffice. */
export function FolhaDoCliente({ folha }: { folha: FolhaDo<'userForm'> }) {
  const op = useOperacoes();
  const { clientes } = useGerente();
  const fv = op.form, tu = folha.id ? clientes.find(x => x.id === folha.id) : undefined;
  const isNew = folha.mode === 'new', isAdmin = folha.mode === 'admin';
  return (
    <Anim type="fadeUp" duration={350} style={{ gap: 12 }}>
      <TituloDaFolha kicker={folha.mode === 'self' ? 'Perfil' : 'Backoffice · cliente'} title={isNew ? 'Novo cliente' : folha.mode === 'self' ? 'Editar dados' : tu ? tu.name : ''} />
      {isAdmin && tu && <InfoGrid cols={3} pad={8} items={[['Saldo', brl(tu.balance)], ['Conta', tu.acct], ['Status', tu.blocked ? 'Bloqueada' : 'Ativa']]} />}
      <Field label="Nome completo"><Input value={fv.name || ''} onChangeText={op.campo.name} autoCapitalize="words" /></Field>
      <Field label="E-mail"><Input value={fv.email || ''} onChangeText={op.campo.email} keyboardType="email-address" autoCapitalize="none" /></Field>
      {folha.mode !== 'self' && <Field label="CPF"><Input value={fv.cpf || ''} onChangeText={op.campo.cpf} placeholder="000.000.000-00" keyboardType="number-pad" /></Field>}
      {isNew && (
        <View style={{ flexDirection: 'row', gap: 12 }}>
          <Field label="PIN inicial" style={{ flex: 1 }}><Input pin value={fv.pin || ''} onChangeText={op.campo.pin} placeholder="••••" /></Field>
          <Field label="Saldo inicial (R$)" style={{ flex: 1 }}><Input value={fv.balance || ''} onChangeText={op.campo.balance} placeholder="0,00" keyboardType="decimal-pad" /></Field>
        </View>
      )}
      <ErrBox msg={op.erro} />
      <Cta label={isNew ? 'Cadastrar cliente' : 'Salvar alterações'} icon={IC.check} onPress={op.salvarCliente} style={{ marginTop: 4 }} />
      {isAdmin && tu && (
        <View style={{ flexDirection: 'row', gap: 8 }}>
          <Plain style={{ flex: 1 }} icon={tu.blocked ? IC.unlock : IC.lock} label={tu.blocked ? 'Desbloquear' : 'Bloquear'} onPress={() => op.alternarBloqueio(tu)} />
          <Plain style={{ flex: 1 }} icon={IC.trash} label="Excluir cliente" onPress={() => op.confirmar({ d: IC.trash, title: 'Excluir ' + tu.name.split(' ')[0] + '?', body: 'O cliente, saldo de ' + brl(tu.balance) + ' e todo o histórico serão removidos do banco.', label: 'Excluir cliente', hasGo: true, cancel: 'Cancelar',
            go: () => op.removerCliente(tu) })} />
        </View>
      )}
    </Anim>
  );
}

/** Listagem ou edição de uma ação em uma das bolsas do globo. */
export function FolhaCadastroDeAtivo({ folha }: { folha: FolhaDo<'stockForm'> }) {
  const op = useOperacoes();
  const fv = op.form, cur = (EXM[fv.ex || 'B3'] || EX[0]).cur;
  const rows = [0, 4, 8].map(i => EX.slice(i, i + 4));
  return (
    <Anim type="fadeUp" duration={350} style={{ gap: 12 }}>
      <TituloDaFolha kicker="Listagem · backoffice" title={folha.edit ? 'Editar ' + fv.ticker : 'Cadastrar ação'} />
      <View style={{ flexDirection: 'row', gap: 12 }}>
        <Field label="Ticker" style={{ flex: 1 }}><Input value={fv.ticker || ''} onChangeText={op.campo.ticker} placeholder="ORBT3" autoCapitalize="characters" editable={!folha.edit} style={{ fontFamily: 'BarlowCondensed_600SemiBold', fontSize: 16, letterSpacing: 0.6 }} /></Field>
        <Field label={'Preço (' + cur + ')'} style={{ flex: 1 }}><Input value={fv.price || ''} onChangeText={op.campo.price} placeholder="10,00" keyboardType="decimal-pad" /></Field>
      </View>
      <Field label="Empresa"><Input value={fv.sname || ''} onChangeText={op.campo.sname} placeholder="Órbita Holding S.A." /></Field>
      <Field label="Setor"><Input value={fv.sector || ''} onChangeText={op.campo.sector} placeholder="Financeiro" /></Field>
      <Field label="Bolsa · aparece no globo">
        <View style={{ gap: 1, backgroundColor: C.divider, borderWidth: 1, borderColor: C.divider, borderRadius: 12, overflow: 'hidden' }}>
          {rows.map((r, i) => (
            <View key={i} style={{ flexDirection: 'row', gap: 1 }}>
              {r.map(e => {
                const on = (fv.ex || 'B3') === e.code;
                return (
                  <Pressable key={e.code} onPress={() => op.definirBolsaDoFormulario(e.code)} style={{ flex: 1, paddingVertical: 9, alignItems: 'center', backgroundColor: on ? C.accent : C.bg }}>
                    <T h style={{ fontSize: 14, color: on ? C.bg : C.text }}>{e.code}</T>
                  </Pressable>
                );
              })}
            </View>
          ))}
        </View>
      </Field>
      <ErrBox msg={op.erro} />
      <Cta label={folha.edit ? 'Salvar ação' : 'Listar na bolsa'} icon={IC.check} onPress={op.salvarAtivo} style={{ marginTop: 4 }} />
      {folha.edit && <Plain icon={IC.trash} label="Remover da bolsa" onPress={() => {
        const t = fv.ticker || '';
        op.confirmar({ d: IC.trash, title: 'Remover ' + t + '?', body: 'A ação sai do mercado e do globo. Posições de clientes deixam de ser exibidas.', label: 'Remover ação', hasGo: true, cancel: 'Cancelar',
          go: () => op.removerAtivo(t) });
      }} />}
    </Anim>
  );
}

export function FolhaAlterarPin() {
  const op = useOperacoes();
  const fv = op.form;
  return (
    <Anim type="fadeUp" duration={350} style={{ gap: 12 }}>
      <TituloDaFolha kicker="Segurança" title="Alterar PIN" />
      <Field label="PIN atual"><Input pin value={fv.pinOld || ''} onChangeText={op.campo.pinOld} placeholder="••••" /></Field>
      <View style={{ flexDirection: 'row', gap: 12 }}>
        <Field label="Novo PIN" style={{ flex: 1 }}><Input pin value={fv.pin || ''} onChangeText={op.campo.pin} placeholder="••••" /></Field>
        <Field label="Confirmar" style={{ flex: 1 }}><Input pin value={fv.pin2 || ''} onChangeText={op.campo.pin2} placeholder="••••" /></Field>
      </View>
      <ErrBox msg={op.erro} />
      <Cta label="Salvar novo PIN" icon={IC.check} onPress={op.salvarPin} style={{ marginTop: 4 }} />
    </Anim>
  );
}
