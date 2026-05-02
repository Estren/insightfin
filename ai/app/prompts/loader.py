from pathlib import Path

_DIR = Path(__file__).parent

_FILES = {
    "MONTHLY_REPORT": "monthly_report.txt",
    "HEALTH_SCORE": "health_score.txt",
    "ALERT": "alert.txt",
    "GOAL_PROJECTION": "goal_projection.txt",
}

_cache: dict[str, str] = {}


def load(analysis_type: str) -> str:
    if analysis_type not in _cache:
        path = _DIR / _FILES[analysis_type]
        _cache[analysis_type] = path.read_text(encoding="utf-8")
    return _cache[analysis_type]
