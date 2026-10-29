package com.tripcraft.security;

import io.jsonwebtoken.Jwts; import io.jsonwebtoken.security.Keys; import org.springframework.beans.factory.annotation.Value; import org.springframework.stereotype.Service;
import javax.crypto.SecretKey; import java.nio.charset.StandardCharsets; import java.util.Date;
@Service public class JwtService {
  private final SecretKey key; public JwtService(@Value("${jwt.secret}") String secret){
    if(secret==null || secret.isBlank() || secret.length()<32) throw new IllegalStateException("JWT_SECRET must be provided and contain at least 32 characters");
    key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }
  public String issue(String email,String role){ return Jwts.builder().subject(email).claim("role",role).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis()+86400000)).signWith(key).compact(); }
  public String email(String token){ return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject(); }
  public String role(String token){ return (String)Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("role"); }
}
