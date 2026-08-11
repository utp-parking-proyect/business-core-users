package com.utp.portal.config;

import com.utp.portal.util.constants.Constants;
import com.utp.portal.util.security.ApiExceptionSecurityHandler;
import com.utp.portal.util.security.UserReadAuthorizationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

@EnableWebFluxSecurity
@Configuration
public class SecurityConfig {

  @Bean
  SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                UserReadAuthorizationManager userReadAuthorizationManager,
                                                ApiExceptionSecurityHandler apiExceptionSecurityHandler) {
    return http
        .authorizeExchange(auth -> auth
            .pathMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/openapi/**").permitAll()
            .pathMatchers(HttpMethod.GET, "/users/username/{username}")
            .hasAuthority(Constants.ROLE_INTERNAL_SERVICE)
            .pathMatchers(HttpMethod.GET, "/users/me").authenticated()
            .pathMatchers(HttpMethod.GET, "/users/{id}").access(userReadAuthorizationManager)
            .anyExchange().authenticated())
        .csrf(ServerHttpSecurity.CsrfSpec::disable)
        .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
        .exceptionHandling(exception -> exception
            .authenticationEntryPoint(apiExceptionSecurityHandler)
            .accessDeniedHandler(apiExceptionSecurityHandler))
        .oauth2ResourceServer(oauth2 -> oauth2
            .authenticationEntryPoint(apiExceptionSecurityHandler)
            .accessDeniedHandler(apiExceptionSecurityHandler)
            .jwt(jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor())))
        .build();
  }

  private Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
    return jwt -> {
      List<String> roles = jwt.getClaimAsStringList(Constants.CLAIM_ROLES);
      Collection<SimpleGrantedAuthority> authorities = roles == null
          ? List.of()
          : roles.stream().map(SimpleGrantedAuthority::new).toList();
      return Mono.just(new JwtAuthenticationToken(jwt, authorities));
    };
  }
}
