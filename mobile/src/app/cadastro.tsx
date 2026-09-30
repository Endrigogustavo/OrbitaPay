import { useState } from 'react';
import { KeyboardAvoidingView, ScrollView, View } from 'react-native';
import { router } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Anim, Btn, Cta, ErrBox, Field, Icon, Input, Kicker, Shake, T } from '@/components/ui';
import { maskCpf } from '@/constants/formatacao';
import { IC } from '@/constants/icones';
import { C } from '@/constants/tema';
import { useSessao } from '@/context/SessaoContext';
import { comoErroDaApi } from '@/integration/http';

const soDigitos = (v: string) => v.replace(/\D/g, '').slice(0, 4);

export default function Cadastro() {
  const { api, entrar } = useSessao();
  const insets = useSafeAreaInsets();
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [cpf, setCpf] = useState('');
  const [pin, setPin] = useState('');
  const [pin2, setPin2] = useState('');
  const [erro, setErro] = useState('');
  const [tremor, setTremor] = useState(0);
  const [ocupado, setOcupado] = useState(false);

  const falhar = (mensagem: string) => {
    setErro(mensagem);
    setTremor(n => n + 1);
  };
  const alterar = (definir: (v: string) => void, tratar?: (v: string) => string) => (v: string) => {
    definir(tratar ? tratar(v) : v);
    setErro('');
  };

  const cadastrar = async () => {
    if (pin.length !== 4) return falhar('Crie um PIN de 4 dígitos');
    if (pin !== pin2) return falhar('Os PINs não conferem');
    if (ocupado) return;
    setOcupado(true);
    try {
      const cliente = await api.clientes.cadastrar({ nome, email, cpf, pin });
      await entrar(cliente.email, pin, 'Conta criada · bem-vindo(a) à Órbita');
    } catch (falha) {
      falhar(comoErroDaApi(falha).message);
    } finally {
      setOcupado(false);
    }
  };

  return (
    <KeyboardAvoidingView behavior="padding" style={{ flex: 1, paddingTop: insets.top, backgroundColor: C.bg }}>
      <Anim type="fadeUp" style={{ flex: 1 }}>
        <ScrollView keyboardShouldPersistTaps="handled" contentContainerStyle={{ gap: 18, paddingHorizontal: 28, paddingTop: 12, paddingBottom: 40 }}>
          <Btn onPress={() => router.back()} style={{ width: 40, height: 40, borderRadius: 20, paddingHorizontal: 0, alignSelf: 'flex-start' }}><Icon d={IC.chevronLeft} size={20} /></Btn>
          <View>
            <Kicker style={{ marginBottom: 6 }}>Nova conta · 1 minuto</Kicker>
            <T h style={{ fontSize: 40, lineHeight: 42 }}>Abra sua conta Órbita.</T>
          </View>
          <Shake n={tremor} style={{ gap: 12 }}>
            <Field label="Nome completo"><Input value={nome} onChangeText={alterar(setNome)} placeholder="Maria Souza" autoCapitalize="words" style={{ minHeight: 46, fontSize: 16 }} /></Field>
            <Field label="E-mail"><Input value={email} onChangeText={alterar(setEmail)} placeholder="maria@email.com" keyboardType="email-address" autoCapitalize="none" style={{ minHeight: 46, fontSize: 16 }} /></Field>
            <Field label="CPF"><Input value={cpf} onChangeText={alterar(setCpf, maskCpf)} placeholder="000.000.000-00" keyboardType="number-pad" style={{ minHeight: 46, fontSize: 16 }} /></Field>
            <View style={{ flexDirection: 'row', gap: 12 }}>
              <Field label="PIN" style={{ flex: 1 }}><Input pin value={pin} onChangeText={alterar(setPin, soDigitos)} placeholder="••••" style={{ minHeight: 46 }} /></Field>
              <Field label="Confirmar PIN" style={{ flex: 1 }}><Input pin value={pin2} onChangeText={alterar(setPin2, soDigitos)} placeholder="••••" style={{ minHeight: 46 }} /></Field>
            </View>
            <ErrBox soft msg={erro} />
            <Cta label={ocupado ? 'Criando…' : 'Criar conta'} onPress={cadastrar} style={{ marginTop: 6 }} />
            <T style={{ fontSize: 12, color: C.n700 }}>Seu PIN autoriza saques e ordens na bolsa. Três erros bloqueiam a conta.</T>
          </Shake>
        </ScrollView>
      </Anim>
    </KeyboardAvoidingView>
  );
}
