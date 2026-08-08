package br.com.fiap.postech.domain.authentication;

import br.com.fiap.postech.domain.authentication.model.*;
import br.com.fiap.postech.domain.user.exception.UsernameNotFoundException;
import br.com.fiap.postech.domain.user.enums.Roles;
import br.com.fiap.postech.domain.user.model.User;
import br.com.fiap.postech.port.authentication.AuthenticationPort;
import br.com.fiap.postech.port.user.UserPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationUseCaseTest {

    @Mock
    private AuthenticationPort authenticationPort;

    @Mock
    private UserPort userPort;

    private AuthenticationUseCase useCase;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        useCase = new AuthenticationUseCase(authenticationPort, userPort);
    }

    @Test
    void shouldGenerateToken() {
        UserLogin login = new UserLogin("andre", "123");
        User user = new User(1L, "andre", List.of(Roles.ADMIN));
        Authentication auth = new Authentication("token", OffsetDateTime.now());

        when(userPort.findByUsername("andre"))
                .thenReturn(Optional.of(user));

        when(authenticationPort.autenticar(user, login))
                .thenReturn(auth);

        Authentication result = useCase.gerarTokenParaUsuario(login);

        assertEquals(auth, result);
    }

    @Test
    void shouldThrowWhenUserNotFoundOnLogin() {
        UserLogin login = new UserLogin("andre", "123");

        when(userPort.findByUsername("andre"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> useCase.gerarTokenParaUsuario(login));
    }

    @Test
    void shouldChangePassword() {
        UserChangePassword change =
                new UserChangePassword("new", "andre", "old");

        User user = new User(1L, "andre", List.of(Roles.ADMIN));

        when(userPort.findByUsername("andre"))
                .thenReturn(Optional.of(user));

        useCase.mudarSenhaUsuario(change);

        verify(authenticationPort).mudarSenha(change, user);
    }

    @Test
    void shouldThrowWhenUserNotFoundOnChangePassword() {
        UserChangePassword change =
                new UserChangePassword("new", "andre", "old");

        when(userPort.findByUsername("andre"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> useCase.mudarSenhaUsuario(change));
    }

    @Test
    void shouldAuthenticateChatBot() {
        Authentication auth = new Authentication("token", OffsetDateTime.now());

        when(authenticationPort.autenticarChatBot("key"))
                .thenReturn(auth);

        Authentication result = useCase.autenticarChatBot("key");

        assertEquals(auth, result);
    }
}