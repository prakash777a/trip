package com.tripcraft.security;
import org.springframework.jdbc.core.JdbcTemplate; import org.springframework.stereotype.Component;
@Component("bookingSecurity") public class BookingSecurity {private final JdbcTemplate db;public BookingSecurity(JdbcTemplate db){this.db=db;}public boolean isOwner(long id,String email){Integer n=db.queryForObject("SELECT COUNT(*) FROM bookings b JOIN users u ON u.id=b.user_id WHERE b.id=? AND u.email=?",Integer.class,id,email);return n!=null&&n>0;}}
