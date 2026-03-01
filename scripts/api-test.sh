#!/usr/bin/env bash
#
# Money Manager — Comprehensive API Test Suite
#
# Tests ALL endpoints from apiEndpoints.js against the running backend.
# Designed for daily scheduled runs or on-demand verification.
#
# Usage:
#   ./scripts/api-test.sh                                    # interactive: prompts for credentials
#   TOKEN=<jwt> ./scripts/api-test.sh                        # pass token directly
#   EMAIL=user@test.com PASSWORD=pass123 ./scripts/api-test.sh
#
# Environment variables:
#   GATEWAY_URL  — gateway base (default: http://localhost:8080)
#   TOKEN        — JWT token (skips login)
#   EMAIL        — login email
#   PASSWORD     — login password
#   LOG_FILE     — write results to file (default: /tmp/api-test-<timestamp>.log)

set -uo pipefail

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8080}"
API="${GATEWAY_URL}/api/v1"
PASS=0
FAIL=0
SKIP=0
TOTAL=0
TOKEN="${TOKEN:-}"
EMAIL="${EMAIL:-}"
PASSWORD="${PASSWORD:-}"
LOG_FILE="${LOG_FILE:-/tmp/api-test-$(date +%Y%m%d-%H%M%S).log}"
TODAY=$(date +%Y-%m-%d)

green()  { printf "\033[32m%s\033[0m\n" "$*"; }
red()    { printf "\033[31m%s\033[0m\n" "$*"; }
yellow() { printf "\033[33m%s\033[0m\n" "$*"; }

log() { echo "$*" | tee -a "$LOG_FILE"; }

test_endpoint() {
    local description="$1" method="$2" url="$3" expected="$4"
    shift 4
    ((TOTAL++))
    local extra_args=()
    [[ $# -gt 0 ]] && extra_args=("$@")

    local response http_code body
    if [[ ${#extra_args[@]} -gt 0 ]]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" "${extra_args[@]}" "$url" 2>/dev/null || echo -e "\n000")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" 2>/dev/null || echo -e "\n000")
    fi
    http_code=$(echo "$response" | tail -1)
    body=$(echo "$response" | sed '$d')

    if [[ "$http_code" == "$expected" ]]; then
        log "$(green "  PASS  [$method] $description (HTTP $http_code)")"
        ((PASS++))
    else
        log "$(red "  FAIL  [$method] $description — expected $expected, got $http_code")"
        log "        URL: $url"
        log "        Response: $(echo "$body" | head -c 300)"
        ((FAIL++))
    fi
    echo "$body"
}

extract_id() {
    echo "$1" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2
}

# ─── Authentication ──────────────────────────────────────────────
echo ""
yellow "═══════════════════════════════════════════════════"
yellow "  Money Manager — Comprehensive API Test Suite"
yellow "  Gateway: $GATEWAY_URL"
yellow "  Log: $LOG_FILE"
yellow "═══════════════════════════════════════════════════"
echo ""

if [[ -z "$TOKEN" ]]; then
    if [[ -z "$EMAIL" || -z "$PASSWORD" ]]; then
        echo "No TOKEN or EMAIL/PASSWORD provided."
        echo "Usage: TOKEN=<jwt> ./scripts/api-test.sh"
        echo "   or: EMAIL=user@test.com PASSWORD=pass123 ./scripts/api-test.sh"
        exit 1
    fi

    yellow "▸ Authenticating..."
    LOGIN_RESP=$(curl -s -X POST "$API/login" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")
    TOKEN=$(echo "$LOGIN_RESP" | grep -o '"accessToken":"[^"]*"' | head -1 | cut -d'"' -f4 || true)

    if [[ -z "$TOKEN" ]]; then
        red "  Failed to authenticate. Check credentials."
        red "  Response: $(echo "$LOGIN_RESP" | head -c 200)"
        exit 1
    fi
    green "  Authenticated successfully"
fi

AUTH=(-H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json")

# ─── Profile Endpoints ───────────────────────────────────────────
echo ""
yellow "▸ Profile Endpoints"
test_endpoint "Get profile" GET "$API/profile" 200 "${AUTH[@]}" > /dev/null
test_endpoint "Update profile" PUT "$API/profile" 200 "${AUTH[@]}" \
    -d '{"fullName":"API Test User"}' > /dev/null

# ─── Category Endpoints ──────────────────────────────────────────
echo ""
yellow "▸ Category Endpoints"
test_endpoint "Get all categories" GET "$API/categories" 200 "${AUTH[@]}" > /dev/null
test_endpoint "Get income categories" GET "$API/categories/income" 200 "${AUTH[@]}" > /dev/null
test_endpoint "Get expense categories" GET "$API/categories/expense" 200 "${AUTH[@]}" > /dev/null

CAT_RESP=$(test_endpoint "Create category" POST "$API/categories" 201 "${AUTH[@]}" \
    -d "{\"name\":\"APITest-$RANDOM\",\"type\":\"income\",\"icon\":\"🧪\"}")
CAT_ID=$(extract_id "$CAT_RESP")

EXP_CAT_RESP=$(test_endpoint "Create expense category" POST "$API/categories" 201 "${AUTH[@]}" \
    -d "{\"name\":\"APITestExp-$RANDOM\",\"type\":\"expense\",\"icon\":\"💸\"}")
EXP_CAT_ID=$(extract_id "$EXP_CAT_RESP")

if [[ -n "$CAT_ID" ]]; then
    test_endpoint "Update category" PUT "$API/categories/$CAT_ID" 200 "${AUTH[@]}" \
        -d '{"name":"APITest-Updated","type":"income","icon":"✅"}' > /dev/null
fi

# ─── Income Endpoints ────────────────────────────────────────────
echo ""
yellow "▸ Income Endpoints"
test_endpoint "Get incomes" GET "$API/incomes" 200 "${AUTH[@]}" > /dev/null

INC_RESP=""
if [[ -n "$CAT_ID" ]]; then
    INC_RESP=$(test_endpoint "Create income" POST "$API/incomes" 201 "${AUTH[@]}" \
        -d "{\"name\":\"API Test Income\",\"amount\":5000,\"date\":\"$TODAY\",\"categoryId\":$CAT_ID}")
fi
INC_ID=$(extract_id "$INC_RESP")

if [[ -n "$INC_ID" ]]; then
    test_endpoint "Update income" PUT "$API/incomes/$INC_ID" 200 "${AUTH[@]}" \
        -d "{\"name\":\"Updated Income\",\"amount\":6000,\"date\":\"$TODAY\",\"categoryId\":$CAT_ID}" > /dev/null
    test_endpoint "Delete income" DELETE "$API/incomes/$INC_ID" 204 "${AUTH[@]}" > /dev/null
fi

# ─── Expense Endpoints ───────────────────────────────────────────
echo ""
yellow "▸ Expense Endpoints"
test_endpoint "Get expenses" GET "$API/expenses" 200 "${AUTH[@]}" > /dev/null

EXP_RESP=""
if [[ -n "$EXP_CAT_ID" ]]; then
    EXP_RESP=$(test_endpoint "Create expense" POST "$API/expenses" 201 "${AUTH[@]}" \
        -d "{\"name\":\"API Test Expense\",\"amount\":2500,\"date\":\"$TODAY\",\"categoryId\":$EXP_CAT_ID}")
fi
EXP_ID=$(extract_id "$EXP_RESP")

if [[ -n "$EXP_ID" ]]; then
    test_endpoint "Update expense" PUT "$API/expenses/$EXP_ID" 200 "${AUTH[@]}" \
        -d "{\"name\":\"Updated Expense\",\"amount\":3000,\"date\":\"$TODAY\",\"categoryId\":$EXP_CAT_ID}" > /dev/null
    test_endpoint "Delete expense" DELETE "$API/expenses/$EXP_ID" 204 "${AUTH[@]}" > /dev/null
fi

# ─── Filter Endpoints ────────────────────────────────────────────
echo ""
yellow "▸ Filter Endpoints"
test_endpoint "Apply filter (income)" POST "$API/transactions/filter" 200 "${AUTH[@]}" \
    -d '{"type":"income"}' > /dev/null
test_endpoint "Apply filter (expense)" POST "$API/transactions/filter" 200 "${AUTH[@]}" \
    -d '{"type":"expense"}' > /dev/null

# ─── Recurring Endpoints ─────────────────────────────────────────
echo ""
yellow "▸ Recurring Transaction Endpoints"
test_endpoint "Get recurring" GET "$API/recurring-transactions" 200 "${AUTH[@]}" > /dev/null

REC_RESP=""
if [[ -n "$CAT_ID" ]]; then
    REC_RESP=$(test_endpoint "Create recurring" POST "$API/recurring-transactions" 201 "${AUTH[@]}" \
        -d "{\"name\":\"API Recurring\",\"amount\":1000,\"type\":\"INCOME\",\"frequency\":\"MONTHLY\",\"startDate\":\"$TODAY\",\"categoryId\":$CAT_ID}")
fi
REC_ID=$(extract_id "$REC_RESP")

if [[ -n "$REC_ID" ]]; then
    test_endpoint "Update recurring" PUT "$API/recurring-transactions/$REC_ID" 200 "${AUTH[@]}" \
        -d "{\"name\":\"Updated Recurring\",\"amount\":1500,\"type\":\"INCOME\",\"frequency\":\"MONTHLY\",\"startDate\":\"$TODAY\",\"categoryId\":$CAT_ID}" > /dev/null
    test_endpoint "Toggle recurring" PATCH "$API/recurring-transactions/$REC_ID/toggle" 200 "${AUTH[@]}" > /dev/null
    test_endpoint "Delete recurring" DELETE "$API/recurring-transactions/$REC_ID" 204 "${AUTH[@]}" > /dev/null
fi

# ─── Lend/Borrow Endpoints ───────────────────────────────────────
echo ""
yellow "▸ Lend/Borrow Endpoints"
test_endpoint "Get lend entries" GET "$API/lend-borrow?type=LEND" 200 "${AUTH[@]}" > /dev/null
test_endpoint "Get borrow entries" GET "$API/lend-borrow?type=BORROW" 200 "${AUTH[@]}" > /dev/null

LEND_RESP=$(test_endpoint "Create lend entry" POST "$API/lend-borrow" 201 "${AUTH[@]}" \
    -d "{\"name\":\"API Lend\",\"personName\":\"Test Person\",\"amount\":10000,\"date\":\"$TODAY\",\"dueDate\":\"2026-12-31\",\"type\":\"LEND\",\"icon\":\"🤝\"}")
LEND_ID=$(extract_id "$LEND_RESP")

if [[ -n "$LEND_ID" ]]; then
    test_endpoint "Update lend entry" PUT "$API/lend-borrow/$LEND_ID" 200 "${AUTH[@]}" \
        -d "{\"name\":\"Updated Lend\",\"personName\":\"Test Person\",\"amount\":10000,\"date\":\"$TODAY\",\"dueDate\":\"2026-12-31\",\"type\":\"LEND\",\"icon\":\"🤝\"}" > /dev/null

    SETTLE_RESP=$(test_endpoint "Add settlement" POST "$API/lend-borrow/$LEND_ID/settlements" 201 "${AUTH[@]}" \
        -d "{\"amount\":3000,\"date\":\"$TODAY\",\"notes\":\"Partial payment\"}")
    SETTLE_ID=$(extract_id "$SETTLE_RESP")
    test_endpoint "Get settlements" GET "$API/lend-borrow/$LEND_ID/settlements" 200 "${AUTH[@]}" > /dev/null

    if [[ -n "$SETTLE_ID" ]]; then
        test_endpoint "Delete settlement" DELETE "$API/lend-borrow/$LEND_ID/settlements/$SETTLE_ID" 204 "${AUTH[@]}" > /dev/null
    fi

    test_endpoint "Update status (PAID)" PATCH "$API/lend-borrow/$LEND_ID/status?status=PAID" 200 "${AUTH[@]}" > /dev/null
    test_endpoint "Update status (PENDING)" PATCH "$API/lend-borrow/$LEND_ID/status?status=PENDING" 200 "${AUTH[@]}" > /dev/null
    test_endpoint "Delete lend entry" DELETE "$API/lend-borrow/$LEND_ID" 204 "${AUTH[@]}" > /dev/null
fi

# ─── Budget Endpoints ────────────────────────────────────────────
echo ""
yellow "▸ Budget Endpoints"
MONTH=$(date +%Y-%m)
test_endpoint "Get budget summary" GET "$API/budgets/summary?month=$MONTH" 200 "${AUTH[@]}" > /dev/null

BUD_RESP=""
if [[ -n "$EXP_CAT_ID" ]]; then
    BUD_RESP=$(test_endpoint "Create budget" POST "$API/budgets" 201 "${AUTH[@]}" \
        -d "{\"categoryId\":$EXP_CAT_ID,\"amount\":5000,\"budgetMonth\":\"$MONTH\"}")
fi
BUD_ID=$(extract_id "$BUD_RESP")

if [[ -n "$BUD_ID" ]]; then
    test_endpoint "Update budget" PUT "$API/budgets/$BUD_ID" 200 "${AUTH[@]}" \
        -d "{\"categoryId\":$EXP_CAT_ID,\"amount\":7500,\"budgetMonth\":\"$MONTH\"}" > /dev/null
    test_endpoint "Delete budget" DELETE "$API/budgets/$BUD_ID" 204 "${AUTH[@]}" > /dev/null
fi

# ─── Savings Endpoints ───────────────────────────────────────────
echo ""
yellow "▸ Savings Goal Endpoints"
test_endpoint "Get savings goals" GET "$API/savings-goals" 200 "${AUTH[@]}" > /dev/null

SAV_RESP=$(test_endpoint "Create savings goal" POST "$API/savings-goals" 201 "${AUTH[@]}" \
    -d '{"name":"API Test Fund","targetAmount":100000,"icon":"🎯"}')
SAV_ID=$(extract_id "$SAV_RESP")

if [[ -n "$SAV_ID" ]]; then
    test_endpoint "Update savings goal" PUT "$API/savings-goals/$SAV_ID" 200 "${AUTH[@]}" \
        -d '{"name":"Updated Fund","targetAmount":120000,"icon":"🎯"}' > /dev/null
    test_endpoint "Contribute to savings" PATCH "$API/savings-goals/$SAV_ID/contribute" 200 "${AUTH[@]}" \
        -d '{"amount":10000}' > /dev/null
    test_endpoint "Delete savings goal" DELETE "$API/savings-goals/$SAV_ID" 204 "${AUTH[@]}" > /dev/null
fi

# ─── Debt Endpoints ──────────────────────────────────────────────
echo ""
yellow "▸ Debt Endpoints"
test_endpoint "Get debts" GET "$API/debts" 200 "${AUTH[@]}" > /dev/null

DEBT_RESP=$(test_endpoint "Create debt" POST "$API/debts" 201 "${AUTH[@]}" \
    -d "{\"name\":\"API Loan\",\"type\":\"PERSONAL_LOAN\",\"originalAmount\":50000,\"startDate\":\"$TODAY\",\"icon\":\"💳\"}")
DEBT_ID=$(extract_id "$DEBT_RESP")

if [[ -n "$DEBT_ID" ]]; then
    test_endpoint "Update debt" PUT "$API/debts/$DEBT_ID" 200 "${AUTH[@]}" \
        -d "{\"name\":\"Updated Loan\",\"type\":\"PERSONAL_LOAN\",\"originalAmount\":50000,\"startDate\":\"$TODAY\",\"icon\":\"💳\"}" > /dev/null
    test_endpoint "Record debt payment" PATCH "$API/debts/$DEBT_ID/payment" 200 "${AUTH[@]}" \
        -d '{"amount":10000}' > /dev/null
    test_endpoint "Delete debt" DELETE "$API/debts/$DEBT_ID" 204 "${AUTH[@]}" > /dev/null
fi

# ─── Investment Endpoints ────────────────────────────────────────
echo ""
yellow "▸ Investment Endpoints"
test_endpoint "Get investments" GET "$API/investments" 200 "${AUTH[@]}" > /dev/null

INV_RESP=$(test_endpoint "Create investment" POST "$API/investments" 201 "${AUTH[@]}" \
    -d "{\"name\":\"API Stock\",\"type\":\"STOCKS\",\"investedAmount\":25000,\"currentValue\":28000,\"purchaseDate\":\"$TODAY\",\"icon\":\"📈\"}")
INV_ID=$(extract_id "$INV_RESP")

if [[ -n "$INV_ID" ]]; then
    test_endpoint "Update investment" PUT "$API/investments/$INV_ID" 200 "${AUTH[@]}" \
        -d "{\"name\":\"Updated Stock\",\"type\":\"STOCKS\",\"investedAmount\":25000,\"currentValue\":30000,\"purchaseDate\":\"$TODAY\",\"icon\":\"📈\"}" > /dev/null
    test_endpoint "Delete investment" DELETE "$API/investments/$INV_ID" 204 "${AUTH[@]}" > /dev/null
fi

# ─── Analytics Endpoints ─────────────────────────────────────────
echo ""
yellow "▸ Analytics Endpoints"
test_endpoint "Dashboard data" GET "$API/dashboard" 200 "${AUTH[@]}" > /dev/null
test_endpoint "Net worth" GET "$API/analytics/net-worth" 200 "${AUTH[@]}" > /dev/null
test_endpoint "Monthly summary" GET "$API/analytics/monthly-summary" 200 "${AUTH[@]}" > /dev/null
test_endpoint "Category breakdown" GET "$API/analytics/category-breakdown" 200 "${AUTH[@]}" > /dev/null

# ─── Report Endpoints ────────────────────────────────────────────
echo ""
yellow "▸ Report Endpoints"
test_endpoint "Email income report" POST "$API/reports/email/income" 200 "${AUTH[@]}" > /dev/null
test_endpoint "Email expense report" POST "$API/reports/email/expense" 200 "${AUTH[@]}" > /dev/null

# ─── Cleanup ─────────────────────────────────────────────────────
if [[ -n "${CAT_ID:-}" ]]; then
    curl -s -o /dev/null -X DELETE "$API/categories/$CAT_ID" "${AUTH[@]}" 2>/dev/null || true
fi
if [[ -n "${EXP_CAT_ID:-}" ]]; then
    curl -s -o /dev/null -X DELETE "$API/categories/$EXP_CAT_ID" "${AUTH[@]}" 2>/dev/null || true
fi

# ─── Report ───────────────────────────────────────────────────────
echo ""
yellow "═══════════════════════════════════════════════════"
if [[ $FAIL -eq 0 ]]; then
    green "  ALL $PASS / $TOTAL TESTS PASSED ($SKIP SKIPPED)"
else
    red "  $FAIL FAILED, $PASS PASSED, $SKIP SKIPPED (of $TOTAL total)"
fi
yellow "  Results saved to: $LOG_FILE"
yellow "═══════════════════════════════════════════════════"
echo ""

exit $FAIL
