"""Per-user throttle on the Coach chat endpoints."""

from __future__ import annotations

from uuid import uuid4

import pytest

from app import config as config_module
from app.coach_agent.api import throttle


@pytest.fixture(autouse=True)
def reset_state(monkeypatch):
    throttle.reset_for_tests()
    monkeypatch.setattr(config_module.settings, "coach_rate_limit_max_calls", 3)
    monkeypatch.setattr(config_module.settings, "coach_rate_limit_window_seconds", 3600)


class TestCheckQuota:
    def test_allows_up_to_max(self):
        user = uuid4()
        for _ in range(3):
            allowed, retry = throttle.check_quota(user)
            assert allowed is True
            assert retry == 0

    def test_rejects_after_max(self):
        user = uuid4()
        for _ in range(3):
            throttle.check_quota(user)
        allowed, retry = throttle.check_quota(user)
        assert allowed is False
        assert retry > 0

    def test_isolated_per_user(self):
        user_a, user_b = uuid4(), uuid4()
        for _ in range(3):
            throttle.check_quota(user_a)
        allowed, _ = throttle.check_quota(user_b)
        assert allowed is True

    def test_max_calls_zero_disables(self, monkeypatch):
        monkeypatch.setattr(config_module.settings, "coach_rate_limit_max_calls", 0)
        user = uuid4()
        for _ in range(10):
            allowed, _ = throttle.check_quota(user)
            assert allowed is True
