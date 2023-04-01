package com.example.mealprep.config;

import com.example.mealprep.dao.MemberJdbcDAO;
import com.example.mealprep.model.Member;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException
    {
     final String authHeader = request.getHeader("Authorization");
     final String jwt;

     // user info to get from jwt token
     final String email;
     final String id;

     if (authHeader == null || !authHeader.startsWith("Bearer ")) {
         filterChain.doFilter(request, response);
         return;
     }

     jwt = authHeader.substring(7);

     email = jwtService.extractEmail(jwt);
     id = jwtService.extractId(jwt);

     if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
         Member user = (Member) this.userDetailsService.loadUserByUsername(email);

         if (jwtService.isTokenValid(jwt, user) == true){

             UsernamePasswordAuthenticationToken authToken =
                     new UsernamePasswordAuthenticationToken(user,
                                                             null,
                             List.of(new SimpleGrantedAuthority("USER"))
                     );
             authToken.setDetails(
                     new WebAuthenticationDetailsSource().buildDetails(request)
             );
             SecurityContextHolder.getContext().setAuthentication(authToken);

         }

     }

    filterChain.doFilter(request, response);

    }
}
