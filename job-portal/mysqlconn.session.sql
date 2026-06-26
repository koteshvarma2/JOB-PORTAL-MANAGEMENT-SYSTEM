-- ─── Fix existing users with NULL or empty role ───────────────────────────────
-- Run this once against job_portal_db to repair any rows created before the fix.

UPDATE users
SET role = 'ROLE_STUDENT'
WHERE role IS NULL OR TRIM(role) = '';

-- ─── Verify role distribution ─────────────────────────────────────────────────
SELECT role, COUNT(*) AS total
FROM users
GROUP BY role;

-- ─── Verify all users ────────────────────────────────────────────────────────
SELECT id, username, email, role, enabled
FROM users
ORDER BY id;