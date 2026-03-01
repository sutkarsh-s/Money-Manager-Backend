from pydantic import BaseModel
from typing import Optional


class InsightResponse(BaseModel):
    summary: str
    recommendations: list[str]
    risk_level: str
    confidence: float


class AnomalyItem(BaseModel):
    transaction_id: int
    description: str
    amount: float
    expected_range: dict[str, float]
    severity: str


class AnomalyResponse(BaseModel):
    anomalies: list[AnomalyItem]
    total_anomalies: int
    analysis_period: str


class ForecastItem(BaseModel):
    month: str
    predicted_amount: float
    lower_bound: float
    upper_bound: float


class ForecastResponse(BaseModel):
    forecasts: list[ForecastItem]
    model_type: str
    confidence_interval: float


class AdvisorResponse(BaseModel):
    score: int
    grade: str
    summary: str
    areas: list[dict]
