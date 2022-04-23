package com.sergiovitorino.springbootjwt.infrastructure.security;

import com.sergiovitorino.springbootjwt.domain.model.Authority;
import com.sergiovitorino.springbootjwt.domain.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@Service
public class TokenAuthenticationService {

    public static final long EXPIRATIONTIME = 864_000_000; // 10 days
    public static final String SECRET = "fajfasjFAHSKweroiv23";
    public static final String TOKEN_PREFIX = "Bearer";
    public static final String HEADER_STRING = "Authorization";

    @Autowired
    private UserRepository userRepository;

    public void addAuthentication(HttpServletResponse res, String email) {
        final var user = userRepository.findByEmail(email);
        if (user == null)
            throw new IllegalArgumentException("User not found");
        final var claims = Jwts.claims();
        claims.put("authorities", extractAuthorities(user.getRole().getAuthorities()));
        claims.put("Username", user.getUsername());

        final var JWT = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getId().toString())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
                .signWith(SignatureAlgorithm.HS512, SECRET).compact();
        res.addHeader(HEADER_STRING, TOKEN_PREFIX + " " + JWT);
    }

    public Authentication getAuthentication(HttpServletRequest request) {
        try {
            final var token = request.getHeader(HEADER_STRING);
            if (token != null) {
                final var userId = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token.replace(TOKEN_PREFIX, "")).getBody().getSubject();

                if (userId == null)
                    throw new NullPointerException("UserId not found");

                final var authorities = ((String) Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token.replace(TOKEN_PREFIX, "")).getBody().get("authorities"));

                final var authoritiesSplitted = splitAuthorites(authorities);
                final var authorityList = AuthorityUtils.createAuthorityList(authoritiesSplitted);

                return new UsernamePasswordAuthenticationToken(userId, null, authorityList);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private String[] splitAuthorites(String text) {
        if (text == null)
            return new String[0];
        return text.split(",");
    }

    private String extractAuthorities(final List<Authority> authorities) {
        final var stringBuilder = new StringBuilder();
        authorities.stream().forEach(permission -> {
            stringBuilder.append(permission.getName() + ",");
        });
        return removeComma(stringBuilder.toString());
    }

    public String removeComma(final String text) {
        if (text.isEmpty()) {
            return text;
        } else {
            return text.substring(0, text.length() - 1);
        }
    }
}