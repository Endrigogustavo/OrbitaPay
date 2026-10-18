#!/usr/bin/env bash
# Sobe o OrbitaPay em modo local: Docker só para RabbitMQ e MongoDB,
# cada microserviço com "mvn spring-boot:run" e o app com "npm run web".
#
#   scripts/orbita.sh subir [--limpar]   sobe tudo e abre uma aba de log por aplicação
#   scripts/orbita.sh parar              para os serviços, o app e os containers
#   scripts/orbita.sh status             mostra o que está no ar
#   scripts/orbita.sh abas               reabre as abas de log (Windows Terminal)
#   scripts/orbita.sh logs [servico]     acompanha o log de um serviço, ou de todos

set -u

RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
PASTA_LOCAL="$RAIZ/.orbita-local"
LOGS="$PASTA_LOCAL/logs"

# Ordem de subida: quem só consome eventos primeiro, quem publica na subida depois,
# para nenhuma mensagem dos dados de demonstração se perder antes da fila existir.
ONDA_1="auth clientes ativos negociacao"
ONDA_2="pagamentos"
ONDA_3="gateway"
SERVICOS="$ONDA_1 $ONDA_2 $ONDA_3"
PORTA_DO_APP=19006

porta_de() {
  case "$1" in
    gateway) echo 9000 ;; clientes) echo 9002 ;; ativos) echo 9003 ;; negociacao) echo 9004 ;;
    auth) echo 9001 ;; pagamentos) echo 9005 ;; app) echo "$PORTA_DO_APP" ;;
  esac
}

ehWindows() {
  case "$(uname -s)" in MINGW*|MSYS*|CYGWIN*) return 0 ;; *) return 1 ;; esac
}

titulo() {
  echo
  echo "== $1"
}

no_ar() {
  (echo > "/dev/tcp/127.0.0.1/$1") > /dev/null 2>&1
}

pid_na_porta() {
  if ehWindows; then
    netstat -ano | awk -v p=":$1" '$4 == "LISTENING" && substr($2, length($2) - length(p) + 1) == p { print $5; exit }'
  else
    lsof -ti "tcp:$1" -sTCP:LISTEN 2> /dev/null | head -1
  fi
}

nome_do_processo() {
  if ehWindows; then
    tasklist //FI "PID eq $1" //FO CSV //NH 2> /dev/null | cut -d, -f1 | tr -d '"'
  else
    ps -p "$1" -o comm= 2> /dev/null
  fi
}

encerrar_pid() {
  if ehWindows; then
    taskkill //F //T //PID "$1" > /dev/null 2>&1
  else
    kill "$1" 2> /dev/null
  fi
}

liberar_porta() {
  local porta="$1" nome="$2" pid processo
  pid="$(pid_na_porta "$porta")"
  [ -z "$pid" ] && return 0
  processo="$(nome_do_processo "$pid")"
  case "$processo" in
    java*|node*)
      echo "   $nome: parando execução anterior na porta $porta ($processo, PID $pid)"
      encerrar_pid "$pid"
      ;;
    *)
      echo "   A porta $porta ($nome) está ocupada por '$processo' (PID $pid). Feche esse programa e rode de novo."
      exit 1
      ;;
  esac
}

exigir() {
  if ! command -v "$1" > /dev/null 2>&1; then
    echo "Não encontrei '$1'. $2"
    exit 1
  fi
}

esperar_no_ar() {
  local limite=$((SECONDS + 240)) faltando servico
  while :; do
    faltando=""
    for servico in "$@"; do
      if ! no_ar "$(porta_de "$servico")"; then
        if grep -qE "APPLICATION FAILED TO START|BUILD FAILURE|ERROR\] Failed" "$LOGS/$servico.log" 2> /dev/null; then
          echo "   $servico falhou ao subir. Últimas linhas do log ($LOGS/$servico.log):"
          tail -25 "$LOGS/$servico.log"
          exit 1
        fi
        faltando="$faltando $servico"
      fi
    done
    [ -z "$faltando" ] && return 0
    if [ "$SECONDS" -ge "$limite" ]; then
      echo "   Tempo esgotado esperando:$faltando. Veja: scripts/orbita.sh logs <servico>"
      exit 1
    fi
    sleep 2
  done
}

iniciar_servico() {
  local servico="$1"
  echo "   $servico -> http://localhost:$(porta_de "$servico")"
  nohup mvn -q -B -f "$RAIZ/backend/$servico/spring/pom.xml" spring-boot:run \
    -Dspring-boot.run.jvmArguments="-Xmx384m -XX:TieredStopAtLevel=1" \
    > "$LOGS/$servico.log" 2>&1 < /dev/null &
}

subir() {
  local limpar=0 servico
  for argumento in "$@"; do
    case "$argumento" in
      --limpar) limpar=1 ;;
      *) echo "Opção desconhecida: $argumento"; exit 2 ;;
    esac
  done

  titulo "1/5 Conferindo as ferramentas"
  exigir docker "Instale o Docker Desktop."
  exigir java "Instale o Java 21."
  exigir mvn "Instale o Maven 3.9."
  exigir npm "Instale o Node.js 20 ou mais novo."
  if ! docker info > /dev/null 2>&1; then
    echo "O Docker não está rodando. Abra o Docker Desktop e tente de novo."
    exit 1
  fi
  echo "   Docker, Java, Maven e Node encontrados."

  titulo "2/5 Infraestrutura no Docker (RabbitMQ e MongoDB)"
  cd "$RAIZ" || exit 1
  if [ "$limpar" -eq 1 ]; then
    echo "   Apagando os dados do MongoDB local..."
    docker compose --profile local rm -s -f mongo-local > /dev/null 2>&1
    docker volume rm -f "$(basename "$RAIZ" | tr '[:upper:]' '[:lower:]')_mongo-local" > /dev/null 2>&1
  fi
  for porta in 5672 27018; do
    local outro
    outro="$(docker ps --filter "publish=$porta" --format '{{.Names}}' | grep -vE 'rabbitmq|mongo-local' | head -1)"
    if [ -n "$outro" ]; then
      echo "   O container '$outro' já usa a porta $porta. Pare-o com: docker stop $outro"
      exit 1
    fi
  done
  docker compose --profile local up -d --wait rabbitmq mongo-local || {
    echo "   Os containers não ficaram saudáveis. Veja: docker compose logs rabbitmq mongo-local"
    exit 1
  }
  echo "   RabbitMQ em localhost:5672 (painel http://localhost:15672) e MongoDB em localhost:27018."

  titulo "3/5 Compilando os 6 serviços (mvn install)"
  for servico in $SERVICOS; do
    liberar_porta "$(porta_de "$servico")" "$servico"
  done
  liberar_porta "$PORTA_DO_APP" app
  mkdir -p "$LOGS"
  for servico in $SERVICOS; do
    printf "   %-11s" "$servico"
    if mvn -q -B -f "$RAIZ/backend/$servico/pom.xml" install -DskipTests > "$LOGS/$servico-build.log" 2>&1; then
      echo "ok"
    else
      echo "FALHOU"
      tail -30 "$LOGS/$servico-build.log"
      exit 1
    fi
  done

  titulo "4/5 Subindo os serviços (mvn spring-boot:run)"
  for onda in "$ONDA_1" "$ONDA_2" "$ONDA_3"; do
    for servico in $onda; do
      iniciar_servico "$servico"
    done
    # shellcheck disable=SC2086
    esperar_no_ar $onda
  done

  titulo "5/5 Subindo o app (npm run web)"
  if [ ! -d "$RAIZ/mobile/node_modules" ]; then
    echo "   Primeira vez: instalando as dependências do app (npm install)..."
    (cd "$RAIZ/mobile" && npm install --no-audit --no-fund > "$LOGS/app-install.log" 2>&1) || {
      echo "   npm install falhou. Veja $LOGS/app-install.log"
      exit 1
    }
  fi
  (cd "$RAIZ/mobile" && BROWSER=none nohup npm run web > "$LOGS/app.log" 2>&1 < /dev/null &)
  esperar_no_ar app

  status
  abrir_navegador "http://localhost:$PORTA_DO_APP"
  echo
  echo "==================================================================="
  echo "  Tudo no ar. Abra o app em:  http://localhost:$PORTA_DO_APP"
  echo "  Para desligar:              scripts/orbita.sh parar"
  echo "==================================================================="

  if abrir_abas; then
    echo
    echo "As abas de log abriram no Windows Terminal (janela \"OrbitaPay\"), uma por aplicação."
    echo "Os serviços continuam rodando mesmo se você fechar esta janela."
    pausar_se_janela
  else
    echo
    echo "Mostrando os logs de todas as aplicações (Ctrl+C sai daqui; os serviços continuam rodando)."
    logs
  fi
}

# Quando o script foi aberto com dois cliques, a janela fecharia sozinha no fim (ou num erro)
# antes de dar tempo de ler: aqui ela espera um Enter.
pausar_se_janela() {
  if [ -t 0 ] && [ -t 1 ]; then
    echo
    read -r -p "Pressione Enter para fechar esta janela..." _
  fi
}

abrir_navegador() {
  if ehWindows; then
    cmd //c start "" "$1" > /dev/null 2>&1
  elif command -v xdg-open > /dev/null 2>&1; then
    xdg-open "$1" > /dev/null 2>&1
  elif command -v open > /dev/null 2>&1; then
    open "$1" > /dev/null 2>&1
  fi
}

# Abre uma janela do Windows Terminal chamada "OrbitaPay" com uma aba por aplicação.
# Cada aba roda "scripts/orbita.sh acompanhar <servico>" e fecha sozinha quando o serviço para.
abrir_abas() {
  ehWindows || return 1
  command -v wt.exe > /dev/null 2>&1 || return 1
  local bash_win script_win servico cor argumentos=()
  bash_win="$(cygpath -w /usr/bin/bash)"
  script_win="$(cygpath -m "$RAIZ/scripts/orbita.sh")"
  for servico in app $SERVICOS; do
    case "$servico" in
      app) cor="#2E7D32" ;; gateway) cor="#1565C0" ;; *) cor="#6A1B9A" ;;
    esac
    [ ${#argumentos[@]} -gt 0 ] && argumentos+=(";")
    argumentos+=(new-tab --title "$servico :$(porta_de "$servico")" --suppressApplicationTitle --tabColor "$cor"
      "$bash_win" -l "$script_win" acompanhar "$servico")
  done
  wt.exe -w OrbitaPay "${argumentos[@]}" > /dev/null 2>&1 &
  return 0
}

# Usado por cada aba: mostra o log ao vivo enquanto o serviço estiver no ar.
acompanhar() {
  local servico="${1:?informe o servico}" porta cauda
  porta="$(porta_de "$servico")"
  echo "=== $servico · http://localhost:$porta · log: .orbita-local/logs/$servico.log"
  echo
  tail -n 200 -F "$LOGS/$servico.log" 2> /dev/null &
  cauda=$!
  until no_ar "$porta"; do sleep 2; done
  while no_ar "$porta"; do sleep 3; done
  kill "$cauda" 2> /dev/null
  echo
  echo "=== $servico parou. Esta aba fecha em 3 segundos."
  sleep 3
}

parar() {
  titulo "Parando os serviços e o app"
  local servico pid
  for servico in $SERVICOS app; do
    pid="$(pid_na_porta "$(porta_de "$servico")")"
    if [ -n "$pid" ]; then
      encerrar_pid "$pid"
      echo "   $servico parado"
    fi
  done
  titulo "Parando os containers (os dados ficam guardados)"
  cd "$RAIZ" && docker compose --profile local stop rabbitmq mongo-local
}

status() {
  titulo "Status"
  local servico porta situacao
  for servico in $SERVICOS app; do
    porta="$(porta_de "$servico")"
    if no_ar "$porta"; then situacao="no ar"; else situacao="parado"; fi
    printf "   %-11s %-7s http://localhost:%s\n" "$servico" "$situacao" "$porta"
  done
  for porta in 5672 27018; do
    if no_ar "$porta"; then situacao="no ar"; else situacao="parado"; fi
    if [ "$porta" = 5672 ]; then servico=rabbitmq; else servico=mongodb; fi
    printf "   %-11s %-7s localhost:%s\n" "$servico" "$situacao" "$porta"
  done
  echo
  echo "   App:              http://localhost:$PORTA_DO_APP"
  echo "   Gateway:          http://localhost:9000/api/ativos"
  echo "   Painel RabbitMQ:  http://localhost:15672 (orbita / orbita)"
  echo "   Logs:             scripts/orbita.sh logs <servico>  (pasta .orbita-local/logs)"
}

logs() {
  if [ -n "${1:-}" ]; then
    tail -n 60 -F "$LOGS/$1.log"
    return
  fi
  local arquivos=() servico
  for servico in $SERVICOS app; do
    arquivos+=("$LOGS/$servico.log")
  done
  tail -n 5 -F "${arquivos[@]}"
}

comando="${1:-subir}"
[ $# -gt 0 ] && shift
case "$comando" in
  subir)
    trap 'codigo=$?; [ "$codigo" -ne 0 ] && [ "$codigo" -ne 130 ] && pausar_se_janela' EXIT
    subir "$@"
    ;;
  parar) parar ;;
  status) status ;;
  abas) abrir_abas || echo "Windows Terminal (wt.exe) não encontrado. Use: scripts/orbita.sh logs" ;;
  acompanhar) acompanhar "$@" ;;
  logs) logs "$@" ;;
  -h|--ajuda|ajuda)
    sed -n '2,10p' "$0" | sed 's/^# \{0,1\}//'
    ;;
  *) echo "Comando desconhecido: $comando (use subir, parar, status ou logs)"; exit 2 ;;
esac
