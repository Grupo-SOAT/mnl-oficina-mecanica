package br.com.fiap.postech.domain.user.model;

import java.util.List;

import br.com.fiap.postech.domain.user.enums.Roles;

public record User(
        Long id,
        String username,
        List<Roles> roles
) {
}
