ALTER TABLE bookings ADD COLUMN extended_days TINYINT NOT NULL DEFAULT 0;
ALTER TABLE bookings ADD COLUMN extension_amount DECIMAL(12,2) NOT NULL DEFAULT 0;
ALTER TABLE bookings ADD CONSTRAINT chk_booking_extended_days CHECK (extended_days BETWEEN 0 AND 1);
