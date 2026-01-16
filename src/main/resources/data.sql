-- Cinema Booking System - Seed Data
-- Passwords are BCrypt-encoded for security
-- admin123 -> $2a$10$QQob1rq6XxACbIUjjcrDXeDElhTnXhpXZ3A1ItmlrH5p1.yJViblS
-- user123 -> $2a$10$/5a3.4tb3xfPs5/5qCjQ0uO.JRQgY7xaltdEvfCs0FOQ0C/ppbjzS
-- employee123 -> $2a$10$phKHgjSlMpqfzqf9d8Bw3e/A8wHFhKg5pW69M7upQv4XkaE73IJem

INSERT INTO roles (name, description) VALUES
('ROLE_ADMIN', 'Administrator with full system access'),
('ROLE_EMPLOYEE', 'Cinema employee with limited access'),
('ROLE_USER', 'Regular customer user')
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description;

INSERT INTO users (username, email, password, first_name, last_name, phone_number, enabled, created_at, updated_at) VALUES
('admin', 'admin@cinema.com', '$2a$10$QQob1rq6XxACbIUjjcrDXeDElhTnXhpXZ3A1ItmlrH5p1.yJViblS', 'Admin', 'Admin', '111-111-1111', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('employee', 'employee@cinema.com', '$2a$10$phKHgjSlMpqfzqf9d8Bw3e/A8wHFhKg5pW69M7upQv4XkaE73IJem', 'Employee', 'Staff', '222-222-2222', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user', 'user@cinema.com', '$2a$10$/5a3.4tb3xfPs5/5qCjQ0uO.JRQgY7xaltdEvfCs0FOQ0C/ppbjzS', 'John', 'Doe', '333-333-3333', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO UPDATE SET
    email = EXCLUDED.email,
    password = EXCLUDED.password,
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    phone_number = EXCLUDED.phone_number,
    enabled = EXCLUDED.enabled,
    updated_at = CURRENT_TIMESTAMP;

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE ((u.username = 'admin' AND r.name IN ('ROLE_ADMIN', 'ROLE_EMPLOYEE', 'ROLE_USER'))
    OR (u.username = 'employee' AND r.name IN ('ROLE_EMPLOYEE', 'ROLE_USER'))
    OR (u.username = 'user' AND r.name = 'ROLE_USER'))
ON CONFLICT (user_id, role_id) DO NOTHING;

-- Insert Halls
INSERT INTO halls (name, total_seats, rows_count, seats_per_row, active) VALUES
('Hall 1', 100, 10, 10, true),
('Hall 2', 80, 8, 10, true),
('Hall 3', 120, 12, 10, true);

INSERT INTO seats (hall_id, row_number, seat_number, seat_type, active)
SELECT h.id, r.row_num, s.seat_num, 'STANDARD', true
FROM halls h
CROSS JOIN LATERAL generate_series(1, h.rows_count) AS r(row_num)
CROSS JOIN LATERAL generate_series(1, h.seats_per_row) AS s(seat_num)
ON CONFLICT (hall_id, row_number, seat_number) DO NOTHING;

INSERT INTO ticket_types (name, description, price_modifier, active) VALUES
('Standard', 'Regular ticket', 30.0, true),
('Student', 'Student discount ticket', 22.0, true),
('Senior', 'Senior citizen discount ticket', 22.0, true),
('Child', 'Children under 12', 20.0, true)
ON CONFLICT (name) DO UPDATE SET
    description = EXCLUDED.description,
    price_modifier = EXCLUDED.price_modifier,
    active = EXCLUDED.active;

-- Insert movies with posters and trailers
INSERT INTO movies (title, description, genre, duration_minutes, director, movie_cast, age_rating, release_year, poster_path, trailer_url, active, created_at, updated_at) VALUES
-- Current/Upcoming Movies (Now Showing)
('The Matrix', 'A computer hacker learns about the true nature of reality and his role in the war against its controllers.', 'Sci-Fi', 136, 'Wachowski Brothers', 'Keanu Reeves, Laurence Fishburne, Carrie-Anne Moss', 'R', 1999, '/images/posters/1.jpg', 'https://www.youtube.com/embed/vKQi3bBA1y8', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Inception', 'A thief who steals corporate secrets through dream-sharing technology is given the inverse task of planting an idea into the mind of a CEO.', 'Sci-Fi', 148, 'Christopher Nolan', 'Leonardo DiCaprio, Joseph Gordon-Levitt, Ellen Page, Tom Hardy', 'PG-13', 2010, '/images/posters/2.jpg', 'https://www.youtube.com/watch?v=YoHD9XEInc0', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('The Dark Knight', 'Batman faces the Joker, a criminal mastermind who wants to plunge Gotham City into anarchy and bring Batman''s world crashing down.', 'Action', 152, 'Christopher Nolan', 'Christian Bale, Heath Ledger, Aaron Eckhart, Maggie Gyllenhaal', 'PG-13', 2008, '/images/posters/3.jpg', 'https://www.youtube.com/embed/EXeTwQWrcwY', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Interstellar', 'A team of explorers travel through a wormhole in space in an attempt to ensure humanity''s survival.', 'Sci-Fi', 169, 'Christopher Nolan', 'Matthew McConaughey, Anne Hathaway, Jessica Chastain, Bill Irwin', 'PG-13', 2014, '/images/posters/4.jpg', 'https://www.youtube.com/watch?v=zSWdZVtXT7E', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Oppenheimer', 'The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb during World War II.', 'Biography', 180, 'Christopher Nolan', 'Cillian Murphy, Robert Downey Jr., Emily Blunt, Matt Damon', 'R', 2023, '/images/posters/5.jpg', 'https://www.youtube.com/embed/uYPbbksJxIg', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Dune: Part Two', 'Paul Atreides travels to the dangerous planet Arrakis to ensure the future of his family and people.', 'Sci-Fi', 166, 'Denis Villeneuve', 'Timothée Chalamet, Zendaya, Rebecca Ferguson, Austin Butler', 'PG-13', 2024, '/images/posters/6.jpg', 'https://www.youtube.com/embed/Way9Dexny3w', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Classic/Archived Movies (Movie Information - no current screenings)
('The Shawshank Redemption', 'Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.', 'Drama', 142, 'Frank Darabont', 'Tim Robbins, Morgan Freeman, Bob Gunton', 'R', 1994, '/images/posters/7.jpg', 'https://www.youtube.com/watch?v=PLl99DlL6b4', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Pulp Fiction', 'The lives of two mob hitmen, a boxer, a gangster and his wife, and a pair of diner bandits intertwine in four tales of violence and redemption.', 'Crime', 154, 'Quentin Tarantino', 'John Travolta, Uma Thurman, Samuel L. Jackson, Harvey Keitel', 'R', 1994, '/images/posters/8.jpg', 'https://www.youtube.com/watch?v=s7EdQ4FqbhY', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('The Godfather', 'The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant son Michael.', 'Crime', 175, 'Francis Ford Coppola', 'Marlon Brando, Al Pacino, James Caan, Robert Duvall', 'R', 1972, '/images/posters/9.jpg', 'https://www.youtube.com/watch?v=UaVTIH8mujA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Forrest Gump', 'The presidencies of Kennedy and Johnson, the Vietnam War, and the Watergate scandal unfold from the perspective of an Alabama man with an IQ of 75.', 'Drama', 142, 'Robert Zemeckis', 'Tom Hanks, Sally Field, Gary Sinise, Mykelti Williamson', 'PG-13', 1994, '/images/posters/10.jpg', 'https://www.youtube.com/watch?v=bLvqoHBptjg', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('The Avengers', 'Earth''s mightiest heroes must come together and learn to fight as a team to save the world from an alien threat.', 'Action', 143, 'Joss Whedon', 'Robert Downey Jr., Chris Evans, Chris Hemsworth, Scarlett Johansson', 'PG-13', 2012, '/images/posters/11.jpg', 'https://www.youtube.com/watch?v=399Ez7WHK5s', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Titanic', 'A seventeen-year-old aristocrat falls in love with a kind but poor artist aboard the luxurious, ill-fated R.M.S. Titanic.', 'Romance', 194, 'James Cameron', 'Leonardo DiCaprio, Kate Winslet, Billy Zane, Kathy Bates', 'PG-13', 1997, '/images/posters/12.jpg', 'https://www.youtube.com/watch?v=kVrqfYjkTdQ', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('The Lion King', 'A young lion prince flees his kingdom only to learn the truth about his father''s death and take his rightful place in the Circle of Life.', 'Animation', 88, 'Roger Allers, Rob Minkoff', 'James Earl Jones, Jeremy Irons, Matthew Broderick, Whoopi Goldberg', 'G', 1994, '/images/posters/13.jpg', 'https://www.youtube.com/watch?v=lFzVJEksoDY', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Gladiator', 'A former Roman General sets out to exact vengeance against the corrupt emperor who murdered his family and sent him into slavery.', 'Action', 155, 'Ridley Scott', 'Russell Crowe, Joaquin Phoenix, Lucilla, Djimon Hounsou', 'R', 2000, '/images/posters/14.jpg', 'https://www.youtube.com/watch?v=owK1qxDselE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Jurassic Park', 'A pragmatic paleontologist touring an almost complete theme park is tasked with protecting a couple of kids and dinosaurs when a power failure causes the park''s cloned dinosaurs to run amok.', 'Sci-Fi', 127, 'Steven Spielberg', 'Sam Neill, Laura Dern, Jeff Goldblum, Richard Attenborough', 'PG-13', 1993, '/images/posters/15.jpg', 'https://www.youtube.com/watch?v=QWBKEmWWL38', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('The Silence of the Lambs', 'A young FBI cadet must receive the help of an incarcerated and manipulative cannibal killer to help catch another serial killer who skins his female victims.', 'Thriller', 118, 'Jonathan Demme', 'Jodie Foster, Scott Glenn, Anthony Hopkins', 'R', 1991, '/images/posters/16.jpg', 'https://www.youtube.com/watch?v=6iB21hsprAQ', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert Screenings - Current and Future (Now Showing)
INSERT INTO screenings (movie_id, hall_id, start_time, end_time, base_price, active, created_at, updated_at)
SELECT 
    f.id,
    h.id,
    CURRENT_DATE + schedule.day_offset * INTERVAL '1 day' + schedule.start_minute * INTERVAL '1 minute',
    CURRENT_DATE + schedule.day_offset * INTERVAL '1 day' + (schedule.start_minute + f.duration_minutes) * INTERVAL '1 minute',
    30.00,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM (
    -- The Matrix (Today and Tomorrow)
    SELECT 'The Matrix' as film_title, 'Hall 1' as hall_name, 14 * 60 as start_minute, 0 as day_offset
    UNION ALL SELECT 'The Matrix', 'Hall 1', 17 * 60, 0
    UNION ALL SELECT 'The Matrix', 'Hall 1', 20 * 60, 0
    UNION ALL SELECT 'The Matrix', 'Hall 2', 13 * 60, 1
    UNION ALL SELECT 'The Matrix', 'Hall 2', 16 * 60, 1
    UNION ALL SELECT 'The Matrix', 'Hall 1', 18 * 60, 2
    
    -- Inception (Next 2 days)
    UNION ALL SELECT 'Inception', 'Hall 2', 15 * 60, 1
    UNION ALL SELECT 'Inception', 'Hall 2', 18 * 60, 1
    UNION ALL SELECT 'Inception', 'Hall 2', 21 * 60, 1
    UNION ALL SELECT 'Inception', 'Hall 1', 16 * 60 + 30, 2
    UNION ALL SELECT 'Inception', 'Hall 3', 19 * 60, 2
    
    -- The Dark Knight (Next 3 days)
    UNION ALL SELECT 'The Dark Knight', 'Hall 3', 16 * 60, 2
    UNION ALL SELECT 'The Dark Knight', 'Hall 3', 19 * 60, 2
    UNION ALL SELECT 'The Dark Knight', 'Hall 2', 14 * 60, 3
    UNION ALL SELECT 'The Dark Knight', 'Hall 1', 17 * 60, 3
    
    -- Interstellar (Coming this week)
    UNION ALL SELECT 'Interstellar', 'Hall 1', 15 * 60, 4
    UNION ALL SELECT 'Interstellar', 'Hall 1', 18 * 60, 4
    UNION ALL SELECT 'Interstellar', 'Hall 2', 20 * 60, 4
    
    -- Oppenheimer (Coming this week)
    UNION ALL SELECT 'Oppenheimer', 'Hall 3', 14 * 60, 5
    UNION ALL SELECT 'Oppenheimer', 'Hall 3', 17 * 60, 5
    
    -- Dune: Part Two (Coming next week)
    UNION ALL SELECT 'Dune: Part Two', 'Hall 2', 16 * 60, 6
    UNION ALL SELECT 'Dune: Part Two', 'Hall 2', 19 * 60, 6
) schedule
JOIN movies f ON f.title = schedule.film_title
JOIN halls h ON h.name = schedule.hall_name
ON CONFLICT DO NOTHING;

-- Insert Past Screenings for Analytics (Archived Movies)
INSERT INTO screenings (movie_id, hall_id, start_time, end_time, base_price, active, created_at, updated_at)
SELECT 
    f.id,
    h.id,
    CURRENT_DATE - schedule.day_offset * INTERVAL '1 day' + schedule.start_minute * INTERVAL '1 minute',
    CURRENT_DATE - schedule.day_offset * INTERVAL '1 day' + (schedule.start_minute + f.duration_minutes) * INTERVAL '1 minute',
    25.00,
    true,
    CURRENT_TIMESTAMP - schedule.day_offset * INTERVAL '1 day',
    CURRENT_TIMESTAMP - schedule.day_offset * INTERVAL '1 day'
FROM (
    -- The Shawshank Redemption (Past weeks)
    SELECT 'The Shawshank Redemption' as film_title, 'Hall 1' as hall_name, 14 * 60 as start_minute, 30 as day_offset
    UNION ALL SELECT 'The Shawshank Redemption', 'Hall 1', 17 * 60, 29
    UNION ALL SELECT 'The Shawshank Redemption', 'Hall 2', 20 * 60, 28
    UNION ALL SELECT 'The Shawshank Redemption', 'Hall 3', 15 * 60, 27
    UNION ALL SELECT 'The Shawshank Redemption', 'Hall 1', 18 * 60, 25
    UNION ALL SELECT 'The Shawshank Redemption', 'Hall 2', 16 * 60, 20
    
    -- Pulp Fiction (Past weeks)
    UNION ALL SELECT 'Pulp Fiction', 'Hall 2', 15 * 60, 35
    UNION ALL SELECT 'Pulp Fiction', 'Hall 2', 18 * 60, 34
    UNION ALL SELECT 'Pulp Fiction', 'Hall 1', 21 * 60, 33
    UNION ALL SELECT 'Pulp Fiction', 'Hall 3', 14 * 60, 31
    UNION ALL SELECT 'Pulp Fiction', 'Hall 2', 17 * 60, 22
    
    -- The Godfather (Past weeks)
    UNION ALL SELECT 'The Godfather', 'Hall 3', 16 * 60, 40
    UNION ALL SELECT 'The Godfather', 'Hall 3', 19 * 60, 39
    UNION ALL SELECT 'The Godfather', 'Hall 1', 14 * 60, 38
    UNION ALL SELECT 'The Godfather', 'Hall 2', 17 * 60, 36
    
    -- Forrest Gump (Past weeks)
    UNION ALL SELECT 'Forrest Gump', 'Hall 1', 15 * 60, 32
    UNION ALL SELECT 'Forrest Gump', 'Hall 1', 18 * 60, 30
    UNION ALL SELECT 'Forrest Gump', 'Hall 3', 20 * 60, 28
    UNION ALL SELECT 'Forrest Gump', 'Hall 2', 14 * 60, 24
    
    -- The Avengers (Past weeks)
    UNION ALL SELECT 'The Avengers', 'Hall 2', 16 * 60, 45
    UNION ALL SELECT 'The Avengers', 'Hall 1', 19 * 60, 43
    UNION ALL SELECT 'The Avengers', 'Hall 3', 15 * 60, 41
    
    -- Titanic (Past weeks)
    UNION ALL SELECT 'Titanic', 'Hall 3', 14 * 60, 50
    UNION ALL SELECT 'Titanic', 'Hall 2', 17 * 60, 48
    UNION ALL SELECT 'Titanic', 'Hall 1', 20 * 60, 46
    
    -- The Lion King (Past weeks)
    UNION ALL SELECT 'The Lion King', 'Hall 1', 15 * 60, 42
    UNION ALL SELECT 'The Lion King', 'Hall 2', 18 * 60, 40
    UNION ALL SELECT 'The Lion King', 'Hall 3', 16 * 60, 37
    
    -- Gladiator (Past weeks)
    UNION ALL SELECT 'Gladiator', 'Hall 3', 15 * 60, 44
    UNION ALL SELECT 'Gladiator', 'Hall 1', 18 * 60, 42
    
    -- Jurassic Park (Past weeks)
    UNION ALL SELECT 'Jurassic Park', 'Hall 2', 14 * 60, 52
    UNION ALL SELECT 'Jurassic Park', 'Hall 3', 17 * 60, 50
    
    -- The Silence of the Lambs (Past weeks)
    UNION ALL SELECT 'The Silence of the Lambs', 'Hall 1', 19 * 60, 55
    UNION ALL SELECT 'The Silence of the Lambs', 'Hall 2', 21 * 60, 53
) schedule
JOIN movies f ON f.title = schedule.film_title
JOIN halls h ON h.name = schedule.hall_name
ON CONFLICT DO NOTHING;

-- Insert bookings for past screenings (for analytics)
INSERT INTO bookings (booking_number, screening_id, user_id, total_price, status, created_at, updated_at)
SELECT 
    CONCAT('BK-', s.id, '-', u.id, '-', FLOOR(RANDOM() * 10000)),
    s.id,
    u.id,
    CASE WHEN RANDOM() < 0.5 THEN 50.0 ELSE 75.0 END as price,
    'COMPLETED',
    s.start_time - INTERVAL '2 hours',
    s.start_time - INTERVAL '2 hours'
FROM screenings s
CROSS JOIN users u
WHERE s.start_time < CURRENT_DATE
    AND u.username = 'user'
    AND RANDOM() < 0.6
ON CONFLICT DO NOTHING;
-- Insert bookings for today's screenings (current date with mix of statuses)
INSERT INTO bookings (booking_number, screening_id, user_id, total_price, status, created_at, updated_at)
SELECT 
    CONCAT('BK-TODAY-', s.id, '-', FLOOR(RANDOM() * 1000)),
    s.id,
    u.id,
    25.0 * (FLOOR(RANDOM() * 3) + 1),
    CASE WHEN RANDOM() < 0.7 THEN 'CONFIRMED' ELSE 'PENDING' END,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM screenings s
CROSS JOIN users u
WHERE DATE(s.start_time) = CURRENT_DATE
    AND u.username = 'user'
    AND RANDOM() < 0.5
ON CONFLICT DO NOTHING;

-- Insert bookings for upcoming screenings (future dates, next 7 days)
INSERT INTO bookings (booking_number, screening_id, user_id, total_price, status, created_at, updated_at)
SELECT 
    CONCAT('BK-FUTURE-', s.id, '-', FLOOR(RANDOM() * 1000)),
    s.id,
    u.id,
    25.0 * (FLOOR(RANDOM() * 3) + 1),
    CASE WHEN RANDOM() < 0.8 THEN 'CONFIRMED' ELSE 'PENDING' END,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM screenings s
CROSS JOIN users u
WHERE s.start_time > CURRENT_DATE
    AND s.start_time <= CURRENT_DATE + INTERVAL '7 days'
    AND u.username = 'user'
    AND RANDOM() < 0.4
ON CONFLICT DO NOTHING;

-- Showcase sold seats for today's screenings with a deterministic booking
WITH target_screening AS (
    SELECT s.id AS screening_id, s.hall_id, s.base_price
    FROM screenings s
    JOIN movies m ON m.id = s.movie_id
    WHERE m.title = 'The Matrix'
      AND DATE(s.start_time) = CURRENT_DATE
    ORDER BY s.start_time
    LIMIT 1
),
target_user AS (
    SELECT id AS user_id, email, phone_number
    FROM users
    WHERE username = 'user'
),
demo_booking AS (
    INSERT INTO bookings (booking_number, screening_id, user_id, total_price, status, payment_method, payment_reference, customer_email, customer_phone, created_at, updated_at)
    SELECT 'BK-MATRIX-DEMO', ts.screening_id, tu.user_id, ts.base_price * 2, 'CONFIRMED',
           'CARD', 'MAT-DEMO-001', tu.email, tu.phone_number,
           CURRENT_TIMESTAMP - INTERVAL '15 minutes', CURRENT_TIMESTAMP - INTERVAL '15 minutes'
    FROM target_screening ts
    CROSS JOIN target_user tu
    ON CONFLICT (booking_number) DO NOTHING
    RETURNING id, screening_id
),
seat_positions AS (
    SELECT 1 AS row_number, 1 AS seat_number
    UNION ALL
    SELECT 1, 2
)
INSERT INTO booking_seats (booking_id, seat_id, ticket_type_id, price, seat_status)
SELECT db.id,
       seat.id,
       tt.id,
       sc.base_price,
       'OCCUPIED'
FROM demo_booking db
JOIN screenings sc ON sc.id = db.screening_id
JOIN seat_positions sp ON true
JOIN seats seat ON seat.hall_id = sc.hall_id
    AND seat.row_number = sp.row_number
    AND seat.seat_number = sp.seat_number
JOIN ticket_types tt ON tt.name = 'Standard';

-- Insert booking seats for all remaining bookings (reserve actual seats)
INSERT INTO booking_seats (booking_id, seat_id, ticket_type_id, price, seat_status)
SELECT b.id,
       seat_choice.seat_id,
       ticket_choice.ticket_type_id,
       ticket_choice.price,
       CASE WHEN b.status IN ('CONFIRMED', 'COMPLETED') THEN 'OCCUPIED' ELSE 'RESERVED' END
FROM bookings b
JOIN screenings sc ON sc.id = b.screening_id
JOIN LATERAL (
    SELECT s.id AS seat_id
    FROM seats s
    WHERE s.hall_id = sc.hall_id
      AND s.row_number >= 3
    ORDER BY RANDOM()
    LIMIT 1
) AS seat_choice ON true
JOIN LATERAL (
    SELECT tt.id AS ticket_type_id, tt.price_modifier AS price
    FROM ticket_types tt
    WHERE tt.name IN ('Standard', 'Student', 'Senior')
    ORDER BY RANDOM()
    LIMIT 1
) AS ticket_choice ON true
WHERE NOT EXISTS (
    SELECT 1 FROM booking_seats existing WHERE existing.booking_id = b.id
)
LIMIT 200;

-- Seed temporary seat holds that expire after 10 minutes
WITH target_screening AS (
    SELECT s.id AS screening_id, s.hall_id
    FROM screenings s
    JOIN movies m ON m.id = s.movie_id
    WHERE m.title = 'The Matrix'
      AND DATE(s.start_time) = CURRENT_DATE
    ORDER BY s.start_time
    LIMIT 1
),
lock_positions AS (
    SELECT 1 AS row_number, 3 AS seat_number, 'seed-hold-1' AS session_id
    UNION ALL
    SELECT 1, 4, 'seed-hold-2'
)
INSERT INTO seat_locks (seat_id, screening_id, session_id, username, status, expires_at, created_at, updated_at)
SELECT seat.id,
       ts.screening_id,
       lp.session_id,
       'guest_hold',
       'ACTIVE',
       CURRENT_TIMESTAMP + INTERVAL '10 minutes',
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP
FROM target_screening ts
JOIN lock_positions lp ON true
JOIN seats seat ON seat.hall_id = ts.hall_id
    AND seat.row_number = lp.row_number
    AND seat.seat_number = lp.seat_number
WHERE NOT EXISTS (
    SELECT 1
    FROM seat_locks sl
    WHERE sl.screening_id = ts.screening_id
      AND sl.seat_id = seat.id
      AND sl.status = 'ACTIVE'
);