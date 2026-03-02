CREATE TABLE IF NOT EXISTS todos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    assignee VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    deadline DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    deleted_at TIMESTAMP NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS mom_auth (
    id INT PRIMARY KEY,
    secret VARCHAR(100) NOT NULL,
    approver_code VARCHAR(20) NOT NULL DEFAULT 'MOTHER',
    secret_initialized BOOLEAN NOT NULL DEFAULT TRUE
);

ALTER TABLE mom_auth ADD COLUMN IF NOT EXISTS approver_code VARCHAR(20) NOT NULL DEFAULT 'MOTHER';
ALTER TABLE mom_auth ADD COLUMN IF NOT EXISTS secret_initialized BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE mom_auth
SET approver_code = 'MOTHER'
WHERE approver_code IS NULL OR TRIM(approver_code) = '';

UPDATE mom_auth
SET secret_initialized = CASE WHEN secret IS NULL OR TRIM(secret) = '' THEN FALSE ELSE TRUE END
WHERE secret_initialized IS NULL;

INSERT INTO mom_auth (id, secret, approver_code, secret_initialized)
SELECT 1, '', 'MOTHER', FALSE
WHERE NOT EXISTS (SELECT 1 FROM mom_auth WHERE id = 1);
