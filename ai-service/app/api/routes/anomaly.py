from fastapi import APIRouter
from app.schemas.responses import AnomalyResponse, AnomalyItem

router = APIRouter()


@router.get("/detect", response_model=AnomalyResponse)
async def detect_anomalies():
    return AnomalyResponse(
        anomalies=[
            AnomalyItem(
                transaction_id=1,
                description="Unusually high grocery expense",
                amount=15000.00,
                expected_range={"min": 3000.0, "max": 8000.0},
                severity="HIGH"
            )
        ],
        total_anomalies=1,
        analysis_period="2026-02"
    )
