package com.proiect.SCD.CropHealthAdvisor.jwt;

import com.proiect.SCD.CropHealthAdvisor.jwt.JwtAuthenticationEntryPoint;
import com.proiect.SCD.CropHealthAdvisor.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder; // Folosit temporar
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    // ATENTIE: Folosim NoOpPasswordEncoder DOAR pentru testare FARA criptare.
    // In productie, acesta trebuie inlocuit cu BCryptPasswordEncoder!
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().and() // Permite configurarea CORS din CorsConfig.java
            .csrf().disable() // Dezactiveaza CSRF (Token-ul JWT il inlocuieste)
            
            // Gestioneaza erorile de autentificare (ex: token lipsa)
            .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and() 
            
            // Nu folosim sesiuni, deoarece folosim JWT
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and() 
            
            .authorizeHttpRequests(authorize -> authorize
                // Permite accesul public la inregistrare si login
                .requestMatchers("/api/auth/**").permitAll() 
                .requestMatchers("/api/health").permitAll()
                
                // Toate celelalte cereri (locations, reports) necesita autentificare
                .anyRequest().authenticated() 
            );

        // Adauga filtrul nostru JWT inainte de filtrul standard de user/parola
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
