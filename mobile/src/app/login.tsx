import { useState } from 'react';
import { KeyboardAvoidingView, ScrollView, View } from 'react-native';
import { router } from 'expo-router';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { Anim, Avatar, Btn, Cta, ErrBox, Field, Input, Kicker, Logo, Shake, T } from '@/components/ui';
import { DEMO } from '@/constants/demonstracao';
import { ini } from '@/constants/formatacao';
import { C } from '@/constants/tema';
import { useSessao } from '@/context/SessaoContext';
import { URL_DO_GATEWAY } from '@/integration/configuracao';
import { comoErroDaApi } from '@/integration/http';

export default function Login() {
  const { entrar } = useSessao();
  const insets = useSafeAreaInsets();
  const [email, setEmail] = useState('');
  const [pin, setPin] = useState('');
  const [erro, setErro] = useState('');
  const [tremor, setTremor] = useState(0);
  const [ocupado, setOcupado] = useState(false);

  const falhar = (mensagem: string) => {
    setErro(mensagem);
    setTremor(n => n + 1);
    setPin('');
  };

  const login = async () => {
    const e = email.trim().toLowerCase();
    if (!e) return falhar('Informe seu e-mail');
    if (pin.length !== 4) return falhar('O PIN tem 4 dígitos');
    if (ocupado) return;
    setOcupado(true);
    try {
      await entrar(e, pin);
    } catch (falha) {
      falhar(comoErroDaApi(falha).message);
    } finally {
      setOcupado(false);
    }
  };

  return (
    <KeyboardAvoidingView behavior="padding" style={{ flex: 1, paddingTop: insets.top, backgroundColor: C.bg }}>
      <Anim type="fadeUp" style={{ flex: 1 }}>
        <ScrollView keyboardShouldPersistTaps="handled" contentContainerStyle={{ flexGrow: 1, gap: 22, paddingHorizontal: 28, paddingTop: 28, paddingBottom: 40 }}>
          <View style={{ flexDirection: 'row', alignItems: 'center', gap: 10 }}>
            <Logo />
            <T h style={{ fontSize: 20, letterSpacing: 1.6, color: C.a800 }}>ÓRBITA</T>
          </View>
          <View style={{ marginTop: 36 }}>
            <Kicker style={{ marginBottom: 8 }}>Acesso seguro</Kicker>
            <T h style={{ fontSize: 48, lineHeight: 50 }}>Entre na sua órbita.</T>
          </View>
          <Shake n={tremor} style={{ gap: 14 }}>
            <Field label="E-mail">
              <Input value={email} onChangeText={v => { setEmail(v); setErro(''); }} placeholder="voce@orbita.com" keyboardType="email-address" autoCapitalize="none" style={{ minHeight: 48, fontSize: 16 }} />
            </Field>
            <Field label="PIN de 4 dígitos">
              <Input pin value={pin} onChangeText={v => { setPin(v.replace(/\D/g, '').slice(0, 4)); setErro(''); }} placeholder="••••" onSubmitEditing={login} style={{ minHeight: 48, fontSize: 20, letterSpacing: 8 }} />
            </Field>
            <ErrBox soft msg={erro} />
            <Cta label={ocupado ? 'Entrando…' : 'Entrar'} onPress={login} style={{ marginTop: 4 }} />
          </Shake>
          <View style={{ gap: 10 }}>
            <T style={{ fontSize: 12, color: C.n700 }}>Entrar rápido</T>
            <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
              {DEMO.map(x => (
                <Btn key={x.email} onPress={() => { setEmail(x.email); setPin(x.pin); setErro(''); }} style={{ gap: 8 }}>
                  <Avatar text={ini(x.nome)} size={22} font={11} border={false} />
                  <T h style={{ fontSize: 14 }}>{x.nome}</T>
                </Btn>
              ))}
            </View>
            <T style={{ fontSize: 12, color: C.n600 }}>Demo: carla@orbita.com está bloqueada · código do gerente 0000</T>
          </View>
          <View style={{ marginTop: 'auto', gap: 6 }}>
            <View style={{ flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', borderTopWidth: 1, borderTopColor: C.divider, paddingTop: 16 }}>
              <T style={{ fontSize: 14, color: C.n700 }}>Ainda não é cliente?</T>
              <Btn kind="ghost" onPress={() => router.push('/cadastro')}><T h style={{ fontSize: 16, color: C.a700 }}>Criar conta →</T></Btn>
            </View>
            <T num style={{ fontSize: 11, color: C.n600 }}>Gateway: {URL_DO_GATEWAY}</T>
          </View>
        </ScrollView>
      </Anim>
    </KeyboardAvoidingView>
  );
}
