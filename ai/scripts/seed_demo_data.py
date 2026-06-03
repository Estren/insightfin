"""Seed the local core-api with a realistic demo user for Coach Agent testing.

Creates ONE user (`coach-demo@insightfin.app`) with 3 months of transactions,
budgets, goals, and a handful of AI feedbacks (so the navbar bell dropdown
has notifications to click on without waiting for the monthly cron). Designed
to give the Coach Agent a coherent story to tell across the 5 anchor
questions from the Agents League plan (`.claude/docs/agents-league/plan.md`).

Budget-alert notifications (kind=BUDGET_ALERT) are *not* directly seeded:
they fire from the Kafka consumer when transactions cross 50/80/100% of a
budget. They appear automatically if Kafka is up locally during the seed.

Designed narrative:
    - March 2026: "good" month — high savings, all budgets within limit
    - April 2026: "decent" month — no freelance, food slightly up
    - May 2026:   "bad" month — Lazer budget blown, savings drop

Usage (from `ai/` with venv active):

    python -m scripts.seed_demo_data           # seed (idempotent only for new users)
    python -m scripts.seed_demo_data --reset   # wipe + re-seed for guaranteed clean state

Without `--reset`, re-running the script accumulates duplicates because the
API has no natural unique key for transactions/budgets. Use `--reset` whenever
you want a clean slate (e.g. before recording the demo video).

Prerequisites:
    1. core-api running locally at http://localhost:8080
    2. Email verification disabled in dev — set in core-api/.env:
           EMAIL_VERIFICATION_REQUIRED=false
       (Quarkus dev mode reads core-api/.env automatically.)
"""

from __future__ import annotations

import argparse
import asyncio
import sys
from datetime import date

import httpx

# Force UTF-8 on stdout/stderr so the checkmark/cross/info glyphs render on
# Windows consoles (default cp1252 can't encode them and raises mid-run).
sys.stdout.reconfigure(encoding="utf-8")
sys.stderr.reconfigure(encoding="utf-8")

API_BASE = "http://localhost:8080/api"
# Internal endpoints (consumed only by the AI service) are NOT under /api —
# they're mounted at the root path. The seed uses both: /api/* for the public
# routes it calls as the demo user, and INTERNAL_BASE for /internal/feedbacks.
INTERNAL_BASE = "http://localhost:8080"

DEMO_USER = {
    "name": "Coach Demo",
    "email": "coach-demo@insightfin.app",
    "password": "DemoPass123!",
}

CATEGORIES = [
    {"name": "Salário", "type": "INCOME", "icon": "wallet", "color": "#10B981"},
    {"name": "Freelance", "type": "INCOME", "icon": "briefcase", "color": "#22C55E"},
    {"name": "Moradia", "type": "EXPENSE", "icon": "home", "color": "#6366F1"},
    {"name": "Alimentação", "type": "EXPENSE", "icon": "shopping-cart", "color": "#F59E0B"},
    {"name": "Transporte", "type": "EXPENSE", "icon": "car", "color": "#0EA5E9"},
    {"name": "Lazer", "type": "EXPENSE", "icon": "music", "color": "#EC4899"},
    {"name": "Saúde", "type": "EXPENSE", "icon": "heart", "color": "#EF4444"},
    {"name": "Assinaturas", "type": "EXPENSE", "icon": "repeat", "color": "#8B5CF6"},
]

# Each entry: (category_name, type, amount, date, description)
TRANSACTIONS: list[tuple[str, str, float, str, str]] = [
    # === MARCH 2026 — good month ===
    ("Salário", "INCOME", 6500, "2026-03-05", "Salário março"),
    ("Freelance", "INCOME", 800, "2026-03-15", "Projeto consultoria"),
    ("Moradia", "EXPENSE", 1800, "2026-03-10", "Aluguel"),
    ("Alimentação", "EXPENSE", 320, "2026-03-08", "Supermercado mensal"),
    ("Alimentação", "EXPENSE", 180, "2026-03-14", "Mercado semanal"),
    ("Alimentação", "EXPENSE", 150, "2026-03-22", "Restaurante"),
    ("Alimentação", "EXPENSE", 90, "2026-03-27", "Delivery"),
    ("Alimentação", "EXPENSE", 60, "2026-03-30", "Padaria"),
    ("Transporte", "EXPENSE", 220, "2026-03-05", "Combustível"),
    ("Transporte", "EXPENSE", 130, "2026-03-19", "Uber + transporte"),
    ("Lazer", "EXPENSE", 120, "2026-03-12", "Cinema com amigos"),
    ("Lazer", "EXPENSE", 80, "2026-03-25", "Show"),
    ("Saúde", "EXPENSE", 150, "2026-03-18", "Farmácia + consulta"),
    ("Assinaturas", "EXPENSE", 39.90, "2026-03-01", "Netflix"),
    ("Assinaturas", "EXPENSE", 21.90, "2026-03-01", "Spotify"),
    # === APRIL 2026 — decent month ===
    ("Salário", "INCOME", 6500, "2026-04-05", "Salário abril"),
    ("Moradia", "EXPENSE", 1800, "2026-04-10", "Aluguel"),
    ("Alimentação", "EXPENSE", 380, "2026-04-08", "Supermercado mensal"),
    ("Alimentação", "EXPENSE", 220, "2026-04-15", "Mercado + feira"),
    ("Alimentação", "EXPENSE", 180, "2026-04-22", "Restaurante aniversário"),
    ("Alimentação", "EXPENSE", 120, "2026-04-26", "Delivery"),
    ("Alimentação", "EXPENSE", 50, "2026-04-28", "Padaria"),
    ("Transporte", "EXPENSE", 260, "2026-04-06", "Combustível"),
    ("Transporte", "EXPENSE", 160, "2026-04-20", "Uber + transporte"),
    ("Lazer", "EXPENSE", 150, "2026-04-14", "Bar com amigos"),
    ("Lazer", "EXPENSE", 140, "2026-04-27", "Cinema + lanche"),
    ("Saúde", "EXPENSE", 100, "2026-04-12", "Farmácia"),
    ("Assinaturas", "EXPENSE", 39.90, "2026-04-01", "Netflix"),
    ("Assinaturas", "EXPENSE", 21.90, "2026-04-01", "Spotify"),
    # === MAY 2026 — bad month: Lazer blown, savings drop ===
    ("Salário", "INCOME", 6500, "2026-05-05", "Salário maio"),
    ("Moradia", "EXPENSE", 1800, "2026-05-10", "Aluguel"),
    ("Alimentação", "EXPENSE", 410, "2026-05-08", "Supermercado mensal"),
    ("Alimentação", "EXPENSE", 240, "2026-05-15", "Mercado"),
    ("Alimentação", "EXPENSE", 200, "2026-05-20", "Delivery (semana corrida)"),
    ("Alimentação", "EXPENSE", 160, "2026-05-22", "Delivery"),
    ("Alimentação", "EXPENSE", 90, "2026-05-25", "Padaria + café"),
    ("Transporte", "EXPENSE", 280, "2026-05-06", "Combustível"),
    ("Transporte", "EXPENSE", 200, "2026-05-21", "Uber + reparo"),
    # Lazer estoura: orçamento de 300, gasta 450
    ("Lazer", "EXPENSE", 180, "2026-05-09", "Show com amigos"),
    ("Lazer", "EXPENSE", 150, "2026-05-16", "Restaurante + bar"),
    ("Lazer", "EXPENSE", 120, "2026-05-23", "Cinema + jantar"),
    ("Saúde", "EXPENSE", 180, "2026-05-12", "Consulta + exames"),
    ("Saúde", "EXPENSE", 100, "2026-05-19", "Medicamentos"),
    ("Assinaturas", "EXPENSE", 39.90, "2026-05-01", "Netflix"),
    ("Assinaturas", "EXPENSE", 21.90, "2026-05-01", "Spotify"),
]

# Budgets for May 2026 (current month — what the agent will discuss)
BUDGETS: list[tuple[str, float, str]] = [
    ("Alimentação", 1000, "2026-05"),  # ~110% — over
    ("Transporte", 500, "2026-05"),    # ~96% — within
    ("Lazer", 300, "2026-05"),         # ~150% — blown
    ("Moradia", 2000, "2026-05"),      # 90% — within
]

# AI feedbacks — surfaced as AI_FEEDBACK notifications in the bell dropdown.
# Created directly via /internal/feedbacks (same path the AI orchestrator uses
# in prod) because the local seed doesn't run the monthly cron. All start
# unread, so the navbar badge will show them and the deep-link flow has
# something to click.
#
# Each entry: (type, title, content, reference_month)
AI_FEEDBACKS: list[tuple[str, str, str, str | None]] = [
    (
        "MONTHLY_REPORT",
        "Março fechou no azul",
        (
            "Você economizou cerca de R$ 3.400 em março — mês forte: salário cheio + freelance, "
            "orçamentos todos dentro do limite. Continue assim e a reserva de emergência fecha o "
            "ano completa."
        ),
        "2026-03",
    ),
    (
        "HEALTH_SCORE",
        "Health score: 82",
        (
            "Sua saúde financeira em março ficou em 82/100. Pontos fortes: receita diversificada "
            "(salário + freelance) e zero estouro de orçamento. Atenção: Alimentação subiu 18% vs "
            "o mês anterior."
        ),
        "2026-03",
    ),
    (
        "MONTHLY_REPORT",
        "Abril ok, sem freelance",
        (
            "Abril sem renda extra: sobraram aproximadamente R$ 1.800. Tudo dentro do esperado, "
            "exceto Alimentação que segue em alta. Vale revisar o quanto vai em delivery."
        ),
        "2026-04",
    ),
    (
        "ALERT",
        "Lazer estourou em maio",
        (
            "Seu orçamento de Lazer (R$ 300) chegou a 150% em maio — gasto total R$ 450. Os "
            "principais lançamentos foram Show, Restaurante+bar e Cinema+jantar. Considere "
            "remanejar para o próximo mês."
        ),
        "2026-05",
    ),
    (
        "HEALTH_SCORE",
        "Health score caiu para 62",
        (
            "Sua saúde financeira em maio ficou em 62/100 (queda de 20 pontos vs março). "
            "Causas principais: estouro do orçamento de Lazer, Alimentação acima do teto e "
            "redução da contribuição para a reserva. Nada grave, mas atenção pro próximo mês."
        ),
        "2026-05",
    ),
    (
        "GOAL_PROJECTION",
        "Viagem Europa: ritmo abaixo do necessário",
        (
            "No ritmo atual, a meta Viagem Europa só seria atingida em fevereiro/2027 — quatro "
            "meses depois da deadline (set/2026). Aumentar a contribuição mensal em ~R$ 750 "
            "reequilibra a projeção."
        ),
        "2026-05",
    ),
]

# Current-month feedbacks (dynamic so they show up on the /feedbacks page and
# the dashboard AI Insights card, which both default to today's month).
# Without these, the seeded data is invisible there even though it appears in
# the notification bell (which doesn't filter by month).
_CURRENT_MONTH = date.today().strftime("%Y-%m")

AI_FEEDBACKS_CURRENT_MONTH: list[tuple[str, str, str, str]] = [
    (
        "MONTHLY_REPORT",
        "Revisão do mês anterior",
        (
            "Maio fechou em alerta: orçamento de Lazer estourou (150%) e Alimentação ficou em "
            "110%. O mês corrente é o momento de reequilibrar — começar com um teto menor de "
            "Lazer e priorizar refeições em casa nas duas primeiras semanas costuma resolver."
        ),
        _CURRENT_MONTH,
    ),
    (
        "HEALTH_SCORE",
        "Seu score começou o mês em 65",
        (
            "Score atual ponderado: 65/100. A queda vem majoritariamente do estouro recorrente "
            "em Lazer nos últimos 30 dias. Manter o orçamento atual de R$ 300 com gasto até R$ "
            "250 sobe o score para ~75 até o fim do mês."
        ),
        _CURRENT_MONTH,
    ),
    (
        "ALERT",
        "Padrão recorrente em delivery",
        (
            "Detectamos um padrão: gastos de delivery cresceram 40% nos últimos 60 dias. Esses "
            "lançamentos somam ~R$ 480/mês, equivalente a 4× a sua contribuição extra mensal "
            "pra Viagem Europa. Vale conversar com o Coach pra discutir alternativas."
        ),
        _CURRENT_MONTH,
    ),
]

# Goals + their contributions over time
GOALS: list[dict] = [
    {
        "title": "Reserva de emergência",
        "targetAmount": 10000,
        "deadline": "2026-12-31",
        "contributions": [
            (1500, "2026-03-15"),
            (2000, "2026-04-15"),
            (1500, "2026-05-15"),
        ],  # 5000/10000 = 50%, on track
    },
    {
        "title": "Viagem Europa",
        "targetAmount": 8000,
        "deadline": "2026-09-30",
        "contributions": [
            (500, "2026-03-20"),
            (800, "2026-04-20"),
        ],  # 1300/8000 = 16%, behind
    },
]


async def register_or_login(http: httpx.AsyncClient) -> str:
    """Return a JWT for the demo user, registering them if necessary."""
    register_resp = await http.post("/auth/register", json=DEMO_USER)
    if register_resp.status_code == 201:
        print("✓ User registered")
        return register_resp.json()["accessToken"]
    if register_resp.status_code == 409:
        print("ℹ User already exists, logging in")
    else:
        # 403 commonly means EMAIL_VERIFICATION_REQUIRED=true blocked login
        print(f"⚠ register returned {register_resp.status_code}: {register_resp.text}")

    login_resp = await http.post(
        "/auth/login",
        json={"email": DEMO_USER["email"], "password": DEMO_USER["password"]},
    )
    login_resp.raise_for_status()
    print("✓ Logged in")
    return login_resp.json()["accessToken"]


async def delete_existing_demo_user(http: httpx.AsyncClient) -> None:
    """Wipe the demo user (cascades to all their data) so seeding starts clean.

    No-op when the user doesn't exist yet.
    """
    login_resp = await http.post(
        "/auth/login",
        json={"email": DEMO_USER["email"], "password": DEMO_USER["password"]},
    )
    # core-api returns 400 ("Invalid email or password") when the user simply
    # doesn't exist; 401 covers other auth-rejection variants.
    if login_resp.status_code in (400, 401):
        print("ℹ Demo user doesn't exist yet — nothing to reset")
        return
    login_resp.raise_for_status()
    token = login_resp.json()["accessToken"]
    headers = {"Authorization": f"Bearer {token}"}

    delete_resp = await http.delete("/users/me", headers=headers)
    delete_resp.raise_for_status()
    print("✓ Wiped existing demo user (and cascaded data)")


async def create_categories(
    http: httpx.AsyncClient, headers: dict
) -> dict[str, str]:
    """Create all categories, return name → id map."""
    name_to_id: dict[str, str] = {}
    for cat in CATEGORIES:
        resp = await http.post("/categories", headers=headers, json=cat)
        resp.raise_for_status()
        name_to_id[cat["name"]] = resp.json()["id"]
    print(f"✓ Created {len(CATEGORIES)} categories")
    return name_to_id


async def create_transactions(
    http: httpx.AsyncClient, headers: dict, cat_ids: dict[str, str]
) -> None:
    for cat_name, t_type, amount, t_date, desc in TRANSACTIONS:
        payload = {
            "categoryId": cat_ids[cat_name],
            "type": t_type,
            "amount": amount,
            "description": desc,
            "date": t_date,
        }
        resp = await http.post("/transactions", headers=headers, json=payload)
        resp.raise_for_status()
    print(f"✓ Created {len(TRANSACTIONS)} transactions")


async def create_budgets(
    http: httpx.AsyncClient, headers: dict, cat_ids: dict[str, str]
) -> None:
    for cat_name, amount, month in BUDGETS:
        payload = {
            "categoryId": cat_ids[cat_name],
            "amount": amount,
            "month": month,
        }
        resp = await http.post("/budgets", headers=headers, json=payload)
        resp.raise_for_status()
    print(f"✓ Created {len(BUDGETS)} budgets")


async def create_goals(http: httpx.AsyncClient, headers: dict) -> None:
    for goal in GOALS:
        payload = {
            "title": goal["title"],
            "targetAmount": goal["targetAmount"],
            "deadline": goal["deadline"],
        }
        resp = await http.post("/goals", headers=headers, json=payload)
        resp.raise_for_status()
        goal_id = resp.json()["id"]

        for amount, contrib_date in goal["contributions"]:
            contrib_resp = await http.post(
                f"/goals/{goal_id}/contributions",
                headers=headers,
                json={"amount": amount, "date": contrib_date},
            )
            contrib_resp.raise_for_status()
    print(f"✓ Created {len(GOALS)} goals with contributions")


async def fetch_user_id(http: httpx.AsyncClient, headers: dict) -> str:
    """Fetch /users/me to get the demo user's UUID for the test_coach script."""
    resp = await http.get("/users/me", headers=headers)
    resp.raise_for_status()
    return resp.json()["id"]


async def create_ai_feedbacks(user_id: str) -> None:
    """POST AI feedbacks via the internal endpoint — no auth header needed."""
    all_feedbacks = AI_FEEDBACKS + AI_FEEDBACKS_CURRENT_MONTH
    async with httpx.AsyncClient(base_url=INTERNAL_BASE, timeout=30.0) as http:
        for fb_type, title, content, ref_month in all_feedbacks:
            payload = {
                "userId": user_id,
                "type": fb_type,
                "title": title,
                "content": content,
                "referenceMonth": ref_month,
            }
            resp = await http.post("/internal/feedbacks", json=payload)
            # 409 = duplicate (userId, type, month) — fine on re-runs without --reset
            if resp.status_code not in (201, 409):
                resp.raise_for_status()
    print(
        f"✓ Created {len(all_feedbacks)} AI feedbacks "
        f"({len(AI_FEEDBACKS_CURRENT_MONTH)} in current month {_CURRENT_MONTH})"
    )


async def main(reset: bool) -> int:
    async with httpx.AsyncClient(base_url=API_BASE, timeout=30.0) as http:
        if reset:
            await delete_existing_demo_user(http)
        token = await register_or_login(http)
        headers = {"Authorization": f"Bearer {token}"}

        cat_ids = await create_categories(http, headers)
        await create_transactions(http, headers, cat_ids)
        await create_budgets(http, headers, cat_ids)
        await create_goals(http, headers)

        user_id = await fetch_user_id(http, headers)
        await create_ai_feedbacks(user_id)
        print()
        print("=" * 60)
        print(f"Demo user_id: {user_id}")
        print(f"Email:        {DEMO_USER['email']}")
        print(f"Password:     {DEMO_USER['password']}")
        print("=" * 60)
        print()
        print("Test the agent with:")
        print(
            f'  python -m scripts.test_coach_local {user_id} '
            f'"por que meu health score caiu este mês?"'
        )

    return 0


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument(
        "--reset",
        action="store_true",
        help="Wipe the demo user (and all their data) before seeding — clean slate.",
    )
    args = parser.parse_args()
    try:
        sys.exit(asyncio.run(main(reset=args.reset)))
    except httpx.HTTPStatusError as exc:
        print(f"\n❌ HTTP {exc.response.status_code}: {exc.response.text}", file=sys.stderr)
        sys.exit(1)
