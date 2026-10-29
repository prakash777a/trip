package com.tripcraft.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** All mutable booking operations live here so the controller cannot accidentally bypass pricing or locks. */
@Service
public class BookingService {
  private final JdbcTemplate db;
  public BookingService(JdbcTemplate db){this.db=db;}

  @Transactional
  public Map<String,Object> saveDetails(long id, Map<String,Object> body){
    assertDraft(id);
    int adults=intValue(body.get("adults")), children=intValue(body.get("children"));
    if(adults<1 || children<0) throw new IllegalArgumentException("At least one adult is required");
    LocalDate date=LocalDate.parse(String.valueOf(body.get("travelDate")));
    if(date.isBefore(LocalDate.now())) throw new IllegalArgumentException("Travel date must be in the future");
    db.update("UPDATE bookings SET travel_date=?,adults=?,children=?,total_people=?,draft_step=2 WHERE id=?",date,adults,children,adults+children,id);
    return booking(id);
  }

  @Transactional
  public Map<String,Object> savePlaces(long id, List<Map<String,Object>> places){
    assertDraft(id);
    db.update("DELETE FROM booking_places WHERE booking_id=?",id);
    for(Map<String,Object> p: places){
      db.update("INSERT INTO booking_places(booking_id,place_id,day_number,order_no,price) SELECT ?,id,?,?,price FROM places WHERE id=?",id,intValue(p.getOrDefault("dayNumber",1)),intValue(p.getOrDefault("orderNo",1)),p.get("placeId"));
    }
    db.update("UPDATE bookings SET draft_step=3 WHERE id=?",id); return booking(id);
  }

  @Transactional public Map<String,Object> addPlace(long bookingId,long placeId,int day,int order){Map<String,Object> b=booking(bookingId);if(!"DRAFT".equals(b.get("status")))throw new IllegalStateException("Only draft bookings can be modified");List<Map<String,Object>> matches=db.queryForList("SELECT COALESCE(pp.type,'ADD_ON') type,COALESCE(pp.add_on_price,pl.price) add_on_price FROM bookings bk JOIN packages p ON p.id=bk.package_id JOIN places pl ON pl.destination_id=p.destination_id AND pl.id=? LEFT JOIN package_places pp ON pp.package_id=p.id AND pp.place_id=pl.id WHERE bk.id=?",placeId,bookingId);if(matches.isEmpty())throw new IllegalArgumentException("That place is not available for this package");Map<String,Object> pp=matches.get(0);if(db.queryForObject("SELECT COUNT(*) FROM booking_places WHERE booking_id=? AND place_id=?",Integer.class,bookingId,placeId)>0)return b;db.update("INSERT INTO booking_places(booking_id,place_id,day_number,order_no,price) SELECT ?,id,?,?,CASE WHEN ?='MANDATORY' THEN 0 ELSE ? END FROM places WHERE id=?",bookingId,day,order,pp.get("type"),pp.get("add_on_price"),placeId);return booking(bookingId);}
  @Transactional public Map<String,Object> removePlace(long bookingId,long placeId){Map<String,Object>b=booking(bookingId);if(!"DRAFT".equals(b.get("status")))throw new IllegalStateException("Only draft bookings can be modified");Map<String,Object> pp=db.queryForMap("SELECT COALESCE(pp.type,'ADD_ON') type FROM bookings bk JOIN packages p ON p.id=bk.package_id JOIN places pl ON pl.destination_id=p.destination_id AND pl.id=? LEFT JOIN package_places pp ON pp.package_id=p.id AND pp.place_id=pl.id WHERE bk.id=?",placeId,bookingId);if("MANDATORY".equals(pp.get("type")))throw new IllegalStateException("Mandatory places cannot be removed");db.update("DELETE FROM booking_places WHERE booking_id=? AND place_id=?",bookingId,placeId);return booking(bookingId);}
  @Transactional public Map<String,Object> reorderPlaces(long bookingId,List<Map<String,Object>> places){
    assertDraft(bookingId);
    for(Map<String,Object> p:places){
      long placeId=Long.parseLong(String.valueOf(p.get("placeId")));
      String type=db.queryForObject("SELECT COALESCE(pp.type,'ADD_ON') FROM booking_places bp JOIN bookings b ON b.id=bp.booking_id LEFT JOIN package_places pp ON pp.package_id=b.package_id AND pp.place_id=bp.place_id WHERE bp.booking_id=? AND bp.place_id=?",String.class,bookingId,placeId);
      if(!"MANDATORY".equals(type)){int day=intValue(p.getOrDefault("dayNumber",1)), order=intValue(p.getOrDefault("orderNo",1));db.update("UPDATE booking_places SET day_number=?,order_no=? WHERE booking_id=? AND place_id=?",day,order,bookingId,placeId);}
    }
    return booking(bookingId);
  }

  @Transactional
  public Map<String,Object> saveRooms(long id,List<Map<String,Object>> rooms){
    assertDraft(id);
    Map<String,Object> b=booking(id); int people=intValue(b.get("total_people"));
    db.update("DELETE FROM booking_rooms WHERE booking_id=?",id);
    int capacity=0; int nights=Math.max(1,packageDays(id)-1);
    for(Map<String,Object> r: rooms){
      int quantity=intValue(r.getOrDefault("quantity",1)); Map<String,Object> room=db.queryForMap("SELECT capacity,price_per_night FROM rooms WHERE id=? AND active=1",r.get("roomId"));
      capacity+=intValue(room.get("capacity"))*quantity;
      db.update("INSERT INTO booking_rooms(booking_id,room_id,quantity,price_per_night,nights) VALUES(?,?,?,?,?)",id,r.get("roomId"),quantity,room.get("price_per_night"),nights);
    }
    if(capacity<people) throw new IllegalArgumentException("You need "+(people-capacity)+" more beds for "+people+" people");
    db.update("UPDATE bookings SET draft_step=4 WHERE id=?",id); return booking(id);
  }

  @Transactional
  public Map<String,Object> saveVehicles(long id,List<Map<String,Object>> vehicles){
    assertDraft(id);
    Map<String,Object> b=booking(id); int people=intValue(b.get("total_people"));
    db.update("DELETE FROM booking_vehicles WHERE booking_id=?",id);
    int seats=0; int days=packageDays(id);
    for(Map<String,Object> v: vehicles){int quantity=intValue(v.getOrDefault("quantity",1)); Map<String,Object> vehicle=db.queryForMap("SELECT seat_capacity,price_per_day FROM vehicles WHERE id=? AND active=1",v.get("vehicleId")); seats+=intValue(vehicle.get("seat_capacity"))*quantity; db.update("INSERT INTO booking_vehicles(booking_id,vehicle_id,quantity,price_per_day,days) VALUES(?,?,?,?,?)",id,v.get("vehicleId"),quantity,vehicle.get("price_per_day"),days);}
    if(seats<people) throw new IllegalArgumentException("You need "+(people-seats)+" more vehicle seats");
    db.update("UPDATE bookings SET draft_step=5 WHERE id=?",id); return booking(id);
  }

  @Transactional public Map<String,Object> saveTravelers(long id,List<Map<String,Object>> travelers){
    assertDraft(id);
    int expected=intValue(booking(id).get("total_people")); 
    if(travelers==null||travelers.size()!=expected) throw new IllegalArgumentException("Add exactly "+expected+" travelers");
    db.update("DELETE FROM booking_travelers WHERE booking_id=?",id); 
    for(Map<String,Object> t:travelers){
      Object name=t.get("fullName")!=null?t.get("fullName"):t.get("full_name");
      Object idNum=t.get("idNumber")!=null?t.get("idNumber"):t.get("id_number");
      Object gender=t.getOrDefault("gender","PREFER_NOT_TO_SAY");
      Object idType=t.getOrDefault("idType",t.getOrDefault("id_type","GOVERNMENT_ID"));
      int age=intValue(t.get("age"));
      db.update("INSERT INTO booking_travelers(booking_id,full_name,age,gender,id_type,id_number) VALUES(?,?,?,?,?,?)",id,name,age,gender,idType,idNum);
    }
    db.update("UPDATE bookings SET draft_step=6 WHERE id=?",id); 
    return booking(id);
  }

  @Transactional public Map<String,Object> pricePreview(long id){
    Map<String,Object>b=booking(id); long people=intValue(b.get("total_people"));
    BigDecimal base=money(db.queryForObject("SELECT price_per_person FROM packages WHERE id=?",BigDecimal.class,b.get("package_id"))).multiply(BigDecimal.valueOf(people)).add(money((BigDecimal)b.get("extension_amount")));
    BigDecimal places=money(db.queryForObject("SELECT COALESCE(SUM(price),0) FROM booking_places WHERE booking_id=?",BigDecimal.class,id));
    BigDecimal rooms=money(db.queryForObject("SELECT COALESCE(SUM(price_per_night*quantity*nights),0) FROM booking_rooms WHERE booking_id=?",BigDecimal.class,id));
    BigDecimal vehicles=money(db.queryForObject("SELECT COALESCE(SUM(price_per_day*quantity*days),0) FROM booking_vehicles WHERE booking_id=?",BigDecimal.class,id));
    BigDecimal subtotal=base.add(places).add(rooms).add(vehicles), tax=subtotal.multiply(new BigDecimal("0.05")).setScale(2,RoundingMode.HALF_UP), total=subtotal.add(tax);
    db.update("UPDATE bookings SET base_amount=?,extra_places_amount=?,rooms_amount=?,vehicles_amount=?,tax_amount=?,total_amount=? WHERE id=?",base,places,rooms,vehicles,tax,total,id);
    return Map.of("basePackage",base,"extraPlaces",places,"rooms",rooms,"vehicles",vehicles,"tax",tax,"total",total);
  }
  @Transactional public Map<String,Object> extendDay(long id){assertDraft(id);Map<String,Object>b=db.queryForMap("SELECT * FROM bookings WHERE id=? FOR UPDATE",id);if(intValue(b.get("extended_days"))==1)return booking(id);BigDecimal charge=BigDecimal.valueOf(intValue(b.get("total_people"))).multiply(new BigDecimal("1500"));db.update("UPDATE bookings SET extended_days=1,extension_amount=? WHERE id=?",charge,id);return booking(id);}

  /** Locks the package date, room rows, and vehicle rows before checking remaining capacity. */
  @Transactional public Map<String,Object> confirmAndPay(long id){
    Map<String,Object>b=db.queryForMap("SELECT * FROM bookings WHERE id=? FOR UPDATE",id); if("CONFIRMED".equals(b.get("status"))) return booking(id); if("CANCELLED".equals(b.get("status"))) throw new IllegalStateException("Cancelled bookings cannot be paid"); if(b.get("travel_date")==null) throw new IllegalArgumentException("Trip details are incomplete");
    LocalDate date=((java.sql.Date)b.get("travel_date")).toLocalDate(); int people=intValue(b.get("total_people"));
    if(people <= 0) throw new IllegalArgumentException("Trip details are incomplete: total travelers must be at least 1");
    List<Map<String,Object>> dates=db.queryForList("SELECT * FROM package_dates WHERE package_id=? AND travel_date=? FOR UPDATE",b.get("package_id"),date);
    if(dates.isEmpty()){
      if(date.isBefore(LocalDate.now())) throw new IllegalArgumentException("Travel date cannot be in the past");
      db.update("INSERT INTO package_dates(package_id,travel_date,seats_total,seats_booked) VALUES(?,?,30,0)",b.get("package_id"),date);
      dates=db.queryForList("SELECT * FROM package_dates WHERE package_id=? AND travel_date=? FOR UPDATE",b.get("package_id"),date);
    }
    Map<String,Object>d=dates.get(0); int free=intValue(d.get("seats_total"))-intValue(d.get("seats_booked")); if(free<people) throw new IllegalStateException("Only "+free+" package seats remain");
    int mandatory=db.queryForObject("SELECT COUNT(*) FROM package_places pp WHERE pp.package_id=? AND pp.type='MANDATORY'",Integer.class,b.get("package_id"));
    int selectedMandatory=db.queryForObject("SELECT COUNT(*) FROM booking_places bp JOIN package_places pp ON pp.package_id=? AND pp.place_id=bp.place_id WHERE bp.booking_id=? AND pp.type='MANDATORY'",Integer.class,b.get("package_id"),id);
    if(selectedMandatory<mandatory){
      db.update("INSERT IGNORE INTO booking_places(booking_id,place_id,day_number,order_no,price) SELECT ?,place_id,day_number,order_no,0 FROM package_places WHERE package_id=? AND type='MANDATORY'",id,b.get("package_id"));
    }
    List<Map<String,Object>> selectedRooms=db.queryForList("SELECT br.*,rm.total_units FROM booking_rooms br JOIN rooms rm ON rm.id=br.room_id WHERE br.booking_id=?",id); if(selectedRooms.isEmpty()) throw new IllegalArgumentException("Room selection is required"); List<Map<String,Object>> selectedVehicles=db.queryForList("SELECT bv.*,ve.units_available FROM booking_vehicles bv JOIN vehicles ve ON ve.id=bv.vehicle_id WHERE bv.booking_id=?",id); if(selectedVehicles.isEmpty()) throw new IllegalArgumentException("Vehicle selection is required"); if(db.queryForObject("SELECT COUNT(*) FROM booking_travelers WHERE booking_id=?",Integer.class,id)!=people) throw new IllegalArgumentException("Traveler details are incomplete");
    for(Map<String,Object> r:selectedRooms){
      db.queryForList("SELECT id FROM rooms WHERE id=? FOR UPDATE",r.get("room_id")); int used=countConfirmed("booking_rooms","room_id",r.get("room_id"),id,date); if(intValue(r.get("quantity"))+used>intValue(r.get("total_units"))) throw new IllegalStateException("Room availability changed; please choose another room");
    }
    for(Map<String,Object> v:selectedVehicles){
      db.queryForList("SELECT id FROM vehicles WHERE id=? FOR UPDATE",v.get("vehicle_id")); int used=countConfirmed("booking_vehicles","vehicle_id",v.get("vehicle_id"),id,date); if(intValue(v.get("quantity"))+used>intValue(v.get("units_available"))) throw new IllegalStateException("Vehicle availability changed; please choose another vehicle");
    }
    pricePreview(id); db.update("UPDATE package_dates SET seats_booked=seats_booked+? WHERE id=?",people,d.get("id")); db.update("UPDATE bookings SET status='CONFIRMED' WHERE id=?",id); db.update("INSERT INTO payments(booking_id,payment_reference,amount,status,paid_at) SELECT id,UUID(),total_amount,'SUCCESS',CURRENT_TIMESTAMP FROM bookings WHERE id=?",id); return booking(id);
  }

  @Transactional public Map<String,Object> cancel(long id){
    Map<String,Object>b=db.queryForMap("SELECT * FROM bookings WHERE id=? FOR UPDATE",id);
    if("CANCELLED".equals(b.get("status"))) return booking(id);
    boolean wasConfirmed="CONFIRMED".equals(b.get("status"));
    LocalDate date=b.get("travel_date")==null?LocalDate.now():((java.sql.Date)b.get("travel_date")).toLocalDate();
    long days=ChronoUnit.DAYS.between(LocalDate.now(),date);
    BigDecimal total=money((BigDecimal)b.get("total_amount"));
    BigDecimal refund=wasConfirmed?(days>=7?total:days>=3?total.multiply(new BigDecimal(".5")):BigDecimal.ZERO):BigDecimal.ZERO;
    if(wasConfirmed&&b.get("travel_date")!=null)
      db.update("UPDATE package_dates SET seats_booked=GREATEST(0,seats_booked-?) WHERE package_id=? AND travel_date=?",b.get("total_people"),b.get("package_id"),date);
    db.update("UPDATE bookings SET status='CANCELLED' WHERE id=?",id);
    if(wasConfirmed) db.update("UPDATE payments SET status='REFUNDED',refund_amount=? WHERE booking_id=? AND status='SUCCESS'",refund,id);
    return Map.of("booking",booking(id),"refundAmount",refund,"daysBeforeTravel",days);
  }

  public Map<String,Object> booking(long id){Map<String,Object>b=db.queryForMap("SELECT b.*,p.title,p.destination_id,d.name destinationName FROM bookings b JOIN packages p ON p.id=b.package_id JOIN destinations d ON d.id=p.destination_id WHERE b.id=?",id);b.put("travelers",db.queryForList("SELECT * FROM booking_travelers WHERE booking_id=?",id));b.put("places",db.queryForList("SELECT bp.*,pp.type,pl.name FROM booking_places bp JOIN places pl ON pl.id=bp.place_id LEFT JOIN package_places pp ON pp.package_id=(SELECT package_id FROM bookings WHERE id=?) AND pp.place_id=bp.place_id WHERE bp.booking_id=? ORDER BY bp.day_number,bp.order_no",id,id));b.put("rooms",db.queryForList("SELECT br.*,rm.room_type FROM booking_rooms br JOIN rooms rm ON rm.id=br.room_id WHERE br.booking_id=?",id));b.put("vehicles",db.queryForList("SELECT bv.*,v.model FROM booking_vehicles bv JOIN vehicles v ON v.id=bv.vehicle_id WHERE bv.booking_id=?",id));return b;}
  private void assertDraft(long id){Map<String,Object>b=booking(id);if(!"DRAFT".equals(b.get("status")))throw new IllegalStateException("Only draft bookings can be modified");}
  private int packageDays(long id){return db.queryForObject("SELECT p.duration_days+b.extended_days FROM packages p JOIN bookings b ON b.package_id=p.id WHERE b.id=?",Integer.class,id);}
  private int countConfirmed(String table,String column,Object value,long current,LocalDate date){return db.queryForObject("SELECT COALESCE(SUM(x.quantity),0) FROM "+table+" x JOIN bookings b ON b.id=x.booking_id WHERE x."+column+"=? AND b.status='CONFIRMED' AND b.travel_date=? AND x.booking_id<>?",Integer.class,value,date,current);}
  private int intValue(Object x){return x==null?0:((Number)x).intValue();} private BigDecimal money(BigDecimal x){return x==null?BigDecimal.ZERO:x;}
}
