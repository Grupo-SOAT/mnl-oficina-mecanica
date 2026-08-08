package br.com.fiap.postech.adapter.input.user;

import br.com.fiap.postech.adapter.input.api.model.Role;
import br.com.fiap.postech.adapter.input.api.model.UserData;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.user.enums.Roles;
import br.com.fiap.postech.domain.user.model.User;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MapperTest {

    private final Mapper mapper = new Mapper();

    @Test
    void shouldConvertRoles() {
        var result = mapper.toDomain(List.of(Role.ADMIN));

        assertEquals(List.of(Roles.ADMIN), result);
    }

    @Test
    void shouldReturnNullWhenRolesNull() {
        assertNull(mapper.toDomain((List<Role>) null));
    }

    @Test
    void shouldConvertUserToResponse() {
        User user = new User(1L, "andre", List.of(Roles.ADMIN));

        UserData result = mapper.toClientResponse(user);

        assertEquals(1L, result.getId());
        assertEquals("andre", result.getUsername());
        assertEquals(List.of(Role.ADMIN), result.getRoles());
    }

    @Test
    void shouldReturnNullUser() {
        assertNull(mapper.toClientResponse(null));
    }

    @Test
    void shouldConvertPaginated() {
        ScrollPage<User> page = mock(ScrollPage.class);

        when(page.pageSize()).thenReturn(10);
        when(page.cursor()).thenReturn("abc");
        when(page.isLast()).thenReturn(true);
        when(page.data()).thenReturn(List.of(new User(1L, "andre", List.of(Roles.ADMIN))));

        var result = mapper.toPaginatedResponse(page);

        assertEquals(10, result.getPageSize());
        assertEquals("abc", result.getCursor());
        assertTrue(result.getIsLast());
        assertEquals(1, result.getData().size());
    }
}