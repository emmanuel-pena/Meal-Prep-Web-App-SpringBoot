package com.example.mealprep.config;

import com.example.mealprep.model.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String secret = "743777217A25432A462D4A614E635266556A586E3272357538782F413F442847";

    public String generateToken(Member member) {
       return generateToken(new HashMap<>(), member);
    }

    public String generateToken(Map<String, Object> extraClaims, Member member) {
        return Jwts.builder().setClaims(extraClaims)
                .setSubject(member.getEmail())
                .setId(member.getId())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 2))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
       final Claims claims = extractAllClaims(token);
       return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token){
      return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
    }

    private Key getSignInKey() {
     byte[] keyBytes = Decoders.BASE64.decode(secret);

     return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean isTokenValid(String token, Member member){
        final String email = extractEmail(token);
        return ( email.matches(member.getEmail()) && !isTokenExpired(token) );
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    public String extractEmail (String token){
       return extractClaim(token, Claims::getSubject);
    }

    public String extractId (String token){
        return extractClaim(token, Claims::getId);
    }

}
