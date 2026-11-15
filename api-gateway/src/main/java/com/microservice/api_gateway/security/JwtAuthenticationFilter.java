package com.microservice.api_gateway.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS
                || path.startsWith("/auth")
                || path.startsWith("/eureka")) {
            return chain.filter(exchange);
        }

        // Block external access to internal microservice endpoints
        if (path.contains("/internal/")) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        String token = null;
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst("accessToken");
        if (cookie != null && StringUtils.hasText(cookie.getValue())) {
            token = cookie.getValue();
        } else {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7).trim();
            }
        }

        if (!StringUtils.hasText(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            Claims claims = jwtService.extractClaims(token);

            String tokenType = claims.get("type", String.class);
            if (tokenType != null && !"access".equalsIgnoreCase(tokenType)) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String role = claims.get("role", String.class);

            if ((path.startsWith("/quiz/create") || path.startsWith("/quiz/createCustom")) && !"TEACHER".equals(role)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            if ((path.startsWith("/question/add") || path.startsWith("/question/createQuizWithQuestions")) && !"TEACHER".equals(role)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            if (path.matches("^/quiz/\\d+/submit/?$") && !"STUDENT".equals(role)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            if ((path.matches("^/quiz/\\d+/start/?$") || path.matches("^/quiz/\\d+/sync/?$"))
                    && !"STUDENT".equals(role)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            ServerWebExchange modified = exchange.mutate()
                    .request(r -> r.headers(h -> {
                        h.set("X-User-Id", claims.getSubject());
                        if (claims.get("email", String.class) != null) {
                            h.set("X-User-Email", claims.get("email", String.class));
                        }
                        if (role != null) {
                            h.set("X-User-Role", role);
                        }
                    }))
                    .build();

            return chain.filter(modified);
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
    @Override
    public int getOrder() {
        return -1;
    }
}
