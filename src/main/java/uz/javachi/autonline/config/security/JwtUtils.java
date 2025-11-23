package uz.javachi.autonline.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtils {

    @Value("${app.jwt.secret:mySecretKey}")
    private String jwtSecret;

    @Value("${app.jwt.expiration:86400000}")
    private int jwtExpirationMs;

    @Value("${app.jwt.refresh.expiration:604800000}")
    private int REFRESH_EXPIRATION_TIME = 604800000;

    private static final String REFRESH_CLAIM = "isRefresh";
    private static final int MAX_CACHE_SIZE = 1000;
    private static final long CACHE_TTL_MS = 60000; // 1 minute cache

    private SecretKey signingKey;
    private final Map<String, CachedClaims> claimsCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            log.warn("JWT secret key is too short or null. Using default key.");
            jwtSecret = "mySecretKeyThatIsAtLeast32CharactersLongForSecurity";
        }
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        log.info("JWT signing key initialized successfully");
    }

    private SecretKey getSigningKey() {
        if (signingKey == null) {
            init();
        }
        return signingKey;
    }

    private static class CachedClaims {
        final Claims claims;
        final long timestamp;

        CachedClaims(Claims claims) {
            this.claims = claims;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractSessionId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("sessionId", String.class);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // Check cache first
        CachedClaims cached = claimsCache.get(token);
        if (cached != null && !cached.isExpired()) {
            return cached.claims;
        }

        // Parse token
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Cache the claims (with size limit)
        if (claimsCache.size() >= MAX_CACHE_SIZE) {
            // Remove expired entries
            claimsCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
            // If still full, clear oldest entries
            if (claimsCache.size() >= MAX_CACHE_SIZE) {
                claimsCache.clear();
            }
        }
        claimsCache.put(token, new CachedClaims(claims));

        return claims;
    }

    private Boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration != null && expiration.before(new Date());
    }

    public String generateToken(UserDetails userDetails, Map<String, Object> claims) {
        return createToken(claims, userDetails.getUsername());
    }

    public String generateToken(UserDetails userDetails, List<String> roles, List<String> permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("permissions", permissions);
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtExpirationMs);
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer("https://javachi.uz")
                .issuedAt(now)
                .expiration(exp)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(@NonNull String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_EXPIRATION_TIME);
        return Jwts.builder()
                .subject(subject)
                .expiration(expiryDate)
                .issuedAt(now)
                .issuer("https://javachi.uz")
                .claim(REFRESH_CLAIM, true)
                .signWith(getSigningKey())
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public Boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT token is expired: {}", e.getMessage());
            // Remove from cache if expired
            claimsCache.remove(token);
        } catch (MalformedJwtException e) {
            log.debug("Invalid JWT token format: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.debug("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.debug("JWT claims string is empty: {}", e.getMessage());
        } catch (JwtException e) {
            log.debug("JWT validation error: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during JWT validation: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Validates if the token is a refresh token
     * @param token the token to validate
     * @return true if the token is a valid refresh token
     */
    public Boolean isRefreshToken(String token) {
        if (!validateToken(token)) {
            return false;
        }
        try {
            Claims claims = extractAllClaims(token);
            Boolean isRefresh = claims.get(REFRESH_CLAIM, Boolean.class);
            return Boolean.TRUE.equals(isRefresh);
        } catch (Exception e) {
            log.debug("Error checking refresh token claim: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validates if the token is an access token (not a refresh token)
     * @param token the token to validate
     * @return true if the token is a valid access token
     */
    public Boolean isAccessToken(String token) {
        if (!validateToken(token)) {
            return false;
        }
        try {
            Claims claims = extractAllClaims(token);
            Boolean isRefresh = claims.get(REFRESH_CLAIM, Boolean.class);
            return !Boolean.TRUE.equals(isRefresh);
        } catch (Exception e) {
            log.debug("Error checking access token claim: {}", e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object rolesObj = claims.get("roles");
            if (rolesObj instanceof List) {
                return (List<String>) rolesObj;
            }
            return List.of();
        } catch (Exception e) {
            log.debug("Error extracting roles from token: {}", e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object permissionsObj = claims.get("permissions");
            if (permissionsObj instanceof List) {
                return (List<String>) permissionsObj;
            }
            return List.of();
        } catch (Exception e) {
            log.debug("Error extracting permissions from token: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Clears the claims cache (useful for testing or memory management)
     */
    public void clearCache() {
        claimsCache.clear();
    }
}
