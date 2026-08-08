package br.com.fiap.postech.adapter.output.user;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.fiap.postech.domain.user.enums.Roles;
import br.com.fiap.postech.domain.user.model.User;
import br.com.fiap.postech.domain.user.model.UserDTO;
import br.com.fiap.postech.port.user.UserPort;
import lombok.RequiredArgsConstructor;


import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.adapter.output.persistence.helper.scroll.Scroller;
import br.com.fiap.postech.adapter.output.user.persistence.entity.UserEntity;
import br.com.fiap.postech.adapter.output.user.persistence.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserAdapter implements UserPort {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${user.password.default}")
    private String defaultPassword;

    @Override
    public String getDefaultPassword() {
        return this.defaultPassword;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public User createUser(UserDTO userDTO, String defaultPassword) {
        var entity = new UserEntity();

        entity.setUsername(userDTO.username());
        entity.setPassword(passwordEncoder.encode(defaultPassword));
        entity.setRolesList(rolesEnumToRolesString(userDTO.roles()));

        var created = userRepository.save(entity);

        return toDomain(created);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserDTO userDTO) {
        final var entity = userRepository.findById(id).get();

        entity.setUsername(userDTO.username());
        entity.setRoles(userDTO.roles().stream().map(Enum::name).toArray(String[]::new));

        final var updated = userRepository.save(entity);

        return toDomain(updated);
    }

    @Override
    public ScrollPage<User> scroll(String username, Integer pageSize, String cursor) {
        return Scroller.scroll(
                cursor,
                pageSize,
                (parsedCursor, pageable) -> {
                    List<UserEntity> results = (username == null || username.isBlank())
                            ? userRepository.findAllAfterCursor(parsedCursor, pageable)
                            : userRepository.findByUsernameAfterCursor(username, parsedCursor, pageable);

                    return results.stream()
                            .map(this::toDomain)
                            .toList();
                }
        );
    }

    @Override
    @Transactional
    public void resetUserPassword(Long id) {
        var entity = userRepository.findById(id).get();
        entity.setPassword(passwordEncoder.encode(this.defaultPassword));
        userRepository.save(entity);
    }

    // =========================
    // MAPPERS
    // =========================

    private User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getUsername(),
                rolesStringToRolesEnum(entity.getRolesList()));
    }

    private List<String> rolesEnumToRolesString(List<Roles> roles) {
        if (roles == null) {
            return null;
        }

        return roles.stream()
                .map(Roles::name)
                .toList();
    }

    private List<Roles> rolesStringToRolesEnum(List<String> roles) {

        if (roles == null) {
            return null;
        }

        return roles.stream()
                .map(Roles::valueOf)
                .toList();
    }

}
