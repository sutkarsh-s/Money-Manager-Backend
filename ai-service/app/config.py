from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    database_url: str = "postgresql+asyncpg://user:password@localhost:5432/money_manager"
    rabbitmq_url: str = "amqp://guest:guest@localhost:5672/"
    frontend_url: str = "http://localhost:5173"
    jwt_secret: str = "changeme"

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"


settings = Settings()
