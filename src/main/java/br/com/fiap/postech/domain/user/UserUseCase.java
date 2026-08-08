package br.com.fiap.postech.domain.user;

import br.com.fiap.postech.adapter.output.persistence.helper.scroll.ScrollPage;
import br.com.fiap.postech.domain.user.exception.NoMatchingUsersException;
import br.com.fiap.postech.domain.user.exception.SameUsernameException;
import br.com.fiap.postech.domain.user.exception.UserNotFoundException;
import br.com.fiap.postech.domain.user.model.User;
import br.com.fiap.postech.domain.user.model.UserDTO;
import br.com.fiap.postech.port.user.UserPort;

public class UserUseCase {

    private final UserPort userPort;

    public UserUseCase(UserPort userPort) {
        this.userPort = userPort;
    }

    public User createUser(UserDTO userDTO) {
        if (userPort.findByUsername(userDTO.username()).isPresent()) {
            throw new SameUsernameException();
        }

        String senhaDefault = userPort.getDefaultPassword();

        return userPort.createUser(userDTO, senhaDefault);
    }

    public void deleteUser(Long id) {
        userPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userPort.deleteUser(id);
    }

    public User getUserById(Long id) {
        return userPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public User updateUser(Long id, UserDTO userDTO) {
        if (userPort.findByUsername(userDTO.username()).isPresent()) {
            throw new SameUsernameException();
        }

        userPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return userPort.updateUser(id, userDTO);
    }

    public ScrollPage<User> scroll(String username, Integer size, String cursor) {
        final var result = userPort.scroll(username, size, cursor);

        if (result.data().isEmpty()) {
            throw new NoMatchingUsersException(username);
        }

        return result;
    }

    public void resetUserPassword(Long id) {
        userPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userPort.resetUserPassword(id);
    }
}
