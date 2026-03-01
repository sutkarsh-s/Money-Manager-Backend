from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.routes import health, insights, anomaly, forecast, advisor
from app.config import settings

app = FastAPI(
    title="Money Manager AI Service",
    version="0.1.0",
    root_path="/api/v1.0/ai"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[settings.frontend_url],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(health.router, tags=["Health"])
app.include_router(insights.router, prefix="/insights", tags=["Insights"])
app.include_router(anomaly.router, prefix="/anomaly", tags=["Anomaly Detection"])
app.include_router(forecast.router, prefix="/forecast", tags=["Forecasting"])
app.include_router(advisor.router, prefix="/advisor", tags=["Financial Advisor"])
