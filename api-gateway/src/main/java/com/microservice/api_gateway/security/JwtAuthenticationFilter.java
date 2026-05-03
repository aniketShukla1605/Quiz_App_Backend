package com.microservice.api_gateway.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    private final JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (path.startsWith("/auth") || path.startsWith("/eureka")) {
            return chain.filter(exchange);
        }
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst("accessToken");
        if (cookie == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        try {
            Claims claims = jwtService.extractClaims(cookie.getValue());

            String role = claims.get("role", String.class);

            if (path.startsWith("/quiz/create") || path.startsWith("/quiz/createCustom") && !role.equals("TEACHER")) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            if (path.startsWith("/question/add") && !role.equals("TEACHER")) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            if (path.matches("/quiz/\\d+/submit") && !role.equals("STUDENT")) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            if (path.startsWith("/auth") || path.startsWith("/eureka") || path.startsWith("/profile/internal")) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            if ((path.matches("/quiz/\\d+/start") || path.matches("/quiz/\\d+/sync"))
                    && !role.equals("STUDENT")) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            ServerWebExchange modified = exchange.mutate()
                    .request(r -> r.headers(h -> {
                        h.add("X-User-Id", claims.getSubject());
                        h.add("X-User-Email", claims.get("email", String.class));
                        h.add("X-User-Role", claims.get("role", String.class));
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
