package br.com.fiap.postech.adapter.input.user;

import br.com.fiap.postech.adapter.input.api.model.OneTimePassword;
import br.com.fiap.postech.adapter.input.api.model.PaginatedUserResponse;
import br.com.fiap.postech.adapter.input.api.model.UserData;
import br.com.fiap.postech.adapter.input.api.model.UserRequest;
import br.com.fiap.postech.domain.user.UserUseCase;
import br.com.fiap.postech.domain.user.model.UserDTO;
import br.com.fiap.postech.port.api.UsersApi;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UsersController implements UsersApi {

    private final UserUseCase userUseCase;
    private final Mapper mapper;

    @Override
    public ResponseEntity<UserData> createUser(UserRequest request) {


        var userDomainDTO = new UserDTO(request.getUsername(),
                mapper.toDomain(request.getRoles()));

        var domainResponse = userUseCase.createUser(userDomainDTO);


        var clientResponse = mapper.toClientResponse(domainResponse);

        return ResponseEntity.status(HttpStatus.CREATED).body(clientResponse);
    }

    @Override
    public ResponseEntity<Void> deleteUser(Long id) {


        userUseCase.deleteUser(id);


        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Override
    public ResponseEntity<UserData> getUserById(Long id) {


        var domainResponse = userUseCase.getUserById(id);

        var clientResponse = mapper.toClientResponse(domainResponse);


        return ResponseEntity.ok(clientResponse);

    }

    @Override
    public ResponseEntity<PaginatedUserResponse> listUsers(Long id,
                                                           String username,
                                                           Integer size,
                                                           String cursor
    ) {

        final var pageResult = userUseCase.scroll(username, size, cursor);
        final var responseBody = mapper.toPaginatedResponse(pageResult);

        return ResponseEntity.ok(responseBody);
    }

    @Override
    // reseta para a senha default
    public ResponseEntity<OneTimePassword> resetPassword(Long id) {


        userUseCase.resetUserPassword(id);

        var response = new OneTimePassword("[message]: senha resetada com sucesso. O usuário com id " +
                id + " está utilizando a senha default do sistema."
        );

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<UserData> updateUser(Long id, UserData userData) {


        var userDTO = new UserDTO(userData.getUsername(), mapper.toDomain(userData.getRoles()));

        var domainResponse = userUseCase.updateUser(id, userDTO);


        var clientResponse = mapper.toClientResponse(domainResponse);

        return ResponseEntity.ok(clientResponse);
    }


}
