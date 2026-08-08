package br.com.fiap.postech.adapter.input.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import br.com.fiap.postech.adapter.input.api.model.PaginatedUserResponse;
import br.com.fiap.postech.adapter.input.api.model.Role;
import br.com.fiap.postech.adapter.input.api.model.UserData;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.user.enums.Roles;
import br.com.fiap.postech.domain.user.model.User;

@Component
public class Mapper {

    public List<Roles> toDomain(List<Role> roles) {
        if (roles == null) {
            return null;
        }

        List<Roles> rolesDomain = new ArrayList<>();

        for (Role role : roles) {
            rolesDomain.add(toDomain(role));
        }

        return rolesDomain;
    }

    public Roles toDomain(Role role) {
        if (role == null) {
            return null;
        }

        return Roles.valueOf(role.name());
    }

    public UserData toClientResponse(User user) {

        if (user == null) {
            return null;
        }

        UserData response = new UserData();

        response.setId(user.id());
        response.setUsername(user.username());
        response.setRoles(toApiRoles(user.roles()));

        return response;
    }

    private List<Role> toApiRoles(List<Roles> roles) {

        if (roles == null) {
            return null;
        }

        return roles.stream()
                .map(this::toApiRole)
                .toList();
    }

    private Role toApiRole(Roles role) {

        if (role == null) {
            return null;
        }

        return Role.valueOf(role.name());
    }

    public PaginatedUserResponse toPaginatedResponse(ScrollPage<User> page) {
        final var result = new PaginatedUserResponse()
                .pageSize(page.pageSize())
                .cursor(page.cursor())
                .isLast(page.isLast());

        page.data().forEach(item -> result.addDataItem(toClientResponse(item)));

        return result;
    }

}
