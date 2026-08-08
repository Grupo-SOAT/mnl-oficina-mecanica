package br.com.fiap.postech.port.user;

import java.util.Optional;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.user.model.User;
import br.com.fiap.postech.domain.user.model.UserDTO;

public interface UserPort {

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    User createUser(UserDTO userDTO, String senhaDefault);

    void deleteUser(Long id);

    User updateUser(Long id, UserDTO userDTO);

    ScrollPage<User> scroll(String username, Integer pageSize, String cursor);

    void resetUserPassword(Long id);

    String getDefaultPassword();
}
