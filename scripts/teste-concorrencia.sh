#!/usr/bin/env bash
set -u

GATEWAY="${GATEWAY:-http://localhost:8080}"
TICKER="${TICKER:-ORBT3}"
ORDENS="${ORDENS:-20}"
SAQUES="${SAQUES:-10}"
VALOR_SAQUE="${VALOR_SAQUE:-500}"
RELATORIO="${SAIDA:-teste-concorrencia-$(date +%Y%m%d-%H%M%S).txt}"
TMP="$(mktemp -d)"
LARGADA="$TMP/largada"
: > "$RELATORIO"

out() {
  printf "$@"
  printf "$@" >> "$RELATORIO"
}

campo() {
  sed -n "s/.*\"$1\":\"\\{0,1\\}\\([^\",}]*\\).*/\\1/p" | head -1
}

entrar() {
  curl -s -X POST "$GATEWAY/api/autenticacao/clientes" -H 'Content-Type: application/json' \
    -d "{\"email\":\"$1\",\"pin\":\"$2\"}" | campo token
}

assinar() {
  curl -s -X POST "$GATEWAY/api/clientes/me/assinaturas" -H 'Content-Type: application/json' \
    -H "Authorization: Bearer $1" -d "{\"pin\":\"$2\"}" | campo token
}

aguardar_largada() {
  while [ ! -f "$LARGADA" ]; do sleep 0.01; done
}

disparar() {
  local nome="$1" total="$2"; shift 2
  rm -f "$LARGADA"
  for i in $(seq 1 "$total"); do
    (
      aguardar_largada
      local inicio fim codigo
      inicio=$(date +%s%3N)
      codigo=$(curl -s -o "$TMP/${nome}_corpo_$i" -w '%{http_code}' "$@")
      fim=$(date +%s%3N)
      printf '%s|%s|%s\n' "$i" "$codigo" "$((fim - inicio))" > "$TMP/${nome}_res_$i"
    ) &
  done
  sleep 1
  touch "$LARGADA"
  wait
}

out 'Teste de concorrencia - OrbitaPay (lock pessimista no MongoDB)\n'
out 'Data: %s\n' "$(date '+%Y-%m-%d %H:%M:%S')"
out 'Gateway: %s\n' "$GATEWAY"
out -- '=============================================\n'

SESSAO_ANA=$(entrar ana@orbita.com 1234)
ASSINATURA_ANA=$(assinar "$SESSAO_ANA" 1234)
if [ -z "$SESSAO_ANA" ] || [ -z "$ASSINATURA_ANA" ]; then
  out 'Falha ao autenticar a cliente Ana. O ecossistema esta de pe?\n'
  exit 1
fi

ANTES=$(curl -s "$GATEWAY/api/ofertas/$TICKER")
out '\n[1] %s ordens simultaneas de COMPRA de 1 %s (negociacao-service, lock no AtivoNegociavel)\n' "$ORDENS" "$TICKER"
out 'Oferta antes: %s\n' "$ANTES"
out -- '---------------------------------------------\n'

disparar compra "$ORDENS" -X POST "$GATEWAY/api/ordens" -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $SESSAO_ANA" -H "X-Assinatura: $ASSINATURA_ANA" \
  -d "{\"ticker\":\"$TICKER\",\"tipo\":\"COMPRA\",\"quantidade\":1}"

ACEITAS=0
RECUSADAS=0
for arquivo in $(ls "$TMP"/compra_res_* | sort -t_ -k3 -n); do
  IFS='|' read -r ID CODIGO MS < "$arquivo"
  CORPO=$(tr -d '\n' < "$TMP/compra_corpo_$ID" | cut -c1-90)
  out '#%-3s HTTP %-4s %5sms  %s\n' "$ID" "$CODIGO" "$MS" "$CORPO"
  if [ "$CODIGO" = "202" ]; then ACEITAS=$((ACEITAS + 1)); else RECUSADAS=$((RECUSADAS + 1)); fi
done

sleep 3
DEPOIS=$(curl -s "$GATEWAY/api/ofertas/$TICKER")
EXECUTADAS=$(curl -s "$GATEWAY/api/ordens" -H "Authorization: Bearer $SESSAO_ANA" | grep -o "\"ticker\":\"$TICKER\"[^}]*\"status\":\"EXECUTADA\"" | wc -l)
out -- '---------------------------------------------\n'
out 'Aceitas (202): %s | Recusadas por falta de oferta: %s\n' "$ACEITAS" "$RECUSADAS"
out 'Ordens de %s executadas no historico da Ana: %s\n' "$TICKER" "$EXECUTADAS"
out 'Oferta depois: %s\n' "$DEPOIS"
out 'Se aceitas > quantidade emitida, a race condition esta confirmada.\n'

SESSAO_BRUNO=$(entrar bruno@orbita.com 4321)
ASSINATURA_BRUNO=$(assinar "$SESSAO_BRUNO" 4321)
SALDO_ANTES=$(curl -s "$GATEWAY/api/contas/me" -H "Authorization: Bearer $SESSAO_BRUNO" | campo saldo)

out '\n[2] %s saques simultaneos de R$ %s na conta do Bruno (contas-service, lock na Conta)\n' "$SAQUES" "$VALOR_SAQUE"
out 'Saldo antes: R$ %s\n' "$SALDO_ANTES"
out -- '---------------------------------------------\n'

disparar saque "$SAQUES" -X POST "$GATEWAY/api/contas/me/saques" -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $SESSAO_BRUNO" -H "X-Assinatura: $ASSINATURA_BRUNO" \
  -d "{\"valor\":$VALOR_SAQUE}"

APROVADOS=0
for arquivo in $(ls "$TMP"/saque_res_* | sort -t_ -k3 -n); do
  IFS='|' read -r ID CODIGO MS < "$arquivo"
  CORPO=$(tr -d '\n' < "$TMP/saque_corpo_$ID" | cut -c1-90)
  out '#%-3s HTTP %-4s %5sms  %s\n' "$ID" "$CODIGO" "$MS" "$CORPO"
  if [ "$CODIGO" = "200" ]; then APROVADOS=$((APROVADOS + 1)); fi
done

SALDO_DEPOIS=$(curl -s "$GATEWAY/api/contas/me" -H "Authorization: Bearer $SESSAO_BRUNO" | campo saldo)
out -- '---------------------------------------------\n'
out 'Saques aprovados: %s de %s\n' "$APROVADOS" "$SAQUES"
out 'Saldo depois: R$ %s (nunca pode ficar negativo)\n' "$SALDO_DEPOIS"

echo
echo "Relatorio salvo em: $RELATORIO"
rm -rf "$TMP"
