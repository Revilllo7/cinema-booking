-- Schema definition for Cinema Booking System
-- Roles, Users, Movies, Halls, Seats, Screenings, Ticket Types, Bookings, and Booking-Seats

-- Drop tables if they exist (in correct order to avoid foreign key constraints)
DROP TABLE IF EXISTS booking_seats CASCADE;
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS screenings CASCADE;
DROP TABLE IF EXISTS film_images CASCADE;
DROP TABLE IF EXISTS films CASCADE;
DROP TABLE IF EXISTS seats CASCADE;
DROP TABLE IF EXISTS halls CASCADE;
DROP TABLE IF EXISTS ticket_types CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- Roles table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200)
);

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User-Role junction table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Movies table
CREATE TABLE movies (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    genre VARCHAR(100),
    age_rating VARCHAR(10),
    duration_minutes INTEGER,
    director VARCHAR(150),
    cast TEXT,
    release_year INTEGER,
    poster_path VARCHAR(500),
    trailer_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Movie images table
CREATE TABLE movie_images (
    id BIGSERIAL PRIMARY KEY,
    movie_id BIGINT NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    image_path VARCHAR(500) NOT NULL,
    display_order INTEGER,
    caption VARCHAR(200)
);

-- Halls table
CREATE TABLE halls (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    total_seats INTEGER NOT NULL,
    rows_count INTEGER NOT NULL,
    seats_per_row INTEGER NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Seats table
CREATE TABLE seats (
    id BIGSERIAL PRIMARY KEY,
    hall_id BIGINT NOT NULL REFERENCES halls(id) ON DELETE CASCADE,
    row_number INTEGER NOT NULL,
    seat_number INTEGER NOT NULL,
    seat_type VARCHAR(20) NOT NULL DEFAULT 'STANDARD',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(hall_id, row_number, seat_number)
);

-- Screenings table
CREATE TABLE screenings (
    id BIGSERIAL PRIMARY KEY,
    film_id BIGINT NOT NULL REFERENCES films(id) ON DELETE CASCADE,
    hall_id BIGINT NOT NULL REFERENCES halls(id) ON DELETE CASCADE,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    base_price DECIMAL(10, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ticket types table
CREATE TABLE ticket_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    price_modifier DECIMAL(10, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Bookings table
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    booking_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    screening_id BIGINT NOT NULL REFERENCES screenings(id) ON DELETE CASCADE,
    total_price DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    payment_reference VARCHAR(100),
    customer_email VARCHAR(150),
    customer_phone VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Booking-Seat junction table (with ticket type)
CREATE TABLE booking_seats (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    seat_id BIGINT NOT NULL REFERENCES seats(id) ON DELETE CASCADE,
    ticket_type_id BIGINT NOT NULL REFERENCES ticket_types(id) ON DELETE CASCADE,
    price DECIMAL(10, 2) NOT NULL,
    seat_status VARCHAR(20) NOT NULL DEFAULT 'RESERVED'
);

-- Indexes for performance optimization
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_films_title ON films(title);
CREATE INDEX idx_films_active ON films(active);
CREATE INDEX idx_screenings_start_time ON screenings(start_time);
CREATE INDEX idx_screenings_film_id ON screenings(film_id);
CREATE INDEX idx_screenings_hall_id ON screenings(hall_id);
CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_screening_id ON bookings(screening_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_booking_seats_booking_id ON booking_seats(booking_id);
CREATE INDEX idx_booking_seats_seat_id ON booking_seats(seat_id);
CREATE INDEX idx_booking_seats_ticket_type_id ON booking_seats(ticket_type_id);