package com.tripcraft.security;
import org.junit.jupiter.api.Test; import static org.assertj.core.api.Assertions.assertThat;
class JwtServiceTest { @Test void roundTripsClaims(){var service=new JwtService("12345678901234567890123456789012");String token=service.issue("tourist@test","ROLE_TOURIST");assertThat(service.email(token)).isEqualTo("tourist@test");assertThat(service.role(token)).isEqualTo("ROLE_TOURIST");} }
