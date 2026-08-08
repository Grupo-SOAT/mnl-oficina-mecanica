package br.com.fiap.postech.adapter.input.authentication;

import br.com.fiap.postech.adapter.input.api.model.*;
import br.com.fiap.postech.adapter.input.authentication.mapper.AuthenticationMapper;
import br.com.fiap.postech.domain.authentication.AuthenticationUseCase;
import br.com.fiap.postech.domain.authentication.model.Authentication;
import br.com.fiap.postech.domain.authentication.model.UserLogin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationUseCase useCase;

    @Mock
    private AuthenticationMapper mapper;

    @InjectMocks
    private AuthenticationController controller;

    private Authentication authentication;
    private TokenResponse tokenResponse;

    @BeforeEach
    void setup() {
        authentication = new Authentication("token123", OffsetDateTime.now());

        tokenResponse = new TokenResponse();
        tokenResponse.setToken("token123");
        tokenResponse.setExpiresAt(authentication.offSetDateTime());
    }

    @Test
    void shouldAuthenticateUser() {
        LoginRequest request = new LoginRequest("andre", "123");

        when(mapper.toDomain(request)).thenReturn(new UserLogin("andre", "123"));
        when(useCase.gerarTokenParaUsuario(any())).thenReturn(authentication);
        when(mapper.toClientResponse(authentication)).thenReturn(tokenResponse);

        ResponseEntity<TokenResponse> response = controller.authenticate(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tokenResponse, response.getBody());

        verify(useCase).gerarTokenParaUsuario(any());
    }

    @Test
    void shouldChangePassword() {
        ChangePasswordRequest request =
                new ChangePasswordRequest("new123", "andre", "old123");

        ResponseEntity<Void> response = controller.changePassword(request);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        verify(useCase).mudarSenhaUsuario(any());
    }

    @Test
    void shouldAuthenticateChatBot() {
        when(useCase.autenticarChatBot("api-key"))
                .thenReturn(authentication);

        when(mapper.toClientResponse(authentication))
                .thenReturn(tokenResponse);

        ResponseEntity<TokenResponse> response =
                controller.authenticatedWithApiKey("api-key");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(tokenResponse, response.getBody());
    }
}