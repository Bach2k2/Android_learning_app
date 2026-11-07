ALTER TABLE app_user
    ADD COLUMN role VARCHAR(16) NOT NULL DEFAULT 'USER';

ALTER TABLE app_user
    ADD CONSTRAINT ck_app_user_role CHECK (role IN ('ADMIN', 'USER'));

CREATE INDEX idx_app_user_role ON app_user(role);
