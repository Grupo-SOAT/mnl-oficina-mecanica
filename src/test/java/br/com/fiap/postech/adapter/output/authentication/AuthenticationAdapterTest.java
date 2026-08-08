package br.com.fiap.postech.adapter.output.authentication;

import br.com.fiap.postech.adapter.output.user.persistence.entity.UserEntity;
import br.com.fiap.postech.adapter.output.user.persistence.repository.UserRepository;
import br.com.fiap.postech.domain.authentication.exception.InvalidChatbotApiKeyException;
import br.com.fiap.postech.domain.authentication.exception.InvalidPasswordException;
import br.com.fiap.postech.domain.authentication.model.Authentication;
import br.com.fiap.postech.domain.authentication.model.UserChangePassword;
import br.com.fiap.postech.domain.authentication.model.UserLogin;
import br.com.fiap.postech.domain.user.enums.Roles;
import br.com.fiap.postech.domain.user.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationAdapterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationAdapter adapter;

    private User user;
    private UserEntity entity;

    @BeforeEach
    void setup() throws Exception {
        user = new User(1L, "andre", List.of(Roles.ADMIN));

        entity = new UserEntity();
        entity.setId(1L);
        entity.setUsername("andre");
        entity.setPassword("encoded");

        // setando @Value
        setField("jwtSecret", "my-super-secret-key-my-super-secret-key");
        setField("chatBotAuthKey", "valid-api-key");
    }

    private void setField(String name, Object value) throws Exception {
        Field field = AuthenticationAdapter.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(adapter, value);
    }

    // =========================
    // AUTENTICAR USUÁRIO
    // =========================
    @Test
    void shouldAuthenticateUserSuccessfully() {
        UserLogin login = new UserLogin("andre", "123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("123", "encoded")).thenReturn(true);

        Authentication result = adapter.autenticar(user, login);

        assertNotNull(result);
        assertNotNull(result.token());
        assertNotNull(result.offSetDateTime());

        verify(passwordEncoder).matches("123", "encoded");
    }

    @Test
    void shouldThrowWhenPasswordIsInvalid() {
        UserLogin login = new UserLogin("andre", "wrong");

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(InvalidPasswordException.class,
                () -> adapter.autenticar(user, login));
    }

    // =========================
    // MUDAR SENHA
    // =========================
    @Test
    void shouldChangePasswordSuccessfully() {
        UserChangePassword change =
                new UserChangePassword("new123", "andre", "old123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("old123", "encoded")).thenReturn(true);
        when(passwordEncoder.encode("new123")).thenReturn("newEncoded");

        adapter.mudarSenha(change, user);

        assertEquals("newEncoded", entity.getPassword());

        verify(passwordEncoder).encode("new123");
    }

    @Test
    void shouldThrowWhenOldPasswordInvalid() {
        UserChangePassword change =
                new UserChangePassword("new123", "andre", "wrong");

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(InvalidPasswordException.class,
                () -> adapter.mudarSenha(change, user));
    }

    // =========================
    // AUTENTICAR CHATBOT
    // =========================
    @Test
    void shouldAuthenticateChatBotSuccessfully() {
        Authentication result = adapter.autenticarChatBot("valid-api-key");

        assertNotNull(result);
        assertNotNull(result.token());
        assertNotNull(result.offSetDateTime());
    }

    @Test
    void shouldThrowWhenApiKeyInvalid() {
        assertThrows(InvalidChatbotApiKeyException.class,
                () -> adapter.autenticarChatBot("invalid-key"));
    }
}