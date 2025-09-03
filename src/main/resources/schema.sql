DROP TABLE IF EXISTS events;

CREATE TABLE events {
  event_id IDENTITY PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(2000),
  created_by_user_id BIGINT,    -- Foreign key to user table (not enforced yet)
  date_time TIMESTAMP NOT NULL,
  location VARCHAR(255) NOT NULL,
  category VARCHAR(100) NOT NULL,
  capacity INT,
  category_fk_id BIGINT,        -- Foreign key to category (not enforced yet)
  price DECIMAL(10,2)
};

-- Create index for efficient upcoming event queries
CREATE INDEX idx_event_date_time ON event(date_time);