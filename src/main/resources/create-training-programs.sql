-- Create training_programs table from scratch
-- This script creates a clean training_programs table

CREATE TABLE IF NOT EXISTS training_programs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    
    -- New fields
    training_topic_id BIGINT,
    training_name_id BIGINT,
    training_name VARCHAR(200),
    training_date DATE,
    training_level VARCHAR(20),
    training_category_id BIGINT,
    faculty_name VARCHAR(200),
    coordinator_id BIGINT,
    training_type VARCHAR(20),
    exam_type VARCHAR(30),
    has_article_material BOOLEAN DEFAULT FALSE,
    has_video_material BOOLEAN DEFAULT FALSE,
    has_slide_material BOOLEAN DEFAULT FALSE,
    thumbnail_image_path VARCHAR(500),
    
    -- Legacy fields
    category VARCHAR(100),
    duration_hours INT,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    max_participants INT,
    prerequisites TEXT,
    learning_objectives TEXT,
    created_by_id BIGINT NOT NULL,
    instructor_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign keys
    FOREIGN KEY (training_topic_id) REFERENCES training_topics(id) ON DELETE SET NULL,
    FOREIGN KEY (training_name_id) REFERENCES training_names(id) ON DELETE SET NULL,
    FOREIGN KEY (training_category_id) REFERENCES training_category_master(id) ON DELETE SET NULL,
    FOREIGN KEY (coordinator_id) REFERENCES coordinators(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (instructor_id) REFERENCES users(id) ON DELETE SET NULL,
    
    -- Indexes
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_created_by (created_by_id),
    INDEX idx_instructor (instructor_id),
    INDEX idx_training_date (training_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
