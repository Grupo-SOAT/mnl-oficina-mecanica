package br.com.fiap.postech.it.cucumber.steps;

import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.fasterxml.jackson.databind.JsonNode;
import br.com.fiap.postech.config.RoleAuthorizationFilter;
import br.com.fiap.postech.config.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.crypto.SecretKey;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

public class AuthenticationStepDefinitions extends BaseStepDefinition {
    @Autowired
    private WebApplicationContext webContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private RoleAuthorizationFilter roleAuthorizationFilter;

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Before("@authentication")
    public void initialize() {
        context.reset();
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("db/seed/canonical-seed.sql"));
        populator.execute(dataSource);
        setMockMvc(MockMvcBuilders
                .webAppContextSetup(webContext)
                .apply(springSecurity())
                .build());
    }

    // Autenticação com usuário e senha

    @Dado("que eu esteja deslogado")
    public void userIsLoggedOut() {
        context.setAuthToken(null);
        context.setCurrentUser(null);
        context.setCurrentRole(null);
    }

    @Dado("que eu esteja devidamente logado")
    public void userIsLoggedIn() {
        performLoginAs("admin");
    }

    @Quando("informo o usuário {string} e a senha {string}")
    public void submitCredentials(String username, String password) {
        context.setCurrentUser(username);
        context.setCurrentPassword(password);
        performPost("/auth/login",
                String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password),
                null);
    }

    @Então("devo receber uma resposta com status {string}")
    public void verifyStatusCode(String expectedStatus) {
        assertEquals(Integer.parseInt(expectedStatus), context.getLastStatusCode(),
                "Status inesperado. Body: " + context.getLastResponseBodyAsString());
    }

    @E("com um token de sessão válido")
    public void verifyValidSessionToken() {
        assertTrue(context.getLastStatusCode() < 300,
                "Status não é 2xx: " + context.getLastStatusCode());
        String token = extractJsonField(context.getLastResponseBody(), "token");
        assertNotNull(token, "Token ausente. Body: " + context.getLastResponseBodyAsString());
        assertFalse(token.isEmpty(), "Token vazio");
        context.setAuthToken(token);
    }

    @E("devo ser capaz de obter meus dados de usuário com {string} igual a {string}")
    public void verifyUserRoleField(String field, String expectedValue) {
        String token = context.getAuthToken();
        assertNotNull(token, "Token ausente para consultar o usuário autenticado");

        Long userId = extractUserIdFromToken(token);
        performGet("/users/" + userId, token);
        assertEquals(200, context.getLastStatusCode(),
            "GET /users/{id} falhou. Body: " + context.getLastResponseBodyAsString());

        JsonNode root = context.getLastResponseBody();
        assertTrue(root.has(field), "Campo '" + field + "' ausente. Body: " + context.getLastResponseBodyAsString());

        JsonNode actualNode = root.get(field);
        if (actualNode.isArray()) {
            assertTrue(arrayContainsValue(actualNode, expectedValue),
                    "Array '" + field + "' não contém o valor esperado '" + expectedValue + "'. Body: " + context.getLastResponseBodyAsString());
            return;
        }

        String actual = actualNode.isValueNode() ? actualNode.asText() : actualNode.toString();
        assertEquals(expectedValue.toUpperCase(), actual.toUpperCase(),
                "Valor inesperado para '" + field + "'");
    }

    // Credenciais inválidas

    @Então("devo receber um erro com status {string}")
    public void verifyErrorStatus(String expectedStatus) {
        assertEquals(Integer.parseInt(expectedStatus), context.getLastStatusCode(),
                "Status de erro inesperado. Body: " + context.getLastResponseBodyAsString());
    }

    @E("com a mensagem de erro igual a {string}")
    public void verifyErrorMessage(String expectedMessage) {
        String reason = extractJsonField(context.getLastResponseBody(), "reason");
        assertEquals(expectedMessage, reason,
            "Mensagem de erro inesperada. Body: " + context.getLastResponseBodyAsString());
    }

    // Autenticação via API Key (chatbot)

    @Quando("informo a chave de API {string}")
    public void submitApiKey(String apiKey) {
        context.setCurrentApiKey(apiKey);
        try {
            MvcResult result = mockMvc().perform(
                    MockMvcRequestBuilders.post("/auth/chatbot")
                            .header("X-API-Key", apiKey)
                            .contentType(MediaType.APPLICATION_JSON)
                    .content("{}")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            ).andReturn();
            context.setLastStatusCode(result.getResponse().getStatus());
            context.setLastResponseBody(result.getResponse().getContentAsString());
            context.setCurrentUser("chatbot");
            context.setCurrentRole("chatbot");
        } catch (Exception e) {
            context.setLastStatusCode(0);
            context.setLastResponseBody(e.getMessage());
        }
    }

    @E("devo ser capaz de obter o tempo de validade da minha chave de API")
    public void verifyApiKeyExpiration() {
        assertTrue(context.getLastStatusCode() < 300,
                "Status não é 2xx: " + context.getLastStatusCode());
        String expiresAt = extractJsonField(context.getLastResponseBody(), "expiresAt");
        assertNotNull(expiresAt, "expiresAt ausente. Body: " + context.getLastResponseBodyAsString());
        assertTrue(OffsetDateTime.parse(expiresAt).isAfter(OffsetDateTime.now()),
                "API Key já expirada: " + expiresAt);
    }

    // Autorização por role

    @Dado("que eu esteja logado como {string}")
    public void userIsLoggedInAs(String role) {
        performLoginAs(role);
    }

    @Dado("que eu esteja autenticado como {string}")
    public void userIsAuthenticatedAs(String role) {
        if ("chatbot".equals(role)) {
            submitApiKey("chatbot-123");
            context.setAuthToken(extractJsonField(context.getLastResponseBody(), "token"));
        } else {
            performLoginAs(role);
        }
    }

    @Quando("acesso o endpoint {string} {string}")
    public void accessEndpoint(String method, String endpoint) {
        String url = endpoint
                .replace(":serviceId", "1")
                .replace(":id", context.getCatalogServiceId() != null ? context.getCatalogServiceId().toString() : "1");
        
        // Gera payload válido baseado no endpoint e método HTTP
        String body = getValidPayloadForEndpoint(method, endpoint);
        
        try {
            MockHttpServletRequestBuilder request = withRoleAuth(
                    buildRequest(method, url)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body));
            if (hasRoleAuth() && !"GET".equalsIgnoreCase(method)) {
                request.with(SecurityMockMvcRequestPostProcessors.csrf());
            }
            MvcResult result = mockMvc().perform(request).andReturn();
            context.setLastStatusCode(result.getResponse().getStatus());
            context.setLastResponseBody(result.getResponse().getContentAsString());
            context.setLastResponseContentType(result.getResponse().getContentType());
            context.setLastResponseContentDisposition(result.getResponse().getHeader("Content-Disposition"));
        } catch (Exception e) {
            context.setLastStatusCode(0);
            context.setLastResponseBody(e.getMessage());
            context.setLastResponseContentType(null);
            context.setLastResponseContentDisposition(null);
        }
    }
    
    private String getValidPayloadForEndpoint(String method, String endpoint) {
        // Para GETs e DELETEs, payload vazio é aceitável
        if ("GET".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
            return "{}";
        }
        
        // Para POST e PATCH, gera payloads válidos por tipo de recurso
        if (endpoint.contains("/users")) {
            return "{\"username\":\"testuser\",\"roles\":[\"ADMIN\"]}";
        } else if (endpoint.contains("/owners")) {
            return "{\"name\":\"Test Owner\",\"document\":\"12345678901234\",\"email\":\"owner@test.com\",\"phone\":\"11999999999\"}";
        } else if (endpoint.contains("/vehicles")) {
            return "{\"ownerId\":1,\"licensePlate\":\"ABC1234\",\"brand\":\"Toyota\",\"model\":\"Corolla\",\"year\":2020,\"color\":\"Red\"}";
        } else if (endpoint.contains("/catalog/services")) {
            // PATCH /catalog/services/:id requer 'id' no payload
            if ("PATCH".equalsIgnoreCase(method)) {
                return "{\"id\":1,\"name\":\"Test Service\",\"description\":\"Test Description\",\"basePrice\":100.00,\"neededSupplies\":[]}";
            }
            // POST /catalog/services não requer 'id'
            return "{\"name\":\"Test Service\",\"description\":\"Test Description\",\"basePrice\":100.00,\"neededSupplies\":[]}";
        } else if (endpoint.contains("/supplies")) {
            return "{\"name\":\"Test Supply\",\"description\":\"Test Supply\",\"quantity\":10,\"unit\":\"UN\"}";
        } else if (endpoint.contains("/service-orders/:id/services") && endpoint.contains(":serviceId")) {
            return "{\"catalogServiceId\":1,\"price\":100.00,\"neededSupplies\":[]}";
        } else if (endpoint.contains("/service-orders/:id/services") && !endpoint.contains(":serviceId")) {
            return "{\"catalogServiceId\":1,\"price\":100.00,\"neededSupplies\":[]}";
        } else if (endpoint.contains("/service-orders/:id/progress")) {
            return "{\"newStatus\":\"IN_PROGRESS\"}";
        } else if (endpoint.contains("/service-orders/:id/budget")) {
            return "{\"budgetAmount\":1000.00}";
        } else if (endpoint.contains("/service-orders")) {
            return "{\"ownerId\":1,\"vehicleId\":1,\"description\":\"Test Order\"}";
        }
        
        return "{}";
    }

    @Então("devo receber uma resposta com status diferente de {string}")
    public void verifyStatusNotEqual(String forbiddenStatus) {
        assertNotEquals(Integer.parseInt(forbiddenStatus), context.getLastStatusCode(),
                "Endpoint não deveria retornar " + forbiddenStatus
                        + ". Body: " + context.getLastResponseBodyAsString());
    }

    private Long extractUserIdFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Object userId = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("userId");

        assertNotNull(userId, "Claim userId ausente no token");
        if (userId instanceof Number number) {
            return number.longValue();
        }

        return Long.parseLong(userId.toString());
    }

    private boolean arrayContainsValue(JsonNode arrayNode, String expectedValue) {
        for (JsonNode item : arrayNode) {
            String actual = item.isValueNode() ? item.asText() : item.toString();
            if (actual != null && actual.equalsIgnoreCase(expectedValue)) {
                return true;
            }
        }
        return false;
    }
}
