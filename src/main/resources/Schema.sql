-- 1) FACULTIES TABLE (no FK yet)
CREATE TABLE faculties (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(100) UNIQUE NOT NULL,
                           description TEXT,
                           owner_id BIGINT, -- will add FK later
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2) USERS TABLE
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       avatar_url VARCHAR(255),
                       bio TEXT,
                       role VARCHAR(20) NOT NULL DEFAULT 'STUDENT', -- SUPER_ADMIN, FACULTY_ADMIN, STUDENT
                       faculty_id BIGINT REFERENCES faculties(id),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Auth fields
                       enabled BOOLEAN DEFAULT FALSE,
                       verification_otp VARCHAR(10),
                       otp_expiry_time TIMESTAMP,
                       reset_password_token VARCHAR(255),
                       reset_token_expiry_time TIMESTAMP
);


-- 3) Now add FK for faculties.owner_id
ALTER TABLE faculties
    ADD CONSTRAINT fk_owner
        FOREIGN KEY (owner_id) REFERENCES users(id);

-- 4) POSTS TABLE (FIXED)
CREATE TABLE posts (
                       id BIGSERIAL PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       content TEXT,
                       link_url VARCHAR(255), -- This will be added by ddl-auto=update
    -- media_urls TEXT, -- <-- THIS IS NOW REMOVED
                       faculty_id BIGINT REFERENCES faculties(id),
                       user_id BIGINT REFERENCES users(id) ON DELETE SET NULL, -- Use SET NULL or CASCADE
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP
);

-- 5) COMMENTS TABLE (FIXED)
CREATE TABLE comments (
                          id BIGSERIAL PRIMARY KEY,
                          post_id BIGINT REFERENCES posts(id) ON DELETE CASCADE,
                          user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                          parent_comment_id BIGINT REFERENCES comments(id) ON DELETE CASCADE,
                          content TEXT NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6) VOTES / LIKES (FIXED)
CREATE TABLE votes (
                       id BIGSERIAL PRIMARY KEY,
                       post_id BIGINT REFERENCES posts(id) ON DELETE CASCADE,
                       user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                       vote_type INT NOT NULL CHECK (vote_type IN (1, -1)),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       UNIQUE (post_id, user_id)
);


-- 7) NOTIFICATIONS TABLE
CREATE TABLE notifications (
                               id BIGSERIAL PRIMARY KEY,
                               user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                               message TEXT NOT NULL,
                               is_read BOOLEAN DEFAULT FALSE,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8) IMAGES TABLE (NEW)
-- This new table holds the URLs for images and videos
CREATE TABLE images (
                        id BIGSERIAL PRIMARY KEY,
                        url TEXT NOT NULL,
                        post_id BIGINT REFERENCES posts(id) ON DELETE CASCADE,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
