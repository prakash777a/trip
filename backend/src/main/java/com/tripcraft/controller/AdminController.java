package com.tripcraft.controller;

import com.tripcraft.service.BulkUploadService;
import com.tripcraft.service.ImageStorageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final JdbcTemplate db;
    private final BulkUploadService bulk;
    private final ImageStorageService images;

    public AdminController(JdbcTemplate db, BulkUploadService bulk, ImageStorageService images) {
        this.db = db;
        this.bulk = bulk;
        this.images = images;
    }

    @GetMapping("/{resource:destinations|places|packages|hotels|rooms|vehicles|bookings|users}")
    public Map<String, Object> list(@PathVariable String resource) {
        String sql = "users".equals(resource)
                ? "SELECT id,email,full_name fullName,phone,role,active,created_at createdAt FROM users ORDER BY id DESC"
                : "SELECT * FROM " + table(resource) + " ORDER BY id DESC";
        return Map.of("content", db.queryForList(sql));
    }

    /** A read-only, human-friendly booking view for the separate admin bookings page. */
    @GetMapping("/bookings/overview")
    public Map<String, Object> bookingOverview() {
        List<Map<String, Object>> bookings = db.queryForList("""
            SELECT b.id, b.booking_code AS bookingCode, b.status, b.travel_date AS travelDate,
                   b.total_people AS totalPeople, b.total_amount AS totalAmount,
                   b.created_at AS createdAt, u.full_name AS travellerName, u.email AS travellerEmail,
                   p.title AS packageTitle, d.name AS destinationName
            FROM bookings b
            JOIN users u ON u.id = b.user_id
            JOIN packages p ON p.id = b.package_id
            JOIN destinations d ON d.id = p.destination_id
            ORDER BY b.created_at DESC
            """);
        return Map.of("content", bookings, "totalElements", bookings.size());
    }

    @PostMapping("/{resource}")
    public ResponseEntity<?> create(@PathVariable String resource, @RequestBody Map<String, Object> b) {
        switch (resource) {
            case "destinations" -> db.update("INSERT INTO destinations(name,slug,state,country,description,best_season,hero_image_url) VALUES(?,?,?,?,?,?,?)", b.get("name"), b.get("slug"), b.get("state"), b.getOrDefault("country", "India"), b.get("description"), b.get("bestSeason"), b.get("heroImageUrl"));
            case "places" -> db.update("INSERT INTO places(destination_id,name,slug,description,suitable_for,best_season,difficulty,price,duration_minutes,image_url) VALUES(?,?,?,?,?,?,?,?,?,?)", b.get("destinationId"), b.get("name"), b.get("slug"), b.get("description"), b.get("suitableFor"), b.get("bestSeason"), b.getOrDefault("difficulty", "EASY"), b.getOrDefault("price", 0), b.getOrDefault("durationMinutes", 60), b.get("imageUrl"));
            case "hotels" -> db.update("INSERT INTO hotels(destination_id,name,description,address,star_rating,image_url) VALUES(?,?,?,?,?,?)", b.get("destinationId"), b.get("name"), b.get("description"), b.get("address"), b.getOrDefault("starRating", 3), b.get("imageUrl"));
            case "rooms" -> db.update("INSERT INTO rooms(hotel_id,room_type,capacity,price_per_night,total_units,amenities) VALUES(?,?,?,?,?,?)", b.get("hotelId"), b.get("roomType"), b.get("capacity"), b.get("pricePerNight"), b.get("totalUnits"), b.getOrDefault("amenities", "[]"));
            case "vehicles" -> db.update("INSERT INTO vehicles(vehicle_type,model,seat_capacity,air_conditioned,price_per_day,price_per_km,driver_included,units_available,pickup_city) VALUES(?,?,?,?,?,?,?,?,?)", b.get("vehicleType"), b.get("model"), b.get("seatCapacity"), b.getOrDefault("airConditioned", true), b.getOrDefault("pricePerDay", 0), b.getOrDefault("pricePerKm", 0), b.getOrDefault("driverIncluded", true), b.getOrDefault("unitsAvailable", 0), b.get("pickupCity"));
            case "packages" -> db.update("INSERT INTO packages(destination_id,title,slug,description,duration_days,price_per_person,inclusions,hotel_id,image_url) VALUES(?,?,?,?,?,?,?,?,?)", b.get("destinationId"), b.get("title"), b.get("slug"), b.get("description"), b.get("durationDays"), b.get("pricePerPerson"), b.getOrDefault("inclusions", "[]"), b.get("hotelId"), b.get("imageUrl"));
            default -> { return ResponseEntity.badRequest().body(Map.of("message", "Unsupported admin resource")); }
        }
        long id = db.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if ("vehicles".equals(resource) && b.get("destinationIds") instanceof List<?> destinations) {
            for (Object destination : destinations) db.update("INSERT INTO destination_vehicles(destination_id,vehicle_id) VALUES(?,?)", destination, id);
        }
        if ("packages".equals(resource)) createPackageChildren(id, b);
        return ResponseEntity.status(201).body(Map.of("id", id, "resource", resource));
    }

    @SuppressWarnings("unchecked")
    private void createPackageChildren(long packageId, Map<String, Object> body) {
        Object rawDates = body.get("availableDates");
        if (rawDates instanceof List<?> dates && !dates.isEmpty()) {
            for (Object raw : dates) {
                if (raw instanceof Map<?, ?> source) {
                    Map<String, Object> date = (Map<String, Object>) source;
                    Object seats = date.get("seatsTotal") == null ? 30 : date.get("seatsTotal");
                    db.update("INSERT INTO package_dates(package_id,travel_date,seats_total) VALUES(?,?,?)", packageId, date.get("travelDate"), seats);
                }
            }
        } else {
            db.update("INSERT INTO package_dates(package_id,travel_date,seats_total) VALUES(?,?,?)", packageId, LocalDate.now().plusDays(20), 30);
            db.update("INSERT INTO package_dates(package_id,travel_date,seats_total) VALUES(?,?,?)", packageId, LocalDate.now().plusDays(50), 30);
        }
        if (body.get("places") instanceof List<?>) packagePlaces(packageId, body);
    }

    @PutMapping("/{resource}/{id}")
    public Map<String, Object> update(@PathVariable String resource, @PathVariable long id, @RequestBody Map<String, Object> b) {
        switch (resource) {
            case "destinations" -> db.update("UPDATE destinations SET name=COALESCE(?,name),state=COALESCE(?,state),description=COALESCE(?,description),best_season=COALESCE(?,best_season),hero_image_url=COALESCE(?,hero_image_url),active=COALESCE(?,active) WHERE id=?", b.get("name"), b.get("state"), b.get("description"), b.get("bestSeason"), b.get("heroImageUrl"), b.get("active"), id);
            case "places" -> db.update("UPDATE places SET name=COALESCE(?,name),description=COALESCE(?,description),suitable_for=COALESCE(?,suitable_for),best_season=COALESCE(?,best_season),difficulty=COALESCE(?,difficulty),price=COALESCE(?,price),duration_minutes=COALESCE(?,duration_minutes),image_url=COALESCE(?,image_url),active=COALESCE(?,active) WHERE id=?", b.get("name"), b.get("description"), b.get("suitableFor"), b.get("bestSeason"), b.get("difficulty"), b.get("price"), b.get("durationMinutes"), b.get("imageUrl"), b.get("active"), id);
            case "packages" -> db.update("UPDATE packages SET title=COALESCE(?,title),description=COALESCE(?,description),duration_days=COALESCE(?,duration_days),price_per_person=COALESCE(?,price_per_person),inclusions=COALESCE(?,inclusions),hotel_id=COALESCE(?,hotel_id),image_url=COALESCE(?,image_url),active=COALESCE(?,active) WHERE id=?", b.get("title"), b.get("description"), b.get("durationDays"), b.get("pricePerPerson"), b.get("inclusions"), b.get("hotelId"), b.get("imageUrl"), b.get("active"), id);
            case "hotels" -> db.update("UPDATE hotels SET name=COALESCE(?,name),description=COALESCE(?,description),address=COALESCE(?,address),star_rating=COALESCE(?,star_rating),image_url=COALESCE(?,image_url),active=COALESCE(?,active) WHERE id=?", b.get("name"), b.get("description"), b.get("address"), b.get("starRating"), b.get("imageUrl"), b.get("active"), id);
            case "rooms" -> db.update("UPDATE rooms SET room_type=COALESCE(?,room_type),capacity=COALESCE(?,capacity),price_per_night=COALESCE(?,price_per_night),total_units=COALESCE(?,total_units),amenities=COALESCE(?,amenities),active=COALESCE(?,active) WHERE id=?", b.get("roomType"), b.get("capacity"), b.get("pricePerNight"), b.get("totalUnits"), b.get("amenities"), b.get("active"), id);
            case "vehicles" -> db.update("UPDATE vehicles SET vehicle_type=COALESCE(?,vehicle_type),model=COALESCE(?,model),seat_capacity=COALESCE(?,seat_capacity),air_conditioned=COALESCE(?,air_conditioned),price_per_day=COALESCE(?,price_per_day),price_per_km=COALESCE(?,price_per_km),driver_included=COALESCE(?,driver_included),units_available=COALESCE(?,units_available),pickup_city=COALESCE(?,pickup_city),active=COALESCE(?,active) WHERE id=?", b.get("vehicleType"), b.get("model"), b.get("seatCapacity"), b.get("airConditioned"), b.get("pricePerDay"), b.get("pricePerKm"), b.get("driverIncluded"), b.get("unitsAvailable"), b.get("pickupCity"), b.get("active"), id);
            case "users" -> { if (b.containsKey("active")) db.update("UPDATE users SET active=? WHERE id=?", b.get("active"), id); }
            default -> throw new IllegalArgumentException("Unsupported admin resource");
        }
        return Map.of("id", id, "resource", resource, "saved", true);
    }

    @SuppressWarnings("unchecked")
    @PutMapping("/packages/{id}/places")
    public Map<String, Object> packagePlaces(@PathVariable long id, @RequestBody Map<String, Object> body) {
        if (!(body.get("places") instanceof List<?> places) || places.isEmpty()) throw new IllegalArgumentException("At least one package place is required");
        db.update("DELETE FROM package_places WHERE package_id=?", id);
        int order = 1;
        for (Object raw : places) {
            if (!(raw instanceof Map<?, ?> source)) continue;
            Map<String, Object> place = (Map<String, Object>) source;
            String type = String.valueOf(place.getOrDefault("type", "OPTIONAL"));
            if (!Set.of("MANDATORY", "OPTIONAL", "ADD_ON").contains(type)) throw new IllegalArgumentException("Invalid package place type");
            db.update("INSERT INTO package_places(package_id,place_id,day_number,order_no,type,add_on_price) VALUES(?,?,?,?,?,?)", id, place.get("placeId"), place.getOrDefault("dayNumber", 1), place.getOrDefault("orderNo", order++), type, place.getOrDefault("addOnPrice", 0));
        }
        return Map.of("packageId", id, "places", db.queryForList("SELECT * FROM package_places WHERE package_id=? ORDER BY day_number,order_no", id));
    }

    @DeleteMapping("/{resource}/{id}")
    public ResponseEntity<?> delete(@PathVariable String resource, @PathVariable long id) {
        if ("rooms".equals(resource) && db.queryForObject("SELECT COUNT(*) FROM booking_rooms br JOIN bookings b ON b.id=br.booking_id WHERE br.room_id=? AND b.status IN ('DRAFT','CONFIRMED')", Integer.class, id) > 0) throw new IllegalStateException("Room has future bookings; deactivate it instead");
        if ("vehicles".equals(resource) && db.queryForObject("SELECT COUNT(*) FROM booking_vehicles bv JOIN bookings b ON b.id=bv.booking_id WHERE bv.vehicle_id=? AND b.status IN ('DRAFT','CONFIRMED')", Integer.class, id) > 0) throw new IllegalStateException("Vehicle has future bookings; deactivate it instead");
        db.update("DELETE FROM " + table(resource) + " WHERE id=?", id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/hotels/{id}/rooms/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> roomsUpload(@PathVariable long id, @RequestPart MultipartFile file) { return bulk.rooms(id, file); }

    @PostMapping(value = "/destinations/{id}/vehicles/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> vehiclesUpload(@PathVariable long id, @RequestPart MultipartFile file) { return bulk.vehicles(id, file); }

    @GetMapping(value = "/hotels/{id}/rooms/bulk-template", produces = "text/csv")
    public ResponseEntity<byte[]> roomTemplate() { return csv("roomType,capacity,pricePerNight,totalUnits,amenities\nGarden Room,2,3200,10,Breakfast|Wi-Fi\n"); }

    @GetMapping(value = "/destinations/{id}/vehicles/bulk-template", produces = "text/csv")
    public ResponseEntity<byte[]> vehicleTemplate() { return csv("vehicleType,model,seatCapacity,airConditioned,pricePerDay,pricePerKm,driverIncluded,unitsAvailable,pickupCity\nSedan,Honda City,4,true,3000,17,true,5,Jaipur\n"); }

    @PostMapping("/rooms/{id}/pricing-rules")
    public Map<String, Object> pricingRule(@PathVariable long id, @RequestBody Map<String, Object> b) { db.update("INSERT INTO room_pricing_rules(room_id,start_date,end_date,price_per_night) VALUES(?,?,?,?)", id, b.get("startDate"), b.get("endDate"), b.get("pricePerNight")); return Map.of("saved", true); }

    @PostMapping("/rooms/{id}/blocked-dates")
    public Map<String, Object> blockedDate(@PathVariable long id, @RequestBody Map<String, Object> b) { db.update("INSERT INTO room_blocked_dates(room_id,blocked_date,reason) VALUES(?,?,?)", id, b.get("date"), b.get("reason")); return Map.of("saved", true); }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> image(@RequestPart MultipartFile file) throws Exception { return Map.of("url", images.store(file)); }

    private String table(String resource) { return switch (resource) { case "destinations", "places", "packages", "hotels", "rooms", "vehicles", "bookings", "users" -> resource; default -> throw new IllegalArgumentException("Unknown resource"); }; }
    private ResponseEntity<byte[]> csv(String value) { return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tripcraft-template.csv").body(value.getBytes(StandardCharsets.UTF_8)); }
}
