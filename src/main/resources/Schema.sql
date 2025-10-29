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
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3) Now add FK for faculties.owner_id
ALTER TABLE faculties
    ADD CONSTRAINT fk_owner
        FOREIGN KEY (owner_id) REFERENCES users(id);

-- 4) EVENTS TABLE
CREATE TABLE events (
                        id BIGSERIAL PRIMARY KEY,
                        title VARCHAR(255) NOT NULL,
                        content TEXT,
                        image_url VARCHAR(255),
                        faculty_id BIGINT REFERENCES faculties(id),
                        user_id BIGINT REFERENCES users(id),
                        is_global BOOLEAN DEFAULT FALSE,
                        status VARCHAR(20) DEFAULT 'APPROVED',
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5) COMMENTS TABLE
CREATE TABLE comments (
                          id BIGSERIAL PRIMARY KEY,
                          event_id BIGINT REFERENCES events(id) ON DELETE CASCADE,
                          user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                          parent_comment_id BIGINT REFERENCES comments(id) ON DELETE CASCADE,
                          content TEXT NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6) VOTES / LIKES
CREATE TABLE votes (
                       id BIGSERIAL PRIMARY KEY,
                       event_id BIGINT REFERENCES events(id) ON DELETE CASCADE,
                       user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                       vote_type INT NOT NULL CHECK (vote_type IN (1, -1)),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       UNIQUE (event_id, user_id)
);

-- 7) NOTIFICATIONS TABLE
CREATE TABLE notifications (
                               id BIGSERIAL PRIMARY KEY,
                               user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                               message TEXT NOT NULL,
                               is_read BOOLEAN DEFAULT FALSE,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
