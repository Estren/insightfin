-- Composite indexes that make cursor pagination O(log n) seeks.
-- Without (id) as tiebreak in the index, the keyset query
--   WHERE (created_at < ?) OR (created_at = ? AND id < ?)
-- still works but the DB scans equal-timestamp rows to apply the id filter.

-- ai_feedbacks: replace the existing (user_id, created_at DESC) index
-- with one that also covers the id tiebreak.
DROP INDEX IF EXISTS idx_ai_feedbacks_user;
CREATE INDEX idx_ai_feedbacks_user_created_id
    ON ai_feedbacks (user_id, created_at DESC, id DESC);

-- budget_alerts had no index for this access pattern (the existing
-- idx_budget_alerts_user_triggered uses triggered_at, not created_at).
CREATE INDEX idx_budget_alerts_user_created_id
    ON budget_alerts (user_id, created_at DESC, id DESC);
