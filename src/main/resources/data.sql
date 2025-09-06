INSERT INTO categories (name) VALUES ('Social');      -- will get category_id = 1
INSERT INTO categories (name) VALUES ('Career');      -- category_id = 2
INSERT INTO categories (name) VALUES ('Hackathon');   -- category_id = 3
INSERT INTO categories (name) VALUES ('Meetup');      -- category_id = 4


-- Past event (won't show in "upcoming")
INSERT INTO events (name, description, created_by_user_id, date_time, location, category, capacity, category_fk_id, price)
VALUES ('Welcome Back BBQ', 'Kick off social', 1, DATEADD('DAY', -10, CURRENT_TIMESTAMP), 'Alumni Courtyard', 'Social', 200, 1, 0.00);

-- Upcoming events (should show, sorted by date_time)
INSERT INTO events (name, description, created_by_user_id, date_time, location, category, capacity, category_fk_id, price)
VALUES ('Cloud Career Panel', 'Industry speakers', 2, DATEADD('HOUR', 3, CURRENT_TIMESTAMP), 'Building 80, Room 10-12', 'Career', 150, 2, 0.00);

INSERT INTO events (name, description, created_by_user_id, date_time, location, category, capacity, category_fk_id, price)
VALUES ('Hack Night', 'Bring your laptop', 2, DATEADD('DAY', 1, CURRENT_TIMESTAMP), 'Fab Lab', 'Hackathon', 80, 3, 0.00);

INSERT INTO events (name, description, created_by_user_id, date_time, location, category, capacity, category_fk_id, price)
VALUES ('Data Science Meetup', 'Lightning talks', 3, DATEADD('DAY', 2, CURRENT_TIMESTAMP), 'Online (Zoom)', 'Meetup', 500, 4, 0.00);