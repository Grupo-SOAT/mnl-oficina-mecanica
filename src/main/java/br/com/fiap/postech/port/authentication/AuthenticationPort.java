package br.com.fiap.postech.port.authentication;

import br.com.fiap.postech.domain.authentication.model.Authentication;
import br.com.fiap.postech.domain.authentication.model.UserChangePassword;
import br.com.fiap.postech.domain.authentication.model.UserLogin;
import br.com.fiap.postech.domain.user.model.User;

public interface AuthenticationPort {

    Authentication autenticar(User user, UserLogin userLogin);

    void mudarSenha(UserChangePassword userChangePassword, User user);

    Authentication autenticarChatBot(String apiKey);
    
}
