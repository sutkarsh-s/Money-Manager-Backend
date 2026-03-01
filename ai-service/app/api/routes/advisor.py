from fastapi import APIRouter
from app.schemas.responses import AdvisorResponse

router = APIRouter()


@router.get("/financial-health", response_model=AdvisorResponse)
async def get_financial_health():
    return AdvisorResponse(
        score=72,
        grade="B",
        summary="Your overall financial health is good with room for improvement.",
        areas=[
            {"area": "Savings", "score": 65, "advice": "Increase your monthly savings rate to 20%"},
            {"area": "Debt Management", "score": 80, "advice": "Your debt-to-income ratio is healthy"},
            {"area": "Investment", "score": 60, "advice": "Consider diversifying your portfolio"},
            {"area": "Emergency Fund", "score": 55, "advice": "Build up 6 months of expenses"},
            {"area": "Budget Adherence", "score": 85, "advice": "Great job sticking to your budgets"}
        ]
    )


@router.get("/investment-suggestions", response_model=AdvisorResponse)
async def get_investment_suggestions():
    return AdvisorResponse(
        score=0,
        grade="N/A",
        summary="Based on your risk profile and financial goals:",
        areas=[
            {"area": "Mutual Funds", "score": 80, "advice": "SIP in index funds for long-term wealth building"},
            {"area": "Fixed Deposits", "score": 70, "advice": "Keep 20% in FDs for stability"},
            {"area": "Gold", "score": 60, "advice": "Allocate 5-10% as inflation hedge"},
            {"area": "Stocks", "score": 50, "advice": "Start with blue-chip stocks if new to equity"}
        ]
    )
