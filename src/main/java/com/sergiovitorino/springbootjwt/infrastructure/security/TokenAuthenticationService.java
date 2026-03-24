package com.sergiovitorino.springbootjwt.infrastructure.security;

import com.sergiovitorino.springbootjwt.domain.model.Authority;
import com.sergiovitorino.springbootjwt.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.List;

@Service
public class TokenAuthenticationService {

    @Value("${jwt.expiration}")
    private long expirationTime;

    public static final String TOKEN_PREFIX = "Bearer";
    public static final String HEADER_STRING = "Authorization";

    private final UserRepository userRepository;
    private final JwtEncoder jwtEncoder;

    public TokenAuthenticationService(UserRepository userRepository, JwtEncoder jwtEncoder) {
        this.userRepository = userRepository;
        this.jwtEncoder = jwtEncoder;
    }

    public void addAuthentication(HttpServletResponse res, String email) {
        final var user = userRepository.findByEmailWithAuthorities(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plusMillis(expirationTime))
                .claim("authorities", extractAuthorities(user.getRole().getAuthorities()))
                .claim("Username", user.getUsername())
                .build();

        var header = JwsHeader.with(MacAlgorithm.HS256).build();
        String jwt = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        res.addHeader(HEADER_STRING, TOKEN_PREFIX + " " + jwt);
    }

    private String extractAuthorities(final List<Authority> authorities) {
        return String.join(",", authorities.stream().map(Authority::getName).toList());
    }
}
