-- ─── Verify all users ────────────────────────────────────────────────────────
SELECT id, username, email, role, enabled
FROM users
ORDER BY id;
