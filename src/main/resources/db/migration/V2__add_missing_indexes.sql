-- Adds the indexes identified in the scale-readiness audit. This is the first
-- migration Flyway actually executes against the live database (V1 is baselined,
-- not run) — these three CREATE INDEX statements are additive only, no data risk.

CREATE INDEX idx_employees_department_id ON employees(department_id);
CREATE INDEX idx_attendance_records_date ON attendance_records(date);
CREATE INDEX idx_notifications_recipient_id ON notifications(recipient_id);
