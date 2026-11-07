-- ============================================
-- Garage team management: custom roles, team members, invites
-- ============================================
-- Hibernate ddl-auto=update creates these automatically; run this script on
-- environments where schema changes are applied by hand.
--
-- Existing data (default roles per garage + a membership for every
-- technician_garage link) is backfilled on application startup by
-- GarageTeamBackfillRunner, so no data inserts are needed here.
--
-- Table names: roles live in garage_team_role / garage_team_role_permission, NOT
-- garage_role. Older databases still have a legacy garage_role table
-- (role_code, description) from an entity removed long ago; leave it alone.
--
-- If a build that used the old names was already started against this database,
-- it left an empty garage_role_permission table behind. It is unused and can be
-- dropped once you've confirmed it is empty:
--   SELECT COUNT(*) FROM garage_role_permission;   -- expect 0
--   DROP TABLE garage_role_permission;

-- Roles defined by each garage
CREATE TABLE garage_team_role (
    id INT AUTO_INCREMENT PRIMARY KEY,
    garage_id INT NOT NULL,
    name VARCHAR(60) NOT NULL,
    description VARCHAR(255),
    is_system BIT(1) NOT NULL DEFAULT b'0',
    created_at DATETIME(6),
    updated_at DATETIME(6),
    CONSTRAINT uk_garage_team_role_name UNIQUE (garage_id, name),
    CONSTRAINT fk_garage_team_role_garage FOREIGN KEY (garage_id) REFERENCES garage(id) ON DELETE CASCADE
);

-- Permissions granted by a role (values of the GaragePermission enum)
CREATE TABLE garage_team_role_permission (
    role_id INT NOT NULL,
    permission VARCHAR(40) NOT NULL,
    PRIMARY KEY (role_id, permission),
    CONSTRAINT fk_garage_team_role_permission_role FOREIGN KEY (role_id) REFERENCES garage_team_role(id) ON DELETE CASCADE
);

-- A person's membership of a garage team
CREATE TABLE garage_team_member (
    id INT AUTO_INCREMENT PRIMARY KEY,
    garage_id INT NOT NULL,
    technician_id INT NOT NULL,
    role_id INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',   -- ACTIVE | SUSPENDED | REMOVED
    invited_by INT NULL,                            -- AutoAdda user id of the inviter
    joined_at DATETIME(6),
    updated_at DATETIME(6),
    CONSTRAINT uk_garage_team_member UNIQUE (garage_id, technician_id),
    CONSTRAINT fk_team_member_garage FOREIGN KEY (garage_id) REFERENCES garage(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_member_technician FOREIGN KEY (technician_id) REFERENCES technician(id),
    CONSTRAINT fk_team_member_role FOREIGN KEY (role_id) REFERENCES garage_team_role(id),
    INDEX idx_team_member_role (role_id)
);

-- Invites reuse the garage/technician request table
ALTER TABLE requesting_garage_and_technician
    ADD COLUMN role_id INT NULL,
    ADD COLUMN invited_phone VARCHAR(30) NULL,
    ADD COLUMN invited_by INT NULL,
    ADD CONSTRAINT fk_request_role FOREIGN KEY (role_id) REFERENCES garage_team_role(id) ON DELETE SET NULL,
    ADD INDEX idx_request_invited_phone (invited_phone);

-- Phone-only invites have no technician yet
ALTER TABLE requesting_garage_and_technician MODIFY technician_id INT NULL;

-- New approval status value (column is VARCHAR via @Enumerated(STRING); listed for reference)
-- approval_status: ACCEPTED | HOLD | REJECTED | CANCELLED
