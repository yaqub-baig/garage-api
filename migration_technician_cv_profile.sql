-- ============================================
-- Technician CV Profile (step-wise form) + profile completion tracking
-- Database Schema Migration
-- All profile columns are optional (nullable); only technician_id is required.
-- ============================================

-- Technician: last name + stored profile completion
ALTER TABLE technician
    ADD COLUMN last_name VARCHAR(255) NULL AFTER first_name,
    ADD COLUMN profile_completion_percent INT DEFAULT 0,
    ADD COLUMN profile_completion_status VARCHAR(20) DEFAULT 'NOT_STARTED',
    ADD COLUMN profile_updated_at DATETIME NULL;

-- Backfill existing technicians (recalculated on their first profile save or completion GET)
UPDATE technician
SET profile_completion_percent = 0,
    profile_completion_status  = 'NOT_STARTED'
WHERE profile_completion_percent IS NULL
   OR profile_completion_status IS NULL;

-- Step 1 (personal details) + step 8 (emergency contact): one row per technician
CREATE TABLE technician_profile (
    id INT AUTO_INCREMENT PRIMARY KEY,
    technician_id INT NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(30),
    nationality VARCHAR(255),
    marital_status VARCHAR(30),
    alternate_phone VARCHAR(255),
    address_line VARCHAR(500),
    country VARCHAR(255),
    passport_number VARCHAR(255),
    passport_expiry DATE,
    visa_status VARCHAR(30),
    visa_expiry DATE,
    driving_license_number VARCHAR(255),
    professional_summary TEXT,
    emergency_contact_name VARCHAR(255),
    emergency_contact_relation VARCHAR(255),
    emergency_contact_phone VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT uk_technician_profile_technician UNIQUE (technician_id),
    CONSTRAINT fk_technician_profile_technician
        FOREIGN KEY (technician_id) REFERENCES technician(id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 2: qualifications
CREATE TABLE technician_qualification (
    id INT AUTO_INCREMENT PRIMARY KEY,
    technician_id INT NOT NULL,
    display_order INT,
    qualification_type VARCHAR(30),
    title VARCHAR(255),
    field_of_study VARCHAR(255),
    institution VARCHAR(255),
    country VARCHAR(255),
    start_date DATE,
    end_date DATE,
    is_current BOOLEAN,
    grade VARCHAR(255),
    certificate_file_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT fk_tech_qualification_technician
        FOREIGN KEY (technician_id) REFERENCES technician(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_tech_qualification_media
        FOREIGN KEY (certificate_file_id) REFERENCES media_file(id)
        ON DELETE SET NULL,
    INDEX idx_tech_qualification_technician (technician_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 3: certifications & licenses
CREATE TABLE technician_certification (
    id INT AUTO_INCREMENT PRIMARY KEY,
    technician_id INT NOT NULL,
    display_order INT,
    certification_type VARCHAR(30),
    name VARCHAR(255),
    issuing_organization VARCHAR(255),
    credential_id VARCHAR(255),
    issue_date DATE,
    expiry_date DATE,
    document_file_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT fk_tech_certification_technician
        FOREIGN KEY (technician_id) REFERENCES technician(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_tech_certification_media
        FOREIGN KEY (document_file_id) REFERENCES media_file(id)
        ON DELETE SET NULL,
    INDEX idx_tech_certification_technician (technician_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 4: free-text technical skills (skillSet/expertise use existing tables)
CREATE TABLE technician_skill (
    id INT AUTO_INCREMENT PRIMARY KEY,
    technician_id INT NOT NULL,
    display_order INT,
    skill_name VARCHAR(255),
    level VARCHAR(10),
    years_of_experience INT,
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT fk_tech_skill_technician
        FOREIGN KEY (technician_id) REFERENCES technician(id)
        ON DELETE CASCADE,
    INDEX idx_tech_skill_technician (technician_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 5: work history (jobs + career breaks explaining gaps)
CREATE TABLE technician_employment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    technician_id INT NOT NULL,
    display_order INT,
    entry_type VARCHAR(20) DEFAULT 'EMPLOYMENT',
    break_reason VARCHAR(30),
    break_description TEXT,
    employer_name VARCHAR(255),
    job_title VARCHAR(255),
    city VARCHAR(255),
    country VARCHAR(255),
    start_date DATE,
    end_date DATE,
    is_current BOOLEAN,
    responsibilities TEXT,
    reason_for_leaving VARCHAR(1000),
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT fk_tech_employment_technician
        FOREIGN KEY (technician_id) REFERENCES technician(id)
        ON DELETE CASCADE,
    INDEX idx_tech_employment_technician (technician_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 6: languages
CREATE TABLE technician_language (
    id INT AUTO_INCREMENT PRIMARY KEY,
    technician_id INT NOT NULL,
    display_order INT,
    language VARCHAR(255),
    proficiency VARCHAR(30),
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT fk_tech_language_technician
        FOREIGN KEY (technician_id) REFERENCES technician(id)
        ON DELETE CASCADE,
    INDEX idx_tech_language_technician (technician_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 7: documents
CREATE TABLE technician_document (
    id INT AUTO_INCREMENT PRIMARY KEY,
    technician_id INT NOT NULL,
    display_order INT,
    document_type VARCHAR(30),
    document_number VARCHAR(255),
    expiry_date DATE,
    file_id BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT fk_tech_document_technician
        FOREIGN KEY (technician_id) REFERENCES technician(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_tech_document_media
        FOREIGN KEY (file_id) REFERENCES media_file(id)
        ON DELETE SET NULL,
    INDEX idx_tech_document_technician (technician_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 8: references
CREATE TABLE technician_reference (
    id INT AUTO_INCREMENT PRIMARY KEY,
    technician_id INT NOT NULL,
    display_order INT,
    name VARCHAR(255),
    company VARCHAR(255),
    designation VARCHAR(255),
    relationship VARCHAR(255),
    phone VARCHAR(255),
    email VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT fk_tech_reference_technician
        FOREIGN KEY (technician_id) REFERENCES technician(id)
        ON DELETE CASCADE,
    INDEX idx_tech_reference_technician (technician_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Step 9: social profiles
CREATE TABLE technician_social_link (
    id INT AUTO_INCREMENT PRIMARY KEY,
    technician_id INT NOT NULL,
    display_order INT,
    platform VARCHAR(30),
    url VARCHAR(2048),
    handle VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT fk_tech_social_link_technician
        FOREIGN KEY (technician_id) REFERENCES technician(id)
        ON DELETE CASCADE,
    INDEX idx_tech_social_link_technician (technician_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
