package org.example.client.core.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

public final class JwtUtil {

    private static final String SECRET_KEY = "VGhpcyBpcyBhIHNlY3JldCBrZXkgd2l0aCAyNTYgYml0cyBsZW5ndGghISEhMTIzNDU2Nzg=";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_KEY));
    private JwtUtil() {
        // Приватный конструктор
    }

    public static String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public static String getUserRoleFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            String role = claims.get("role", String.class);

            if (role != null) {
                role = role.startsWith("ROLE_") ? role.substring(5) : role;
                return role.toUpperCase();
            }
            return null;
        } catch (ExpiredJwtException e) {
            System.err.println("Token expired: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.err.println("Invalid token format: " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println("Security violation: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error parsing role: " + e.getMessage());
        }
        return null;
    }

    public static Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.get("user_id", Long.class);
        } catch (ExpiredJwtException e) {
            System.err.println("Token expired: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.err.println("Invalid token format: " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println("Security violation: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error parsing user ID: " + e.getMessage());
        }
        return null;
    }

    public static boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            System.err.println("Token validation error: " + e.getMessage());
            return false;
        }
    }

    private static boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private static Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private static <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseToken(token);
        return claimsResolver.apply(claims);
    }

    private static Claims parseToken(String token) {
        return Jwts.parser()              // <-- Изменения здесь
                .verifyWith(KEY)          // вместо setSigningKey()
                .build()
                .parseSignedClaims(token) // вместо parseClaimsJws()
                .getPayload();            // вместо getBody()
    }
}