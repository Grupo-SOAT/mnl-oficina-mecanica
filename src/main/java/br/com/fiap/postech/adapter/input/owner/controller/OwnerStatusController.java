package br.com.fiap.postech.adapter.input.owner.controller;

import br.com.fiap.postech.adapter.output.owner.persistence.repository.OwnerRepository;
import br.com.fiap.postech.domain.owner.exception.OwnerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class OwnerStatusController {
    private final OwnerRepository repository;
    public record StatusRequest(Boolean active) {}
    @PatchMapping("/owners/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void updateStatus(@PathVariable Long id, @RequestBody StatusRequest request) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities().stream().noneMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            throw new org.springframework.security.access.AccessDeniedException("ADMIN required");
        }
        if (request.active() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "active obrigatorio");
        var owner = repository.findById(id).orElseThrow(() -> new OwnerNotFoundException(id));
        owner.setActive(request.active());
        repository.save(owner);
    }
}