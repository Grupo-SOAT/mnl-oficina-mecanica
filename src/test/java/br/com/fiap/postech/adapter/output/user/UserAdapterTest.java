package br.com.fiap.postech.adapter.output.user;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.user.persistence.entity.UserEntity;
import br.com.fiap.postech.adapter.output.user.persistence.repository.UserRepository;
import br.com.fiap.postech.domain.user.enums.Roles;
import br.com.fiap.postech.domain.user.model.User;
import br.com.fiap.postech.domain.user.model.UserDTO;

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
class UserAdapterTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserAdapter adapter;

    @BeforeEach
    void setup() throws Exception {
        // setando o @Value manualmente
        Field field = UserAdapter.class.getDeclaredField("defaultPassword");
        field.setAccessible(true);
        field.set(adapter, "123");
    }

    // =========================
    // GET DEFAULT PASSWORD
    // =========================
    @Test
    void shouldReturnDefaultPassword() {
        assertEquals("123", adapter.getDefaultPassword());
    }

    // =========================
    // FIND BY USERNAME
    // =========================
    @Test
    void shouldFindUserByUsername() {
        UserEntity entity = buildEntity();

        when(userRepository.findByUsername("andre"))
                .thenReturn(Optional.of(entity));

        Optional<User> result = adapter.findByUsername("andre");

        assertTrue(result.isPresent());
        assertEquals("andre", result.get().username());
    }

    @Test
    void shouldReturnEmptyWhenUsernameNotFound() {
        when(userRepository.findByUsername("andre"))
                .thenReturn(Optional.empty());

        assertTrue(adapter.findByUsername("andre").isEmpty());
    }

    // =========================
    // FIND BY ID
    // =========================
    @Test
    void shouldFindUserById() {
        UserEntity entity = buildEntity();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(entity));

        Optional<User> result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().id());
    }

    // =========================
    // CREATE USER
    // =========================
    @Test
    void shouldCreateUser() {
        UserDTO dto = new UserDTO("andre", List.of(Roles.ADMIN));

        when(passwordEncoder.encode("123")).thenReturn("encoded");
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> {
                    UserEntity e = invocation.getArgument(0);
                    e.setId(1L);
                    return e;
                });

        User result = adapter.createUser(dto, "123");

        assertEquals("andre", result.username());
        assertEquals(List.of(Roles.ADMIN), result.roles());

        verify(passwordEncoder).encode("123");
        verify(userRepository).save(any(UserEntity.class));
    }

    // =========================
    // DELETE USER
    // =========================
    @Test
    void shouldDeleteUser() {
        adapter.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    // =========================
    // UPDATE USER
    // =========================
    @Test
    void shouldUpdateUser() {
        UserDTO dto = new UserDTO("joao", List.of(Roles.ATTENDANT));
        UserEntity existing = buildEntity();
        UserEntity updated = buildEntity();
        updated.setUsername("joao");
        updated.setRolesList(List.of("ATTENDANT"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity e = invocation.getArgument(0);
            // Simula o que o banco retorna após salvar
            return updated;
        });

        User result = adapter.updateUser(1L, dto);

        assertEquals("joao", result.username());
        assertEquals(List.of(Roles.ATTENDANT), result.roles());
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(UserEntity.class));
    }

    // =========================
    // RESET PASSWORD
    // =========================
    @Test
    void shouldResetPassword() {
        UserEntity entity = buildEntity();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(entity));
        when(userRepository.save(any()))
                .thenReturn(entity);

        when(passwordEncoder.encode("123")).thenReturn("encoded");

        adapter.resetUserPassword(1L);

        assertEquals("encoded", entity.getPassword());
        verify(userRepository).findById(any());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void shouldThrowWhenResetPasswordUserNotFound() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(Exception.class,
                () -> adapter.resetUserPassword(1L));
    }

    // =========================
    // SCROLL (parcial - sem mock do static)
    // =========================
    @Test
    void shouldScrollUsers() {
        UserEntity entity = buildEntity();

        when(userRepository.findAllAfterCursor(anyLong(), any()))
                .thenReturn(List.of(entity));

        ScrollPage<User> result = adapter.scroll(null, 10, "1");

        assertNotNull(result);
        assertFalse(result.data().isEmpty());
        assertEquals("andre", result.data().get(0).username());
    }

    // =========================
    // HELPERS
    // =========================
    private UserEntity buildEntity() {
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setUsername("andre");
        entity.setPassword("123");
        entity.setRolesList(List.of("ADMIN"));
        return entity;
    }
}
