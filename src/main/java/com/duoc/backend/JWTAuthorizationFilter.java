package com.duoc.backend;

import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import static com.duoc.backend.Constants.*;

@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;

    //parsea token
    private Claims setSigningKey(HttpServletRequest request) {
    String jwtToken = request.getHeader(HEADER_AUTHORIZACION_KEY);

        if (jwtToken == null || !jwtToken.startsWith(TOKEN_BEARER_PREFIX)) {
            return null;
        }

        jwtToken = jwtToken.replace(TOKEN_BEARER_PREFIX, "");

        return Jwts.parser()
                .verifyWith((SecretKey) Constants.getSigningKey(secret))
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload();
    }

    //setea SecurityContext
    private void setAuthentication(Claims claims) {

        if (claims == null || claims.get("authorities") == null) {
            SecurityContextHolder.clearContext();
            return;
        }

        List<String> authorities = (List<String>) claims.get("authorities");

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        claims.getSubject(),
                        null,
                        authorities.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList())
                );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    //revisa header
    private boolean isJWTValid(HttpServletRequest request) {
        String header = request.getHeader(HEADER_AUTHORIZACION_KEY);

        return header != null && header.startsWith(TOKEN_BEARER_PREFIX);
    }

    //orquesta todo
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        SecurityContextHolder.clearContext(); 

        try {
            if (isJWTValid(request)) {

                Claims claims = setSigningKey(request);

                if (claims != null && claims.get("authorities") != null) {
                    setAuthentication(claims);
                }
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException e) {
            SecurityContextHolder.clearContext(); 
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        }
    }

    //indica que estos endpoints deberian ser publicos
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.startsWith("/login")
        || path.startsWith("/greetings")
        || path.startsWith("/h2-console")
        || path.startsWith("/swagger-ui")
        || path.startsWith("/v3/api-docs");
    }

}