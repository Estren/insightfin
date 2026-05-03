from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # Azure OpenAI
    azure_openai_endpoint: str = ""
    azure_openai_key: str = ""
    azure_openai_model: str = "gpt-4o-mini"

    # Core-API
    core_api_url: str = "http://core-api:8080"

    # Kafka
    kafka_bootstrap_servers: str = "kafka:9092"
    kafka_consumer_group: str = "ai-service"

    # Operational
    monthly_cron: str = "0 8 1 * *"
    max_llm_calls_per_day: int = 500
    analysis_batch_size: int = 10
    llm_timeout_seconds: int = 30

    model_config = {"env_file": ".env", "env_file_encoding": "utf-8"}


settings = Settings()
