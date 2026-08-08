package br.com.fiap.postech.domain.authentication;

import java.util.Optional;

import br.com.fiap.postech.domain.authentication.model.Authentication;
import br.com.fiap.postech.domain.authentication.model.UserChangePassword;
import br.com.fiap.postech.domain.authentication.model.UserLogin;
import br.com.fiap.postech.domain.user.exception.UsernameNotFoundException;
import br.com.fiap.postech.domain.user.model.User;
import br.com.fiap.postech.port.authentication.AuthenticationPort;
import br.com.fiap.postech.port.user.UserPort;

public class AuthenticationUseCase {

    private final AuthenticationPort authenticationPort;
    private final UserPort userPort;

    public AuthenticationUseCase(AuthenticationPort authenticationPort, UserPort userPort) {
        this.authenticationPort = authenticationPort;
        this.userPort = userPort;
    }

    public Authentication gerarTokenParaUsuario(UserLogin userLogin){

        Optional<User> usuario = userPort.findByUsername(userLogin.username());

        if (!usuario.isPresent()){

            throw new UsernameNotFoundException("Usuário " + userLogin.username() + " não foi encontrado.");

        }
        
        User user = usuario.get();

        return authenticationPort.autenticar(user, userLogin);


    }

    public void mudarSenhaUsuario(UserChangePassword userChangePassword){

        Optional<User> usuario = userPort.findByUsername(userChangePassword.username());

        if (!usuario.isPresent()){

            throw new UsernameNotFoundException(userChangePassword.username());

        }

        User user = usuario.get();

        authenticationPort.mudarSenha(userChangePassword, user);

    }

    public Authentication autenticarChatBot(String apiKey){

        return authenticationPort.autenticarChatBot(apiKey);

    }
    
}
