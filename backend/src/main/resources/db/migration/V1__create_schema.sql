CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(190) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    phone VARCHAR(30),
    role ENUM('ROLE_TOURIST','ROLE_ADMIN') NOT NULL DEFAULT 'ROLE_TOURIST',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE user_interests (
    user_id BIGINT NOT NULL,
    interest ENUM('BEACH','TEMPLES','ADVENTURE','FOOD','NATURE','HISTORY') NOT NULL,
    PRIMARY KEY (user_id, interest),
    CONSTRAINT fk_user_interests_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE destinations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL UNIQUE,
    slug VARCHAR(140) NOT NULL UNIQUE,
    state VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL DEFAULT 'India',
    description TEXT NOT NULL,
    best_season VARCHAR(120) NOT NULL,
    hero_image_url VARCHAR(500),
    latitude DECIMAL(9,6),
    longitude DECIMAL(9,6),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE places (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    destination_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(170) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    suitable_for VARCHAR(255) NOT NULL,
    best_season VARCHAR(120) NOT NULL,
    difficulty ENUM('EASY','MODERATE','HARD') NOT NULL DEFAULT 'EASY',
    price DECIMAL(10,2) NOT NULL DEFAULT 0,
    duration_minutes INT NOT NULL,
    rating DECIMAL(3,2) NOT NULL DEFAULT 4.00,
    image_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_place_price CHECK (price >= 0),
    CONSTRAINT chk_place_duration CHECK (duration_minutes > 0),
    CONSTRAINT fk_places_destination FOREIGN KEY (destination_id) REFERENCES destinations(id)
);

CREATE TABLE place_tags (
    place_id BIGINT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (place_id, tag),
    CONSTRAINT fk_place_tags_place FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE CASCADE
);

CREATE TABLE hotels (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    destination_id BIGINT NOT NULL,
    name VARCHAR(160) NOT NULL,
    description TEXT,
    address VARCHAR(255) NOT NULL,
    star_rating DECIMAL(2,1) NOT NULL DEFAULT 3.0,
    image_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_hotels_destination FOREIGN KEY (destination_id) REFERENCES destinations(id)
);

CREATE TABLE rooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    hotel_id BIGINT NOT NULL,
    room_type VARCHAR(100) NOT NULL,
    capacity INT NOT NULL,
    price_per_night DECIMAL(10,2) NOT NULL,
    total_units INT NOT NULL,
    amenities JSON,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_room_capacity CHECK (capacity > 0),
    CONSTRAINT chk_room_units CHECK (total_units >= 0),
    CONSTRAINT chk_room_price CHECK (price_per_night >= 0),
    CONSTRAINT fk_rooms_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id)
);

CREATE TABLE room_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_room_images_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
);

CREATE TABLE room_pricing_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    price_per_night DECIMAL(10,2) NOT NULL,
    CONSTRAINT chk_room_rule_dates CHECK (end_date >= start_date),
    CONSTRAINT fk_room_rules_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
);

CREATE TABLE room_blocked_dates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    blocked_date DATE NOT NULL,
    reason VARCHAR(255),
    UNIQUE KEY uq_room_blocked_date (room_id, blocked_date),
    CONSTRAINT fk_room_blocked_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE
);

CREATE TABLE vehicles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vehicle_type VARCHAR(80) NOT NULL,
    model VARCHAR(120) NOT NULL,
    seat_capacity INT NOT NULL,
    air_conditioned BOOLEAN NOT NULL DEFAULT TRUE,
    price_per_day DECIMAL(10,2) NOT NULL DEFAULT 0,
    price_per_km DECIMAL(10,2) NOT NULL DEFAULT 0,
    driver_included BOOLEAN NOT NULL DEFAULT TRUE,
    units_available INT NOT NULL,
    pickup_city VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_vehicle_seats CHECK (seat_capacity > 0),
    CONSTRAINT chk_vehicle_units CHECK (units_available >= 0)
);

CREATE TABLE destination_vehicles (
    destination_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    PRIMARY KEY (destination_id, vehicle_id),
    CONSTRAINT fk_destination_vehicles_destination FOREIGN KEY (destination_id) REFERENCES destinations(id) ON DELETE CASCADE,
    CONSTRAINT fk_destination_vehicles_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);

CREATE TABLE vehicle_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    vehicle_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_vehicle_images_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
);

CREATE TABLE packages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    destination_id BIGINT NOT NULL,
    title VARCHAR(180) NOT NULL,
    slug VARCHAR(200) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    duration_days INT NOT NULL,
    price_per_person DECIMAL(10,2) NOT NULL,
    inclusions JSON NOT NULL,
    hotel_id BIGINT,
    image_url VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_package_duration CHECK (duration_days > 0),
    CONSTRAINT chk_package_price CHECK (price_per_person >= 0),
    CONSTRAINT fk_packages_destination FOREIGN KEY (destination_id) REFERENCES destinations(id),
    CONSTRAINT fk_packages_hotel FOREIGN KEY (hotel_id) REFERENCES hotels(id)
);

CREATE TABLE package_dates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    package_id BIGINT NOT NULL,
    travel_date DATE NOT NULL,
    seats_total INT NOT NULL,
    seats_booked INT NOT NULL DEFAULT 0,
    UNIQUE KEY uq_package_date (package_id, travel_date),
    CONSTRAINT chk_package_seats CHECK (seats_total >= seats_booked AND seats_booked >= 0),
    CONSTRAINT fk_package_dates_package FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE CASCADE
);

CREATE TABLE package_places (
    package_id BIGINT NOT NULL,
    place_id BIGINT NOT NULL,
    day_number INT NOT NULL,
    order_no INT NOT NULL,
    type ENUM('MANDATORY','OPTIONAL','ADD_ON') NOT NULL,
    add_on_price DECIMAL(10,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (package_id, place_id),
    UNIQUE KEY uq_package_place_order (package_id, day_number, order_no),
    CONSTRAINT chk_package_place_day CHECK (day_number > 0),
    CONSTRAINT chk_package_place_price CHECK (add_on_price >= 0),
    CONSTRAINT fk_package_places_package FOREIGN KEY (package_id) REFERENCES packages(id) ON DELETE CASCADE,
    CONSTRAINT fk_package_places_place FOREIGN KEY (place_id) REFERENCES places(id)
);

CREATE TABLE bookings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_code VARCHAR(40) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    package_id BIGINT NOT NULL,
    travel_date DATE,
    adults INT NOT NULL DEFAULT 0,
    children INT NOT NULL DEFAULT 0,
    total_people INT NOT NULL DEFAULT 0,
    status ENUM('DRAFT','CONFIRMED','CANCELLED','COMPLETED') NOT NULL DEFAULT 'DRAFT',
    base_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    extra_places_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    rooms_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    vehicles_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    draft_step TINYINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_booking_people CHECK (adults >= 0 AND children >= 0 AND total_people = adults + children),
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_bookings_package FOREIGN KEY (package_id) REFERENCES packages(id)
);

CREATE TABLE booking_travelers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    age INT NOT NULL,
    gender ENUM('MALE','FEMALE','NON_BINARY','PREFER_NOT_TO_SAY') NOT NULL,
    id_type VARCHAR(40) NOT NULL,
    id_number VARCHAR(120) NOT NULL,
    CONSTRAINT chk_traveler_age CHECK (age >= 0),
    CONSTRAINT fk_booking_travelers_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

CREATE TABLE booking_places (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    place_id BIGINT NOT NULL,
    day_number INT NOT NULL,
    order_no INT NOT NULL,
    price DECIMAL(10,2) NOT NULL DEFAULT 0,
    UNIQUE KEY uq_booking_place (booking_id, place_id),
    CONSTRAINT fk_booking_places_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_places_place FOREIGN KEY (place_id) REFERENCES places(id)
);

CREATE TABLE booking_rooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price_per_night DECIMAL(10,2) NOT NULL,
    nights INT NOT NULL,
    CONSTRAINT chk_booking_room_quantity CHECK (quantity > 0),
    CONSTRAINT fk_booking_rooms_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_rooms_room FOREIGN KEY (room_id) REFERENCES rooms(id)
);

CREATE TABLE booking_vehicles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price_per_day DECIMAL(10,2) NOT NULL,
    days INT NOT NULL,
    CONSTRAINT chk_booking_vehicle_quantity CHECK (quantity > 0),
    CONSTRAINT fk_booking_vehicles_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_vehicles_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);

CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    payment_reference VARCHAR(80) NOT NULL UNIQUE,
    amount DECIMAL(12,2) NOT NULL,
    status ENUM('PENDING','SUCCESS','REFUNDED','FAILED') NOT NULL,
    paid_at TIMESTAMP NULL,
    refund_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

CREATE TABLE reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    package_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL UNIQUE,
    rating TINYINT NOT NULL,
    review_text VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_review_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_reviews_package FOREIGN KEY (package_id) REFERENCES packages(id),
    CONSTRAINT fk_reviews_booking FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

CREATE TABLE ai_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NULL,
    endpoint VARCHAR(120) NOT NULL,
    request_summary JSON,
    response_summary JSON,
    provider VARCHAR(60) NOT NULL,
    success BOOLEAN NOT NULL,
    latency_ms BIGINT,
    error_message VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_places_destination ON places(destination_id, active);
CREATE INDEX idx_hotels_destination ON hotels(destination_id, active);
CREATE INDEX idx_rooms_hotel ON rooms(hotel_id, active);
CREATE INDEX idx_package_dates_date ON package_dates(travel_date, package_id);
CREATE INDEX idx_packages_destination ON packages(destination_id, active);
CREATE INDEX idx_bookings_user_status ON bookings(user_id, status);
CREATE INDEX idx_bookings_travel_date ON bookings(travel_date, status);
CREATE INDEX idx_ai_logs_created_at ON ai_logs(created_at);
