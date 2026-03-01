#!/usr/bin/env bash
#
# Money Manager — Post-Release Smoke Test
#
# Validates all microservice health endpoints, performs a register/login flow,
# and runs CRUD cycle tests for each service domain.
#
# Usage:
#   ./scripts/smoke-test.sh                          # defaults to http://localhost:8080
#   GATEWAY_URL=http://prod:8080 ./scripts/smoke-test.sh

set -uo pipefail

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8080}"
HOST="${GATEWAY_HOST:-http://localhost}"
API="${GATEWAY_URL}/api"
PASS=0
FAIL=0
SKIP=0
TOKEN=""
TEST_EMAIL="smoketest_$(date +%s)@test.local"
TEST_PASSWORD="Test@12345"

green()  { printf "\033[32m%s\033[0m\n" "$*"; }
red()    { printf "\033[31m%s\033[0m\n" "$*"; }
yellow() { printf "\033[33m%s\033[0m\n" "$*"; }

assert_status() {
    local description="$1" method="$2" url="$3" expected="$4"
    shift 4
    local extra_args=()
    [[ $# -gt 0 ]] && extra_args=("$@")

    local http_code
    if [[ ${#extra_args[@]} -gt 0 ]]; then
        http_code=$(curl -s -o /dev/null -w "%{http_code}" -X "$method" "${extra_args[@]}" "$url" 2>/dev/null || echo "000")
    else
        http_code=$(curl -s -o /dev/null -w "%{http_code}" -X "$method" "$url" 2>/dev/null || echo "000")
    fi

    if [[ "$http_code" == "$expected" ]]; then
        green "  PASS  $description (HTTP $http_code)"
        ((PASS++))
    else
        red "  FAIL  $description — expected $expected, got $http_code"
        ((FAIL++))
    fi
}

assert_status_body() {
    local description="$1" method="$2" url="$3" expected="$4"
    shift 4
    local extra_args=()
    [[ $# -gt 0 ]] && extra_args=("$@")

    local response
    if [[ ${#extra_args[@]} -gt 0 ]]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" "${extra_args[@]}" "$url" 2>/dev/null || echo -e "\n000")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" 2>/dev/null || echo -e "\n000")
    fi
    local body http_code
    http_code=$(echo "$response" | tail -1)
    body=$(echo "$response" | sed '$d')

    if [[ "$http_code" == "$expected" ]]; then
        green "  PASS  $description (HTTP $http_code)"
        ((PASS++))
    else
        red "  FAIL  $description — expected $expected, got $http_code"
        red "        Response: $(echo "$body" | head -c 200)"
        ((FAIL++))
    fi
    echo "$body"
}

extract_id() {
    echo "$1" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2
}

# ─── Phase 1: Health Checks ──────────────────────────────────────
echo ""
yellow "═══════════════════════════════════════════"
yellow "  Money Manager — Smoke Test Suite"
yellow "  Gateway: $GATEWAY_URL"
yellow "═══════════════════════════════════════════"
echo ""
yellow "▸ Phase 1: Health Checks"

assert_status "API Gateway health"          GET "$API/health"             200
assert_status "Auth Service health"         GET "$HOST:8081/api/health"   200
assert_status "Transaction Service health"  GET "$HOST:8082/api/health"   200
assert_status "Planning Service health"     GET "$HOST:8083/api/health"   200
assert_status "Investment Service health"   GET "$HOST:8084/api/health"   200
assert_status "Banking Service health"      GET "$HOST:8085/api/health"   200
assert_status "Analytics Service health"    GET "$HOST:8086/api/health"   200
assert_status "AI Service health"           GET "$HOST:8087/health"       200
assert_status "Notification Service health" GET "$HOST:8088/api/health"   200
assert_status "Email Service health"        GET "$HOST:8089/api/health"   200

# ─── Phase 2: Auth Flow ──────────────────────────────────────────
echo ""
yellow "▸ Phase 2: Authentication Flow"

assert_status "Register new user" POST "$API/v1/register" 201 \
    -H "Content-Type: application/json" \
    -d "{\"fullName\":\"Smoke Tester\",\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\"}"

LOGIN_RESPONSE=$(assert_status_body "Login (pre-activation — expect 401)" POST "$API/v1/login" 401 \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\"}")

TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*"' | head -1 | cut -d'"' -f4 || true)

if [[ -z "$TOKEN" ]]; then
    yellow "  INFO  No token (account needs activation). Skipping auth-required tests."
    TOKEN="dummy"
fi

AUTH=(-H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json")

# ─── Phase 3: Public API Endpoints ───────────────────────────────
echo ""
yellow "▸ Phase 3: Public API Endpoints (via Gateway)"

assert_status "POST /v1/support/contact"    POST "$API/v1/support/contact" 201 \
    -H "Content-Type: application/json" \
    -d '{"name":"Smoke Test","email":"smoke@test.local","message":"Automated test"}'

# ─── Phase 4: Protected Endpoints ────────────────────────────────
echo ""
yellow "▸ Phase 4: Protected Endpoints (via Gateway)"

if [[ "$TOKEN" == "dummy" ]]; then
    yellow "  SKIP  Authenticated endpoint tests (no valid token)"
    SKIP=$((SKIP + 30))
else
    # Read endpoints
    assert_status "GET /v1/profile"                GET  "$API/v1/profile"                 200 "${AUTH[@]}"
    assert_status "GET /v1/incomes"                GET  "$API/v1/incomes"                 200 "${AUTH[@]}"
    assert_status "GET /v1/expenses"               GET  "$API/v1/expenses"                200 "${AUTH[@]}"
    assert_status "GET /v1/categories"             GET  "$API/v1/categories"              200 "${AUTH[@]}"
    assert_status "GET /v1/categories/income"      GET  "$API/v1/categories/income"       200 "${AUTH[@]}"
    assert_status "POST /v1/transactions/filter"   POST "$API/v1/transactions/filter"     200 "${AUTH[@]}" \
        -d '{"type":"income"}'
    assert_status "GET /v1/recurring-transactions" GET  "$API/v1/recurring-transactions"  200 "${AUTH[@]}"
    assert_status "GET /v1/budgets/summary"        GET  "$API/v1/budgets/summary"         200 "${AUTH[@]}"
    assert_status "GET /v1/savings-goals"          GET  "$API/v1/savings-goals"           200 "${AUTH[@]}"
    assert_status "GET /v1/debts"                  GET  "$API/v1/debts"                   200 "${AUTH[@]}"
    assert_status "GET /v1/investments"            GET  "$API/v1/investments"             200 "${AUTH[@]}"
    assert_status "GET /v1/lend-borrow?type=LEND"  GET  "$API/v1/lend-borrow?type=LEND"  200 "${AUTH[@]}"
    assert_status "GET /v1/dashboard"              GET  "$API/v1/dashboard"               200 "${AUTH[@]}"
    assert_status "GET /v1/analytics/net-worth"    GET  "$API/v1/analytics/net-worth"     200 "${AUTH[@]}"
    assert_status "GET /v1/analytics/monthly-summary" GET "$API/v1/analytics/monthly-summary" 200 "${AUTH[@]}"

    # ─── Phase 4b: CRUD Cycle Tests ──────────────────────────────
    echo ""
    yellow "▸ Phase 4b: CRUD Cycles (Create → Read → Update → Delete)"

    TODAY=$(date +%Y-%m-%d)

    # Category CRUD
    CAT_BODY=$(assert_status_body "Create category" POST "$API/v1/categories" 201 "${AUTH[@]}" \
        -d "{\"name\":\"SmokeTest-Cat-$RANDOM\",\"type\":\"income\",\"icon\":\"🧪\"}")
    CAT_ID=$(extract_id "$CAT_BODY")
    if [[ -n "$CAT_ID" ]]; then
        assert_status "Update category" PUT "$API/v1/categories/$CAT_ID" 200 "${AUTH[@]}" \
            -d "{\"name\":\"SmokeTest-Cat-Updated\",\"type\":\"income\",\"icon\":\"✅\"}"
    fi

    # Income CRUD
    if [[ -n "$CAT_ID" ]]; then
        INC_BODY=$(assert_status_body "Create income" POST "$API/v1/incomes" 201 "${AUTH[@]}" \
            -d "{\"name\":\"Smoke Income\",\"amount\":1000,\"date\":\"$TODAY\",\"categoryId\":$CAT_ID,\"icon\":\"💰\"}")
        INC_ID=$(extract_id "$INC_BODY")
        if [[ -n "$INC_ID" ]]; then
            assert_status "Update income" PUT "$API/v1/incomes/$INC_ID" 200 "${AUTH[@]}" \
                -d "{\"name\":\"Updated Income\",\"amount\":2000,\"date\":\"$TODAY\",\"categoryId\":$CAT_ID,\"icon\":\"💰\"}"
            assert_status "Delete income" DELETE "$API/v1/incomes/$INC_ID" 204 "${AUTH[@]}"
        fi

        # Expense CRUD
        EXP_CAT_BODY=$(assert_status_body "Create expense category" POST "$API/v1/categories" 201 "${AUTH[@]}" \
            -d "{\"name\":\"SmokeExp-$RANDOM\",\"type\":\"expense\",\"icon\":\"💸\"}")
        EXP_CAT_ID=$(extract_id "$EXP_CAT_BODY")
        if [[ -n "$EXP_CAT_ID" ]]; then
            EXP_BODY=$(assert_status_body "Create expense" POST "$API/v1/expenses" 201 "${AUTH[@]}" \
                -d "{\"name\":\"Smoke Expense\",\"amount\":500,\"date\":\"$TODAY\",\"categoryId\":$EXP_CAT_ID,\"icon\":\"🛒\"}")
            EXP_ID=$(extract_id "$EXP_BODY")
            if [[ -n "$EXP_ID" ]]; then
                assert_status "Update expense" PUT "$API/v1/expenses/$EXP_ID" 200 "${AUTH[@]}" \
                    -d "{\"name\":\"Updated Expense\",\"amount\":750,\"date\":\"$TODAY\",\"categoryId\":$EXP_CAT_ID,\"icon\":\"🛒\"}"
                assert_status "Delete expense" DELETE "$API/v1/expenses/$EXP_ID" 204 "${AUTH[@]}"
            fi
        fi
    fi

    # Lend-Borrow CRUD + Settlement
    LEND_BODY=$(assert_status_body "Create lend entry" POST "$API/v1/lend-borrow" 201 "${AUTH[@]}" \
        -d "{\"name\":\"Smoke Lend\",\"personName\":\"John\",\"amount\":5000,\"date\":\"$TODAY\",\"dueDate\":\"2026-12-31\",\"type\":\"LEND\",\"icon\":\"🤝\"}")
    LEND_ID=$(extract_id "$LEND_BODY")
    if [[ -n "$LEND_ID" ]]; then
        assert_status "Update lend entry" PUT "$API/v1/lend-borrow/$LEND_ID" 200 "${AUTH[@]}" \
            -d "{\"name\":\"Updated Lend\",\"personName\":\"John D\",\"amount\":5000,\"date\":\"$TODAY\",\"dueDate\":\"2026-12-31\",\"type\":\"LEND\",\"icon\":\"🤝\"}"

        # Settlement
        SETTLE_BODY=$(assert_status_body "Add settlement" POST "$API/v1/lend-borrow/$LEND_ID/settlements" 201 "${AUTH[@]}" \
            -d "{\"amount\":2000,\"date\":\"$TODAY\",\"notes\":\"Partial payment\"}")
        SETTLE_ID=$(extract_id "$SETTLE_BODY")
        assert_status "Get settlements" GET "$API/v1/lend-borrow/$LEND_ID/settlements" 200 "${AUTH[@]}"
        if [[ -n "$SETTLE_ID" ]]; then
            assert_status "Delete settlement" DELETE "$API/v1/lend-borrow/$LEND_ID/settlements/$SETTLE_ID" 204 "${AUTH[@]}"
        fi

        assert_status "Mark as paid" PATCH "$API/v1/lend-borrow/$LEND_ID/status?status=PAID" 200 "${AUTH[@]}"
        assert_status "Delete lend entry" DELETE "$API/v1/lend-borrow/$LEND_ID" 204 "${AUTH[@]}"
    fi

    # Investment CRUD
    INV_BODY=$(assert_status_body "Create investment" POST "$API/v1/investments" 201 "${AUTH[@]}" \
        -d "{\"name\":\"Smoke Stock\",\"type\":\"STOCKS\",\"investedAmount\":10000,\"currentValue\":12000,\"purchaseDate\":\"$TODAY\",\"icon\":\"📈\"}")
    INV_ID=$(extract_id "$INV_BODY")
    if [[ -n "$INV_ID" ]]; then
        assert_status "Update investment" PUT "$API/v1/investments/$INV_ID" 200 "${AUTH[@]}" \
            -d "{\"name\":\"Updated Stock\",\"type\":\"STOCKS\",\"investedAmount\":10000,\"currentValue\":13000,\"purchaseDate\":\"$TODAY\",\"icon\":\"📈\"}"
        assert_status "Delete investment" DELETE "$API/v1/investments/$INV_ID" 204 "${AUTH[@]}"
    fi

    # Savings Goal CRUD
    SAV_BODY=$(assert_status_body "Create savings goal" POST "$API/v1/savings-goals" 201 "${AUTH[@]}" \
        -d "{\"name\":\"Smoke Fund\",\"targetAmount\":50000,\"icon\":\"🎯\"}")
    SAV_ID=$(extract_id "$SAV_BODY")
    if [[ -n "$SAV_ID" ]]; then
        assert_status "Update savings goal" PUT "$API/v1/savings-goals/$SAV_ID" 200 "${AUTH[@]}" \
            -d "{\"name\":\"Updated Fund\",\"targetAmount\":60000,\"icon\":\"🎯\"}"
        assert_status "Contribute to savings" PATCH "$API/v1/savings-goals/$SAV_ID/contribute" 200 "${AUTH[@]}" \
            -d '{"amount":5000}'
        assert_status "Delete savings goal" DELETE "$API/v1/savings-goals/$SAV_ID" 204 "${AUTH[@]}"
    fi

    # Debt CRUD
    DEBT_BODY=$(assert_status_body "Create debt" POST "$API/v1/debts" 201 "${AUTH[@]}" \
        -d "{\"name\":\"Smoke Loan\",\"type\":\"PERSONAL_LOAN\",\"originalAmount\":20000,\"startDate\":\"$TODAY\",\"icon\":\"💳\"}")
    DEBT_ID=$(extract_id "$DEBT_BODY")
    if [[ -n "$DEBT_ID" ]]; then
        assert_status "Update debt" PUT "$API/v1/debts/$DEBT_ID" 200 "${AUTH[@]}" \
            -d "{\"name\":\"Updated Loan\",\"type\":\"PERSONAL_LOAN\",\"originalAmount\":20000,\"startDate\":\"$TODAY\",\"icon\":\"💳\"}"
        assert_status "Debt payment" PATCH "$API/v1/debts/$DEBT_ID/payment" 200 "${AUTH[@]}" \
            -d '{"amount":5000}'
        assert_status "Delete debt" DELETE "$API/v1/debts/$DEBT_ID" 204 "${AUTH[@]}"
    fi
fi

# ─── Phase 5: CORS Preflight ─────────────────────────────────────
echo ""
yellow "▸ Phase 5: CORS Preflight Check"

CORS_STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X OPTIONS "$API/v1/login" \
    -H "Origin: http://localhost:5173" \
    -H "Access-Control-Request-Method: POST" \
    -H "Access-Control-Request-Headers: content-type,authorization" 2>/dev/null || echo "000")

if [[ "$CORS_STATUS" == "200" ]]; then
    green "  PASS  CORS preflight (HTTP $CORS_STATUS)"
    ((PASS++))
else
    red "  FAIL  CORS preflight — expected 200, got $CORS_STATUS"
    ((FAIL++))
fi

# ─── Report ───────────────────────────────────────────────────────
echo ""
yellow "═══════════════════════════════════════════"
if [[ $FAIL -eq 0 ]]; then
    green "  ALL $PASS TESTS PASSED ($SKIP SKIPPED)"
else
    red "  $FAIL FAILED, $PASS PASSED, $SKIP SKIPPED"
fi
yellow "═══════════════════════════════════════════"
echo ""

exit $FAIL
