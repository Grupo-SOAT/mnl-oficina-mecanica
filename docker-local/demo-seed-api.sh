#!/usr/bin/env bash
#
# Gera tráfego de demonstração contra a API do monolito para popular os dashboards
# de observabilidade (Prometheus/Loki/Tempo/Grafana) rodando via docker-compose.
#
# Obs.: pode ser usado no ambiente produtivo também, basta mudar as ENVs.
#
# Uso:
#   ./docker-local/demo-seed-api.sh
#
# Pré-requisitos: curl + jq no host; stack observabilidade de pé
# (docker compose -f docker-local/docker-compose.yaml up -d --build).
#
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
KAFKA_CONTAINER="${KAFKA_CONTAINER:-workshop-kafka}"
BUDGET_TOPIC="${BUDGET_TOPIC:-budget-decision}"
N_OS="${N_OS:-3}"
STATUS_DELAY="${STATUS_DELAY:-5}"
DLT_N="${DLT_N:-3}"
USERNAME="admin"
PASSWORD="admin"

log() { printf '\n==> %s\n' "$*"; }

require() {
  command -v "$1" >/dev/null 2>&1 || { echo "Erro: '$1' é obrigatório."; exit 1; }
}
require curl
require jq

# 1. Aguarda o app responder
log "Aguardando $BASE_URL/actuator/health"
for _ in $(seq 1 60); do
  if curl -sf "$BASE_URL/actuator/health" >/dev/null 2>&1; then
    echo "   app pronto"
    break
  fi
  sleep 5
done
curl -sf "$BASE_URL/actuator/health" >/dev/null 2>&1 || { echo "App não respondeu a tempo."; exit 1; }

# 2. Login
log "Login ($USERNAME)"
TOKEN="$(curl -sf -X POST "$BASE_URL/auth/login" \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}" | jq -r '.token')"
[ -n "$TOKEN" ] || { echo "Falha no login."; exit 1; }
AUTH="Authorization: Bearer $TOKEN"

os_status() {
  curl -sf "$BASE_URL/service-orders/$1" -H "$AUTH" | jq -r '.status'
}

wait_status() {
  local os_id="$1" want="$2" st=""
  for _ in $(seq 1 30); do
    st="$(os_status "$os_id" 2>/dev/null || echo '')"
    [ "$st" = "$want" ] && return 0
    sleep 1
  done
  echo "   ERRO: OS $os_id não chegou a $want (último: $st)" >&2
  return 1
}

# 3. Cria e avança uma OS pelo ciclo completo de status
create_and_advance() {
  local i="$1" os_id sid sids plate
  plate="DEM-$(printf '%04d' $(( (RANDOM % 9000) + 1000 )))"
  log "Criando OS #$i (placa $plate)"
  os_id="$(curl -sf -X POST "$BASE_URL/service-orders/cascade" \
    -H "$AUTH" -H 'Content-Type: application/json' \
    -d "{\"vehicle\":{\"ownerId\":1,\"licensePlate\":\"$plate\",\"brand\":\"VW\",\"model\":\"Gol\",\"year\":2020,\"color\":\"PRATA\"},\"description\":\"OS de demonstração observabilidade $i\",\"catalogServiceIds\":[1,2]}" \
    | jq -r '.id')"
  echo "   OS id=$os_id (PENDING)"; sleep "$STATUS_DELAY"

  progress() {
    curl -sf -X POST "$BASE_URL/service-orders/$os_id/progress" \
      -H "$AUTH" -H 'Content-Type: application/json' -d "$1" >/dev/null
  }

  progress '{"action":"START_INSPECTION"}';   echo "   -> IN_INSPECTION"; sleep "$STATUS_DELAY"
  progress '{"action":"COMPLETE_INSPECTION"}'; echo "   -> AWAITING_APPROVAL"; sleep "$STATUS_DELAY"

  # Decisão de orçamento via Kafka (integração real consumida pelo monolito)
  log "Publicando decisão APPROVE da OS $os_id no Kafka ($BUDGET_TOPIC)"
  docker exec -i "$KAFKA_CONTAINER" kafka-console-producer \
    --bootstrap-server localhost:9092 \
    --topic "$BUDGET_TOPIC" \
    --property parse.key=true --property key.separator=: \
    <<< "$os_id:{\"serviceOrderId\":$os_id,\"decision\":\"APPROVE\"}"
  echo "   aguardando APPROVED..."
  wait_status "$os_id" "APPROVED"
  echo "   -> APPROVED"; sleep "$STATUS_DELAY"

  sids="$(curl -sf "$BASE_URL/service-orders/$os_id/services" -H "$AUTH" | jq -r '.data[].id')"

  # START_SERVICE só é permitido enquanto a OS está APPROVED (1º serviço);
  # serviços subsequentes partem direto para COMPLETE_SERVICE.
  first_sid="$(printf '%s\n' "$sids" | head -n1)"
  progress "{\"action\":\"START_SERVICE\",\"relatedServiceId\":$first_sid}"; echo "   serviço $first_sid -> IN_PROGRESS"; sleep "$STATUS_DELAY"

  for sid in $sids; do
    progress "{\"action\":\"COMPLETE_SERVICE\",\"relatedServiceId\":$sid}"; echo "   serviço $sid -> COMPLETED"
  done

  progress '{"action":"DELIVER_VEHICLE"}'; echo "   -> DELIVERED"
}

for i in $(seq 1 "$N_OS"); do
  create_and_advance "$i"
done

# 4. Cenários de erro deliberados (populam o dashboard de erros)
log "Disparando cenários de erro deliberados"
curl -s -o /dev/null -w "   token inválido -> %{http_code}\n" \
  "$BASE_URL/service-orders" -H "Authorization: Bearer invalid.token.here"

curl -s -o /dev/null -w "   placa inválida na criação -> %{http_code}\n" \
  -X POST "$BASE_URL/service-orders/cascade" -H "$AUTH" -H 'Content-Type: application/json' \
  -d '{"vehicle":{"ownerId":1,"licensePlate":"X","brand":"VW","model":"Gol","year":2020,"color":"PRATA"},"description":"erro","catalogServiceIds":[1]}'

curl -s -o /dev/null -w "   OS inexistente -> %{http_code}\n" \
  "$BASE_URL/service-orders/999999" -H "$AUTH"

# 5. Força a DLT: publica decisões inválidas (o consumer lança -> retry -> DLT)
log "Forçando $DLT_N mensagens para a DLT (decisões inválidas)"
for i in $(seq 1 "$DLT_N"); do
  docker exec -i "$KAFKA_CONTAINER" kafka-console-producer \
    --bootstrap-server localhost:9092 \
    --topic "$BUDGET_TOPIC" \
    --property parse.key=true --property key.separator=: \
    <<< "9999$i:{\"serviceOrderId\":9999$i,\"decision\":\"INVALID\"}"
done
echo "   aguardando retries -> $BUDGET_TOPIC-dlt..."
sleep 15

log "Pronto! Confira os dashboards em http://localhost:3000 (admin/admin)"
