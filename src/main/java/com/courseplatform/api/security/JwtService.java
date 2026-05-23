package com.courseplatform.api.security;

import com.courseplatform.api.model.Role;
import com.courseplatform.api.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Key;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationMs;

    private Key signingKey;

    @PostConstruct
    void init() {
        this.signingKey = createSigningKey(jwtSecret);
    }

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("fullName", user.getFullName());
        claims.put("roles", user.getRoles().stream().map(Role::name).sorted().collect(Collectors.toList()));
        claims.put("role", extractPrimaryRole(user.getRoles().stream().map(Role::name).collect(Collectors.toList())));
        return generateToken(claims, user.getEmail());
    }

    public String generateToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .collect(Collectors.toList());
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("role", extractPrimaryRole(roles));
        return generateToken(claims, userDetails.getUsername());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        Object roleClaim = claims.get("role");
        if (roleClaim instanceof String role && !role.isBlank()) {
            return role;
        }

        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof List<?> roles && !roles.isEmpty()) {
            return String.valueOf(roles.get(0));
        }

        return null;
    }

    public List<String> extractRoles(String token) {
        Object rolesClaim = extractAllClaims(token).get("roles");
        if (rolesClaim instanceof List<?> roles) {
            return roles.stream().map(String::valueOf).collect(Collectors.toList());
        }

        String singleRole = extractRole(token);
        return singleRole == null ? List.of() : List.of(singleRole);
    }

    public Instant extractExpiration(String token) {
        return extractClaim(token, claims -> claims.getExpiration().toInstant());
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).isBefore(Instant.now());
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String generateToken(Map<String, Object> extraClaims, String subject) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtExpirationMs);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Key createSigningKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret must be configured");
        }

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            keyBytes = sha256(secret);
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] sha256(String secret) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to derive JWT signing key", ex);
        }
    }

    private String extractPrimaryRole(Collection<String> roles) {
        return roles.stream()
                .filter(role -> role != null && !role.isBlank())
                .sorted()
                .findFirst()
                .orElse(null);
    }
}