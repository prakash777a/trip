package com.tripcraft.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

  private JdbcTemplate db;
  private BookingService service;

  @BeforeEach
  void setUp() {
    db = mock(JdbcTemplate.class);
    service = new BookingService(db);
  }

  @Test
  void saveDetailsThrowsWhenBookingIsNotDraft() {
    when(db.queryForMap(startsWith("SELECT status"), anyLong()))
        .thenReturn(Map.of("status", "CONFIRMED"));

    assertThatThrownBy(() -> service.saveDetails(1L, Map.of("travelDate", "2026-10-15")))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Only draft bookings can be modified");
  }

  @Test
  void confirmAndPayThrowsWhenTravelersCountDoesNotMatch() {
    Date travelDate = Date.valueOf(LocalDate.now().plusDays(10));
    when(db.queryForMap(startsWith("SELECT * FROM bookings WHERE id=? FOR UPDATE"), anyLong()))
        .thenReturn(Map.of("id", 1L, "package_id", 10L, "status", "DRAFT", "adults", 2, "children", 0, "total_people", 2, "travel_date", travelDate));
    when(db.queryForList(startsWith("SELECT * FROM package_dates WHERE package_id=?"), anyLong(), any()))
        .thenReturn(List.of(Map.of("id", 5L, "seats_total", 20, "seats_booked", 0)));
    when(db.queryForObject(contains("COUNT(*) FROM package_places pp"), eq(Integer.class), anyLong()))
        .thenReturn(1);
    when(db.queryForObject(contains("COUNT(*) FROM booking_places bp"), eq(Integer.class), anyLong(), anyLong()))
        .thenReturn(1);
    when(db.queryForList(contains("FROM booking_rooms"), anyLong()))
        .thenReturn(List.of(Map.of("room_id", 1L, "quantity", 1, "total_units", 5)));
    when(db.queryForList(contains("FROM booking_vehicles"), anyLong()))
        .thenReturn(List.of(Map.of("vehicle_id", 1L, "quantity", 1, "units_available", 5)));
    when(db.queryForObject(contains("COUNT(*) FROM booking_travelers"), eq(Integer.class), anyLong()))
        .thenReturn(0);

    assertThatThrownBy(() -> service.confirmAndPay(1L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Traveler details are incomplete");
  }

  @Test
  void cancelDraftReturnsZeroRefund() {
    when(db.queryForMap(startsWith("SELECT * FROM bookings WHERE id=? FOR UPDATE"), anyLong()))
        .thenReturn(Map.of("id", 1L, "status", "DRAFT", "total_amount", BigDecimal.ZERO));
    when(db.queryForMap(startsWith("SELECT b.*,p.title"), anyLong()))
        .thenReturn(new HashMap<>(Map.of("id", 1L, "status", "CANCELLED", "total_amount", BigDecimal.ZERO)));

    Map<String, Object> result = service.cancel(1L);

    assertThat(result.get("refundAmount")).isEqualTo(BigDecimal.ZERO);
    verify(db).update(contains("UPDATE bookings SET status='CANCELLED'"), eq(1L));
  }

  @Test
  void cancelConfirmedMoreThanSevenDaysAheadReturnsFullRefund() {
    BigDecimal total = new BigDecimal("10000.00");
    Date futureDate = Date.valueOf(LocalDate.now().plusDays(10));
    when(db.queryForMap(startsWith("SELECT * FROM bookings WHERE id=? FOR UPDATE"), anyLong()))
        .thenReturn(Map.of("id", 2L, "status", "CONFIRMED", "total_amount", total, "travel_date", futureDate, "total_people", 2, "package_id", 5L));
    when(db.queryForMap(startsWith("SELECT b.*,p.title"), anyLong()))
        .thenReturn(new HashMap<>(Map.of("id", 2L, "status", "CANCELLED", "total_amount", total)));

    Map<String, Object> result = service.cancel(2L);

    assertThat((BigDecimal) result.get("refundAmount")).isEqualByComparingTo("10000.00");
    verify(db).update(contains("UPDATE bookings SET status='CANCELLED'"), eq(2L));
    verify(db).update(contains("UPDATE payments SET status='REFUNDED'"), eq(total), eq(2L));
  }
}
