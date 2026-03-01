from fastapi import APIRouter
from app.schemas.responses import ForecastResponse, ForecastItem

router = APIRouter()


@router.get("/expenses", response_model=ForecastResponse)
async def forecast_expenses():
    return ForecastResponse(
        forecasts=[
            ForecastItem(month="2026-03", predicted_amount=45000.0, lower_bound=38000.0, upper_bound=52000.0),
            ForecastItem(month="2026-04", predicted_amount=43000.0, lower_bound=36000.0, upper_bound=50000.0),
            ForecastItem(month="2026-05", predicted_amount=44000.0, lower_bound=37000.0, upper_bound=51000.0),
        ],
        model_type="Prophet",
        confidence_interval=0.95
    )


@router.get("/income", response_model=ForecastResponse)
async def forecast_income():
    return ForecastResponse(
        forecasts=[
            ForecastItem(month="2026-03", predicted_amount=75000.0, lower_bound=70000.0, upper_bound=80000.0),
            ForecastItem(month="2026-04", predicted_amount=75000.0, lower_bound=70000.0, upper_bound=80000.0),
            ForecastItem(month="2026-05", predicted_amount=76000.0, lower_bound=71000.0, upper_bound=81000.0),
        ],
        model_type="Prophet",
        confidence_interval=0.95
    )
