from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # Azure OpenAI (standalone resource — used by the batch orchestrator)
    azure_openai_endpoint: str = ""
    azure_openai_key: str = ""
    azure_openai_model: str = "gpt-4o-mini"

    # Azure AI Foundry project (used by the Coach Agent)
    azure_foundry_project_endpoint: str = ""
    azure_foundry_api_key: str = ""
    azure_foundry_model: str = "gpt-4.1-mini"
    # Optional: if set, reuse this agent across runs. Otherwise create on demand.
    azure_foundry_agent_id: str = ""
    # Optional: if set, the Coach Agent uses Foundry IQ (file_search) to ground
    # answers in the financial education corpus. Empty disables grounding.
    azure_foundry_vector_store_id: str = ""

    # Core-API
    core_api_url: str = "http://core-api:8080"

    # Kafka
    kafka_bootstrap_servers: str = "kafka:9092"
    kafka_consumer_group: str = "ai-service"
    kafka_security_protocol: str = "PLAINTEXT"
    kafka_sasl_mechanism: str = "PLAIN"
    kafka_sasl_password: str = ""

    # Operational
    monthly_cron: str = "0 8 1 * *"
    max_llm_calls_per_day: int = 500
    analysis_batch_size: int = 10
    llm_timeout_seconds: int = 30

    # Observability — Sentry is off when the DSN is empty.
    sentry_dsn: str = ""
    sentry_environment: str = "production"

    model_config = {"env_file": ".env", "env_file_encoding": "utf-8", "extra": "ignore"}


settings = Settings()
