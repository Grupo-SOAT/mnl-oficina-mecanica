package br.com.fiap.postech.adapter.output.authentication;

import br.com.fiap.postech.adapter.output.user.persistence.repository.UserRepository;
import br.com.fiap.postech.domain.authentication.exception.InvalidChatbotApiKeyException;
import br.com.fiap.postech.domain.authentication.exception.InvalidPasswordException;
import br.com.fiap.postech.domain.authentication.model.Authentication;
import br.com.fiap.postech.domain.authentication.model.UserChangePassword;
import br.com.fiap.postech.domain.authentication.model.UserLogin;
import br.com.fiap.postech.domain.user.model.User;
import br.com.fiap.postech.port.authentication.AuthenticationPort;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationAdapter implements AuthenticationPort {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${authentication.chatbot.api.key}")
    private String chatBotAuthKey;

    private static final long EXPIRATION_MINUTES = 30;

    @Override
    public Authentication autenticar(User user, UserLogin userLogin) {

        var usuario = userRepository.findById(user.id());

        if (!passwordEncoder.matches(userLogin.password(), usuario.get().getPassword())) {
            throw new InvalidPasswordException();
        }

        Instant now = Instant.now();
        Instant expiration = now.plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES);

        List<String> roles = user.roles().stream()
                .map(Enum::name)
                .toList();

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .subject(user.username())
                .issuer("mechanic-workshop-system")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claim("roles", roles)
                .claim("userId", user.id())
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        return new Authentication(
                token,
                OffsetDateTime.ofInstant(expiration, ZoneOffset.UTC));
    }

    @Override
    @Transactional
    public void mudarSenha(UserChangePassword userChangePassword, User user) {

        var usuario = userRepository.findById(user.id());

        if (!passwordEncoder.matches(userChangePassword.password(), usuario.get().getPassword())) {
            throw new InvalidPasswordException();
        }

        usuario.get().setPassword(passwordEncoder.encode(userChangePassword.newPassword()));
    }

    @Override
    public Authentication autenticarChatBot(String apiKey) {

        if (!apiKey.equals(this.chatBotAuthKey)) {
            throw new InvalidChatbotApiKeyException();
        }

        Instant now = Instant.now();
        Instant expiration = now.plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES);

        List<String> roles = List.of("CHATBOT");

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .subject("chatbot")
                .issuer("mechanic-workshop-system")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .claim("roles", roles)
                .claim("userId", "chatbot")
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        return new Authentication(
                token,
                OffsetDateTime.ofInstant(expiration, ZoneOffset.UTC));
    }
}
