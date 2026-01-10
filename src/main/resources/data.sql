-- Cinema Booking System - Seed Data
-- Passwords are BCrypt-encoded for security
-- admin123 -> $2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a
-- user123 -> $2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW
-- employee123 -> $2a$10$UfCJqmPqQlEBbak42/y1Oe5t1JXy7KLKnjmHqKHGkz7gUAqPTYPYe

INSERT INTO roles (name, description) VALUES
('ROLE_ADMIN', 'Administrator with full system access'),
('ROLE_EMPLOYEE', 'Cinema employee with limited access'),
('ROLE_USER', 'Regular customer user')
ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description;

INSERT INTO users (username, email, password, first_name, last_name, phone_number, enabled, created_at, updated_at) VALUES
('admin', 'admin@cinema.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Admin', 'Admin', '111-111-1111', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('employee', 'employee@cinema.com', '$2a$10$UfCJqmPqQlEBbak42/y1Oe5t1JXy7KLKnjmHqKHGkz7gUAqPTYPYe', 'Employee', 'Staff', '222-222-2222', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user', 'user@cinema.com', '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO/BTk76klW', 'John', 'Doe', '333-333-3333', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
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
('Standard', 'Regular ticket', 1.0, true),
('Student', 'Student discount ticket', 0.75, true),
('Senior', 'Senior citizen discount ticket', 0.75, true),
('Child', 'Children under 12', 0.6, true)
ON CONFLICT (name) DO UPDATE SET
    description = EXCLUDED.description,
    price_modifier = EXCLUDED.price_modifier,
    active = EXCLUDED.active;

-- Insert movies
INSERT INTO movies (title, description, genre, duration_minutes, director, movie_cast, age_rating, release_year, active, created_at, updated_at) VALUES
('The Matrix', 'A computer hacker learns about the true nature of reality', 'Sci-Fi', 136, 'Wachowski Brothers', 'Keanu Reeves, Laurence Fishburne', 'R', 1999, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Inception', 'A thief who steals corporate secrets through dream-sharing technology', 'Sci-Fi', 148, 'Christopher Nolan', 'Leonardo DiCaprio, Joseph Gordon-Levitt', 'PG-13', 2010, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('The Dark Knight', 'Batman faces the Joker in a battle for Gotham City', 'Action', 152, 'Christopher Nolan', 'Christian Bale, Heath Ledger', 'PG-13', 2008, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('The Shawshank Redemption', 'Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.', 'Drama',  142, 'Frank Darabont', 'Tim Robbins, Morgan Freeman', 'R', 1994, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Pulp Fiction', 'The lives of two mob hitmen, a boxer, a gangster and his wife intertwine in four tales of violence and redemption.', 'Crime', 154, 'Quentin Tarantino', 'John Travolta, Uma Thurman, Samuel L. Jackson', 'R', 1994, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('The Godfather', 'The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant son.', 'Crime', 175, 'Francis Ford Coppola', 'Marlon Brando, Al Pacino', 'R', 1972, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert Screenings
INSERT INTO screenings (movie_id, hall_id, start_time, end_time, base_price, active, created_at, updated_at)
SELECT 
    f.id,
    h.id,
    CURRENT_DATE + schedule.day_offset * INTERVAL '1 day' + schedule.start_minute * INTERVAL '1 minute',
    CURRENT_DATE + schedule.day_offset * INTERVAL '1 day' + (schedule.start_minute + f.duration_minutes) * INTERVAL '1 minute',
    12.00,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM (
    SELECT 'The Matrix' as film_title, 'Hall 1' as hall_name, 14 * 60 as start_minute, 0 as day_offset
    UNION ALL SELECT 'The Matrix', 'Hall 1', 17 * 60, 0
    UNION ALL SELECT 'The Matrix', 'Hall 1', 20 * 60, 0
    UNION ALL SELECT 'Inception', 'Hall 2', 15 * 60, 1
    UNION ALL SELECT 'Inception', 'Hall 2', 18 * 60, 1
    UNION ALL SELECT 'Inception', 'Hall 2', 21 * 60, 1
    UNION ALL SELECT 'The Dark Knight', 'Hall 3', 16 * 60, 2
    UNION ALL SELECT 'The Dark Knight', 'Hall 3', 19 * 60, 2
    UNION ALL SELECT 'The Matrix', 'Hall 2', 13 * 60, 3
    UNION ALL SELECT 'Inception', 'Hall 1', 16 * 60 + 30, 3
) schedule
JOIN movies f ON f.title = schedule.film_title
JOIN halls h ON h.name = schedule.hall_name;
