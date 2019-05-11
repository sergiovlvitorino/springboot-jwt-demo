package com.sergiovitorino.springbootjwt.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Service;
import com.sergiovitorino.springbootjwt.domain.model.Authority;
import com.sergiovitorino.springbootjwt.domain.model.User;
import com.sergiovitorino.springbootjwt.domain.repository.UserRepository;

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

	@Autowired private UserRepository userRepository;

	public void addAuthentication(HttpServletResponse res, String email) {
		final User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
		final Claims claims = Jwts.claims();
		claims.put("authorities", extractAuthorities(user.getRole().getAuthorities()));
		claims.put("Username", user.getUsername());

		final String JWT = Jwts.builder()
				.setClaims(claims)
				.setSubject(user.getId().toString())
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
				.signWith(SignatureAlgorithm.HS512, SECRET).compact();
		res.addHeader(HEADER_STRING, TOKEN_PREFIX + " " + JWT);
	}

	public Authentication getAuthentication(HttpServletRequest request) {
		try {
			final String token = request.getHeader(HEADER_STRING);
			if (token != null) {
				final String userId = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token.replace(TOKEN_PREFIX, "")).getBody().getSubject();
				final String[] authorities = ((String) Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token.replace(TOKEN_PREFIX, "")).getBody().get("authorities")).split(",");
				final List<GrantedAuthority> authorityList = AuthorityUtils.createAuthorityList(authorities);
				return userId != null ? new UsernamePasswordAuthenticationToken(userId, null, authorityList) : null;
			}
		}catch(Exception e) {
			return null;
		}
		return null;
	}

	private String extractAuthorities(final List<Authority> authorities){
		final StringBuilder stringBuilder = new StringBuilder();
		authorities.stream().forEach(permission -> {
			stringBuilder.append(permission.getName());
			stringBuilder.append(",");
		});
		return removeComma(stringBuilder.toString());
	}

	private String removeComma(final String text){
		if (text.isEmpty()){
			return text;
		}else{
			return text.substring(0, text.length()-1);
		}
	}
}