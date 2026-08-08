package br.com.fiap.postech.it.cucumber.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

public class UsersStepDefinitions extends BaseStepDefinition {
    private Long userId;
    private String lastUpdatedUsername;

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${user.password.default}")
    private String defaultPassword;

    @Before
    public void initialize() {
        initializeFeature("users");
        userId = null;
        lastUpdatedUsername = null;
    }

    @Dado("que o filtro username seja {string}")
    public void setUsernameFilter(String value) {
        context.setFilterName("username");
        context.setFilterValue(value);
    }

    @Dado("que o id do usuario seja {long}")
    public void setUserId(Long id) {
        userId = id;
    }

    @Dado("que o corpo do novo usuario seja:")
    public void setCreateBody(DataTable table) throws Exception {
        setJsonBody(table);
    }

    @Dado("que o corpo de atualização do usuario seja:")
    public void setUpdateBody(DataTable table) throws Exception {
        setJsonBody(table);
    }

    @Quando("eu listar os usuarios")
    public void listUsers() {
        executeRequest("GET", "/users", null, paginationParams());
    }

    @Quando("eu consultar o usuario por id")
    public void getUserById() {
        executeRequest("GET", "/users/" + userId, null, null);
    }

    @Quando("eu criar o usuario")
    public void createUser() {
        executeRequest("POST", "/users", context.getRequestBody(), null);
    }

    @Quando("eu atualizar o usuario")
    public void updateUser() {
        executeRequest("PATCH", "/users/" + userId, context.getRequestBody(), null);
    }

    @Quando("eu remover o usuario")
    public void deleteUser() {
        executeRequest("DELETE", "/users/" + userId, null, null);
    }

    @Quando("eu resetar a senha do usuario")
    public void resetUserPassword() {
        // Antes de resetar, obtém o username do usuário que será atualizado
        lastUpdatedUsername = getUsernameForId(userId);
        executeRequest("POST", "/users/" + userId + "/reset-password", null, null);
    }

    @E("quando eu fizer logout")
    public void logout() {
        context.setAuthToken(null);
        context.setCurrentUser(null);
        context.setCurrentRole(null);
        context.setCurrentPassword(null);
    }

    @E("eu fazer login com username {string} e senha padrão")
    public void loginWithDefaultPassword(String username) {
        String loginPayload = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, defaultPassword);
        executeRequest("POST", "/auth/login", loginPayload, null);
    }

    @Então("a resposta deve conter um token JWT válido")
    public void verifyValidJWT() throws Exception {
        String responseBody = context.getLastResponseBodyAsString();
        assertNotNull(responseBody, "Response body não pode ser nulo");
        
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        String token = extractJsonField(jsonNode, "token");
        assertNotNull(token, "Token ausente na resposta");
        assertFalse(token.isEmpty(), "Token não pode estar vazio");
        
        // Valida se o token é um JWT válido
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
        } catch (Exception e) {
            fail("Token JWT inválido ou expirado: " + e.getMessage());
        }
        
        context.setAuthToken(token);
    }

    @Então("a resposta deve conter no maximo {int} usuarios")
    public void verifyMaxUsers(int maxSize) throws Exception {
        assertMaxDataSize(maxSize, "usuarios");
    }

    @Então("a resposta deve conter ao menos um usuario com username {string}")
    public void verifyUsername(String expectedUsername) throws Exception {
        assertHasItemWithField("username", expectedUsername, "usuario");
    }

    // Helper para obter username a partir do ID (baseado no seed)
    private String getUsernameForId(Long id) {
        return switch (id.intValue()) {
            case 1 -> "admin";
            case 2 -> "mecanico";
            case 3 -> "atendente";
            case 4 -> "almoxarife";
            case 5 -> "chatbot";
            default -> null;
        };
    }
}