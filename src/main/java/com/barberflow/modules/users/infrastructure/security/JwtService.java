package com.barberflow.modules.users.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String SECRET_KEY = "TU_LLAVE_SECRETA_SUPER_LARGA_Y_SEGURA_AQUI";

    public String createToken(UserDetails userDetails, Long barbershopId) {
        Map<String, Object> claims = new HashMap<>();
        // Guardamos el rol y el id de la barbería como "etiquetas"
        claims.put("role", userDetails.getAuthorities().toString());
        claims.put("barbershopId", barbershopId);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 8)) // 8 horas
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignInKey() {
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8); // Usamos StandardCharsets.UTF_8 para convertir String en bytes de forma segura
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String jwt) {
        return extractClaim(jwt, Claims::getSubject);
       
    }

    public boolean isTokenValid(String jwt, UserDetails userDetails) {
        final String username = extractUsername(jwt);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(jwt);    

    }

    // Metodos de apoyo.

    private boolean isTokenExpired(String jwt) {
        Date expirationDate = extractClaim(jwt, Claims::getExpiration);
        return expirationDate.before(new Date());
    }   

    private Date extractExpiration(String jwt) {
        return extractClaim(jwt, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllclaims(token);
        return claimsResolver.apply(claims);
    }

    //lee el token usando la llave secreta.
    private Claims extractAllclaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
