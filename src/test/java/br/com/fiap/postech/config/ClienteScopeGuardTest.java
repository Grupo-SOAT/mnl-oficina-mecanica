package br.com.fiap.postech.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClienteScopeGuardTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void allowsClienteToAccessOwnResource() {
        authenticateAs(1L, "ROLE_CLIENTE");

        assertDoesNotThrow(() -> ClienteScopeGuard.assertOwnsResource(1L));
    }

    @Test
    void deniesClienteAccessingSomeoneElsesResource() {
        authenticateAs(1L, "ROLE_CLIENTE");

        assertThrows(ClienteResourceAccessDeniedException.class,
                () -> ClienteScopeGuard.assertOwnsResource(2L));
    }

    @Test
    void doesNotRestrictStaffRoles() {
        authenticateAs(null, "ROLE_ADMIN");

        assertDoesNotThrow(() -> ClienteScopeGuard.assertOwnsResource(999L));
    }

    @Test
    void doesNothingWhenUnauthenticated() {
        SecurityContextHolder.clearContext();

        assertDoesNotThrow(() -> ClienteScopeGuard.assertOwnsResource(1L));
    }

    private static void authenticateAs(Long userId, String role) {
        var authentication = new UsernamePasswordAuthenticationToken(
                "someone", userId, List.of(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    @Test
    void deniesMissingIdentityEvenWhenResourceOwnerIsMissing() {
        authenticateAs(null, "ROLE_CLIENTE");
        assertThrows(ClienteResourceAccessDeniedException.class, () -> ClienteScopeGuard.assertOwnsResource(null));
        assertThrows(ClienteResourceAccessDeniedException.class, () -> ClienteScopeGuard.assertOwnsResource(1L));
    }
    @Test
    void deniesMalformedIdentity() {
        var auth = new UsernamePasswordAuthenticationToken("client", "invalid", List.of(new SimpleGrantedAuthority("ROLE_CLIENTE")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        assertThrows(ClienteResourceAccessDeniedException.class, () -> ClienteScopeGuard.assertOwnsResource(1L));
    }}
