package com.tynyany.simplewmsv2.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyAuthenticationFilterTest {

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validKeyAuthenticatesAsApi() throws Exception {
        AtomicReference<Authentication> seen = new AtomicReference<>();
        MockHttpServletRequest request = request("secret");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new ApiKeyAuthenticationFilter("secret").doFilter(request, response,
                (req, res) -> seen.set(SecurityContextHolder.getContext().getAuthentication()));

        assertThat(seen.get()).isNotNull();
        assertThat(seen.get().getAuthorities()).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_API");
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void wrongKeyIsRejected() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<Boolean> passed = new AtomicReference<>(false);

        new ApiKeyAuthenticationFilter("secret").doFilter(request("bad"), response, (req, res) -> passed.set(true));

        assertThat(passed.get()).isFalse();
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void anyKeyIsRejectedWhenKeyIsNotConfigured() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        new ApiKeyAuthenticationFilter("").doFilter(request(""), response, (req, res) -> { });
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    void requestWithoutKeyPassesThrough() throws Exception {
        AtomicReference<Boolean> passed = new AtomicReference<>(false);
        new ApiKeyAuthenticationFilter("secret").doFilter(new MockHttpServletRequest("POST", "/api/new_receiving"),
                new MockHttpServletResponse(), (req, res) -> passed.set(true));
        assertThat(passed.get()).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    private static MockHttpServletRequest request(String key) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/new_receiving");
        request.addHeader(ApiKeyAuthenticationFilter.HEADER, key);
        return request;
    }
}
