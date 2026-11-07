package br.com.fiap.postech.adapter.input.authentication;

import br.com.fiap.postech.adapter.input.api.model.ChangePasswordRequest;
import br.com.fiap.postech.adapter.input.api.model.LoginRequest;
import br.com.fiap.postech.adapter.input.api.model.TokenResponse;
import br.com.fiap.postech.adapter.input.authentication.mapper.AuthenticationMapper;
import br.com.fiap.postech.domain.authentication.AuthenticationUseCase;
import br.com.fiap.postech.domain.authentication.model.UserChangePassword;
import br.com.fiap.postech.port.api.AuthenticationApi;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class AuthenticationController implements AuthenticationApi {

    private final AuthenticationUseCase authenticationUseCase;
    private final AuthenticationMapper mapper;

    @Override
    public ResponseEntity<TokenResponse> authenticate(LoginRequest request) {


        var domainResponse = authenticationUseCase.gerarTokenParaUsuario(mapper.toDomain(request));

        var clientResponse = mapper.toClientResponse(domainResponse);


        return ResponseEntity.ok(clientResponse);
    }

    @Override
    public ResponseEntity<Void> changePassword(ChangePasswordRequest request) {


        var userData = new UserChangePassword(request.getNewPassword(), 
        request.getUsername(), request.getPassword());

        authenticationUseCase.mudarSenhaUsuario(userData);


        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @Override
    public ResponseEntity<TokenResponse> authenticatedWithApiKey(String apiKey) {


        var domainResponse = authenticationUseCase.autenticarChatBot(apiKey);


        var clientResponse = mapper.toClientResponse(domainResponse);

        return ResponseEntity.ok(clientResponse);
    }

}
