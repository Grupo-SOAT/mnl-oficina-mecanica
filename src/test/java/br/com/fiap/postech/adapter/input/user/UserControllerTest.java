package br.com.fiap.postech.adapter.input.user;

import br.com.fiap.postech.adapter.input.api.model.*;
import br.com.fiap.postech.domain.user.UserUseCase;
import br.com.fiap.postech.domain.user.enums.Roles;
import br.com.fiap.postech.domain.user.model.User;
import br.com.fiap.postech.domain.user.model.UserDTO;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsersControllerTest {

    @Mock
    private UserUseCase userUseCase;

    @Mock
    private Mapper mapper;

    @InjectMocks
    private UsersController controller;

    private User user;
    private UserData userData;

    @BeforeEach
    void setup() {
        user = new User(1L, "andre", List.of(Roles.ADMIN));

        userData = new UserData();
        userData.setId(1L);
        userData.setUsername("andre");
        userData.setRoles(List.of(Role.ADMIN));
    }

    @Test
    void shouldCreateUser() {
        UserRequest request = new UserRequest();
        request.setUsername("andre");
        request.setRoles(List.of(Role.ADMIN));

        when(mapper.toDomain(request.getRoles())).thenReturn(List.of(Roles.ADMIN));
        when(userUseCase.createUser(any(UserDTO.class))).thenReturn(user);
        when(mapper.toClientResponse(user)).thenReturn(userData);

        ResponseEntity<UserData> response = controller.createUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(userData, response.getBody());

        verify(userUseCase).createUser(any(UserDTO.class));
    }

    @Test
    void shouldDeleteUser() {
        ResponseEntity<Void> response = controller.deleteUser(1L);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(userUseCase).deleteUser(1L);
    }

    @Test
    void shouldGetUserById() {
        when(userUseCase.getUserById(1L)).thenReturn(user);
        when(mapper.toClientResponse(user)).thenReturn(userData);

        ResponseEntity<UserData> response = controller.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userData, response.getBody());
    }

    @Test
    void shouldListUsers() {
        ScrollPage<User> page = mock(ScrollPage.class);
        PaginatedUserResponse paginated = new PaginatedUserResponse();

        when(userUseCase.scroll("andre", 10, "abc")).thenReturn(page);
        when(mapper.toPaginatedResponse(page)).thenReturn(paginated);

        ResponseEntity<PaginatedUserResponse> response =
                controller.listUsers(null, "andre", 10, "abc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(paginated, response.getBody());
    }

    @Test
    void shouldResetPassword() {
        ResponseEntity<OneTimePassword> response = controller.resetPassword(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getPassword().contains("senha resetada"));

        verify(userUseCase).resetUserPassword(1L);
    }

    @Test
    void shouldUpdateUser() {
        when(mapper.toDomain(userData.getRoles())).thenReturn(List.of(Roles.ADMIN));
        when(userUseCase.updateUser(eq(1L), any(UserDTO.class))).thenReturn(user);
        when(mapper.toClientResponse(user)).thenReturn(userData);

        ResponseEntity<UserData> response = controller.updateUser(1L, userData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userData, response.getBody());
    }
}