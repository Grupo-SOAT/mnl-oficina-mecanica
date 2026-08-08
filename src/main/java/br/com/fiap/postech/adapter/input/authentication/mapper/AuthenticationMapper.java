package br.com.fiap.postech.adapter.input.authentication.mapper;

import org.springframework.stereotype.Component;

import br.com.fiap.postech.adapter.input.api.model.LoginRequest;
import br.com.fiap.postech.adapter.input.api.model.TokenResponse;
import br.com.fiap.postech.domain.authentication.model.Authentication;
import br.com.fiap.postech.domain.authentication.model.UserLogin;

@Component
public class AuthenticationMapper {


    public UserLogin toDomain(LoginRequest loginRequest){

        return new UserLogin(loginRequest.getUsername(), loginRequest.getPassword());

    }

    public TokenResponse toClientResponse(Authentication authentication){

        return new TokenResponse(authentication.token(), authentication.offSetDateTime());

    }
    
}
