package com.proiect.SCD.CropHealthAdvisor.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            logger.info("JWT Filter - Request URI: " + request.getRequestURI() + ", Method: " + request.getMethod());
            logger.info("JWT Filter - Authorization header: " + request.getHeader("Authorization"));
            
            if (StringUtils.hasText(jwt)) {
                logger.info("JWT Filter - Token found, length: " + jwt.length());
                if (tokenProvider.validateToken(jwt)) {
                    logger.info("JWT Filter - Token is valid");
                    String username = tokenProvider.getUsernameFromJWT(jwt);
                    logger.info("JWT Filter - Username from token: " + username);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("JWT Filter - Authentication set successfully");
                } else {
                    logger.warn("JWT Filter - JWT token is invalid or expired for request: " + request.getRequestURI());
                }
            } else {
                logger.warn("JWT Filter - No JWT token found in request for: " + request.getRequestURI() + ", Method: " + request.getMethod());
            }
        } catch (Exception ex) {
            logger.error("JWT Filter - Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
