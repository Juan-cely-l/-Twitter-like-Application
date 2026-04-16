package edu.eci.co.support;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Arrays;
import java.util.Collection;

public final class TestJwtFactory {

    public static final String NS = "https://twitter-like-app.example.com";
    public static final String SUBJECT = "google-oauth2|105063856870618633482";
    public static final String NAME = "gptazo";
    public static final String NICKNAME = "gptazo7";
    public static final String EMAIL = "gptazo7@gmail.com";

    private TestJwtFactory() {
    }

    public static RequestPostProcessor jwtWithScopes(String... scopes) {
        Collection<GrantedAuthority> authorities = Arrays.stream(scopes)
                .map(scope -> (GrantedAuthority) new SimpleGrantedAuthority("SCOPE_" + scope))
                .toList();

        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt
                        .subject(SUBJECT)
                        .claim(NS + "/name", NAME)
                        .claim(NS + "/nickname", NICKNAME)
                        .claim(NS + "/email", EMAIL))
                .authorities(authorities);
    }
}
