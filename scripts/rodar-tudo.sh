#!/usr/bin/env bash
set -u

RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
SERVICOS="auth clientes contas ativos negociacao carteira pagamentos relatorios gateway"
LIMPAR=0
UNITARIOS=1
MANTER=1

for argumento in "$@"; do
  case "$argumento" in
    --limpar) LIMPAR=1 ;;
    --sem-unitarios) UNITARIOS=0 ;;
    --derrubar) MANTER=0 ;;
    -h|--ajuda)
      echo "Uso: scripts/rodar-tudo.sh [--limpar] [--sem-unitarios] [--derrubar]"
      echo "  --limpar         apaga os dados (volumes) antes de subir"
      echo "  --sem-unitarios  pula os testes unitarios com Maven"
      echo "  --derrubar       derruba os containers no final"
      exit 0
      ;;
    *) echo "Opcao desconhecida: $argumento"; exit 2 ;;
  esac
done

cd "$RAIZ" || exit 1

RESULTADOS=()
FALHAS=0

etapa() {
  echo
  echo "=================================================================="
  echo " $1"
  echo "=================================================================="
}

registrar() {
  if [ "$2" -eq 0 ]; then
    RESULTADOS+=("[OK]    $1")
  else
    RESULTADOS+=("[FALHA] $1")
    FALHAS=$((FALHAS + 1))
  fi
}

if ! docker info > /dev/null 2>&1; then
  echo "O Docker nao esta rodando. Abra o Docker Desktop e tente de novo."
  exit 1
fi

if [ "$UNITARIOS" -eq 1 ]; then
  etapa "1/3 Testes unitarios (Maven)"
  if command -v mvn > /dev/null 2>&1; then
    for servico in $SERVICOS; do
      echo "-> $servico"
      mvn -B -q -f "backend/$servico/pom.xml" verify
      registrar "Maven: $servico" $?
    done
  else
    echo "Maven nao encontrado: os testes unitarios foram pulados (o build Docker ainda compila tudo)."
  fi
fi

etapa "2/3 Subindo o ecossistema (docker compose)"
if [ "$LIMPAR" -eq 1 ]; then
  docker compose down -v --remove-orphans
fi
docker compose up --build -d --wait --wait-timeout 900
CODIGO=$?
registrar "Ecossistema no ar (todos os containers saudaveis)" $CODIGO
if [ "$CODIGO" -ne 0 ]; then
  docker compose ps
  echo "Os containers nao ficaram saudaveis. Veja os logs com: docker compose logs <servico>"
  exit 1
fi

etapa "3/3 Testes de ponta a ponta (k6 pelo gateway)"
rodar_k6() {
  local nome="$1"; shift
  echo
  echo "-> $nome"
  docker compose run --rm k6 run --quiet "$@"
  registrar "k6: $nome" $?
}

rodar_k6 "smoke (todos os contextos)" smoke.js
rodar_k6 "concorrencia (lock pessimista)" concorrencia.js
rodar_k6 "jornada do investidor (compra, venda e Pix)" -e INVESTIDORES=5 -e DURACAO=30s jornada-investidor.js
rodar_k6 "carga no mercado" -e TAXA=50 -e DURACAO=30s carga-mercado.js

if [ "$MANTER" -eq 0 ]; then
  docker compose down --remove-orphans
fi

etapa "Resumo"
for linha in "${RESULTADOS[@]}"; do
  echo "$linha"
done
echo
if [ "$FALHAS" -eq 0 ]; then
  echo "Tudo certo. Gateway em http://localhost:8080 e painel do RabbitMQ em http://localhost:15672 (orbita/orbita)."
else
  echo "$FALHAS etapa(s) falharam."
fi
exit "$FALHAS"
