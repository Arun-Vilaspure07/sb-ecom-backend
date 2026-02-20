package com.ecommerce.project.security;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.RoleRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.security.jwt.AuthEntryPointJwt;
import com.ecommerce.project.security.jwt.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {


    private final AuthEntryPointJwt unauthorizedHandler;
    private final AuthTokenFilter authTokenFilter; // ✅ ADD THIS

    @Value("${app.bootstrap.user-password}")
    private String userPassword;

    @Value("${app.bootstrap.seller-password}")
    private String sellerPassword;

    @Value("${app.bootstrap.admin-password}")
    private String adminPassword;

    public WebSecurityConfig(AuthEntryPointJwt unauthorizedHandler,
                             AuthTokenFilter authTokenFilter) { // ✅ ADD
        this.unauthorizedHandler = unauthorizedHandler;
        this.authTokenFilter = authTokenFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // CSRF is disabled because this is a stateless REST API using JWT authentication.
        // No cookies or server-side sessions are used, so CSRF protection is not applicable.
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {
                })
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                                .requestMatchers("/h2-console/**").permitAll()
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                .requestMatchers("/api/seller/**").hasAnyRole("ADMIN", "SELLER")
                                .requestMatchers("/api/public/**").permitAll()
                                .requestMatchers("/actuator/**").permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .anyRequest().authenticated()
                );

        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        http.headers(headers ->
                headers.frameOptions(FrameOptionsConfig::sameOrigin));

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                "/v2/api-docs",
                "/configuration/ui",
                "/swagger-resources/**",
                "/configuration/security",
                "/swagger-ui.html",
                "/webjars/**"
        );
    }

    @Bean
    @Profile("!test")
    @Transactional
    public CommandLineRunner initData(RoleRepository roleRepository,
                                      UserRepository userRepository,
                                      PasswordEncoder passwordEncoder) {

        return args -> {

            Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
                    .orElseGet(() -> roleRepository.save(new Role(AppRole.ROLE_USER)));

            Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
                    .orElseGet(() -> roleRepository.save(new Role(AppRole.ROLE_SELLER)));

            Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(AppRole.ROLE_ADMIN)));

            createUserIfMissing(userRepository, roleRepository, passwordEncoder,
                    "user1", "user1@example.com", userPassword,
                    Set.of(AppRole.ROLE_USER));

            createUserIfMissing(userRepository, roleRepository, passwordEncoder,
                    "seller1", "seller1@example.com", sellerPassword,
                    Set.of(AppRole.ROLE_SELLER));

            createUserIfMissing(userRepository, roleRepository, passwordEncoder,
                    "admin", "admin@example.com", adminPassword,
                    Set.of(AppRole.ROLE_USER, AppRole.ROLE_SELLER, AppRole.ROLE_ADMIN));
        };
    }

    @SuppressWarnings("java:S5411")
    private void createUserIfMissing(UserRepository repo,
                                     RoleRepository roleRepository,
                                     PasswordEncoder encoder,
                                     String username,
                                     String email,
                                     String rawPassword,
                                     Set<AppRole> roleNames) {

        if (repo.existsByUserName(username)) {
            return;
        }

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalStateException(
                    "Bootstrap password missing for user: " + username);
        }

        User user = new User(username, email, encoder.encode(rawPassword));

        Set<Role> managedRoles = roleNames.stream()
                .map(roleName ->
                        roleRepository.findByRoleName(roleName)
                                .orElseThrow(() ->
                                        new IllegalStateException("Role not found: " + roleName)))
                .collect(Collectors.toSet());

        user.setRoles(managedRoles);
        repo.save(user);
    }
}