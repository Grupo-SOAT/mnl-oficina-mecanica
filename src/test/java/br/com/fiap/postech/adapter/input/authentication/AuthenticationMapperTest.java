package br.com.fiap.postech.adapter.input.authentication;

import br.com.fiap.postech.adapter.input.api.model.LoginRequest;
import br.com.fiap.postech.adapter.input.api.model.TokenResponse;
import br.com.fiap.postech.adapter.input.authentication.mapper.AuthenticationMapper;
import br.com.fiap.postech.domain.authentication.model.Authentication;
import br.com.fiap.postech.domain.authentication.model.UserLogin;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationMapperTest {

    private final AuthenticationMapper mapper = new AuthenticationMapper();

    @Test
    void shouldConvertLoginRequestToDomain() {
        LoginRequest request = new LoginRequest("andre", "123");

        UserLogin result = mapper.toDomain(request);

        assertEquals("andre", result.username());
        assertEquals("123", result.password());
    }

    @Test
    void shouldConvertAuthenticationToResponse() {
        OffsetDateTime now = OffsetDateTime.now();
        Authentication auth = new Authentication("token123", now);

        TokenResponse result = mapper.toClientResponse(auth);

        assertEquals("token123", result.getToken());
        assertEquals(now, result.getExpiresAt());
    }
}