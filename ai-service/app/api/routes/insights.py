from fastapi import APIRouter
from app.schemas.responses import InsightResponse

router = APIRouter()


@router.get("/spending", response_model=InsightResponse)
async def get_spending_insights():
    return InsightResponse(
        summary="Your spending patterns are healthy.",
        recommendations=[
            "Consider reducing dining out expenses by 15%",
            "Your utility bills are above average for your income bracket",
            "Great job keeping entertainment expenses under control"
        ],
        risk_level="LOW",
        confidence=0.85
    )


@router.get("/savings", response_model=InsightResponse)
async def get_savings_insights():
    return InsightResponse(
        summary="Your savings rate could be improved.",
        recommendations=[
            "Aim to save at least 20% of your monthly income",
            "Consider automating your savings transfers",
            "Your emergency fund should cover 6 months of expenses"
        ],
        risk_level="MEDIUM",
        confidence=0.78
    )
