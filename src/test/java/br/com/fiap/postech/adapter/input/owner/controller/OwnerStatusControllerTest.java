package br.com.fiap.postech.adapter.input.owner.controller;
import br.com.fiap.postech.adapter.output.owner.persistence.repository.OwnerRepository;
import br.com.fiap.postech.adapter.output.owner.persistence.entity.OwnerEntity;
import org.junit.jupiter.api.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class OwnerStatusControllerTest {
    final OwnerRepository repository = mock(OwnerRepository.class);
    final OwnerStatusController controller = new OwnerStatusController(repository);
    void auth(String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", null, List.of(new SimpleGrantedAuthority(role))));
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    @Test void adminCanDisableAndReactivate() {
        auth("ROLE_ADMIN");
        var owner = OwnerEntity.builder().id(1L).build();
        when(repository.findById(1L)).thenReturn(Optional.of(owner));
        controller.updateStatus(1L, new OwnerStatusController.StatusRequest(false));
        assertFalse(owner.isActive());
        controller.updateStatus(1L, new OwnerStatusController.StatusRequest(true));
        assertTrue(owner.isActive());
        verify(repository, times(2)).save(owner);
    }
    @Test void clientCannotChangeStatus() {
        auth("ROLE_CLIENTE");
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> controller.updateStatus(1L, new OwnerStatusController.StatusRequest(true)));
        verifyNoInteractions(repository);
    }
    @Test void missingStatusRejected() {
        auth("ROLE_ADMIN");
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.updateStatus(1L, new OwnerStatusController.StatusRequest(null)));
        verifyNoInteractions(repository);
    }
}