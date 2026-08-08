package br.com.fiap.postech.it.cucumber.steps;

import br.com.fiap.postech.it.cucumber.context.CucumberTestContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

public abstract class BaseStepDefinition {
    @Autowired
    protected WebApplicationContext webContext;

    @Autowired
    protected DataSource dataSource;

    protected final CucumberTestContext context;
    protected final ObjectMapper objectMapper = new ObjectMapper();

    public BaseStepDefinition() {
        this.context = CucumberTestContext.getInstance();
    }

    protected MockMvc mockMvc() {
        return context.getMockMvc();
    }

    protected void setMockMvc(MockMvc mockMvc) {
        context.setMockMvc(mockMvc);
    }

    protected void initializeFeature() {
        initializeFeature(new String[0]);
    }

    protected void initializeFeature(String... domains) {
        context.reset();
        if (domains != null) {
            for (String domain : domains) {
                runDomainSeed(domain);
            }
        }
        setMockMvc(MockMvcBuilders
                .webAppContextSetup(webContext)
                .apply(springSecurity())
                .build());
    }

    protected void runDomainSeed(String domain) {
        if (domain == null || domain.isBlank()) {
            return;
        }
        String seedPath = "db/" + domain + "-seed.sql";
        ClassPathResource seedResource = new ClassPathResource(seedPath);
        if (!seedResource.exists()) {
            return;
        }

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(seedResource);
        populator.execute(dataSource);
    }

    protected Map<String, String> paginationParams() {
        Map<String, String> params = new LinkedHashMap<>();
        if (context.getPageSize() != null) {
            params.put("pageSize", String.valueOf(context.getPageSize()));
        }
        if (context.getCursor() != null) {
            params.put("cursor", context.getCursor());
        }
        if (context.getFilterName() != null && context.getFilterValue() != null) {
            params.put(context.getFilterName(), context.getFilterValue());
        }
        return params;
    }

    protected void executeRequest(String method, String url, String body, Map<String, String> params) {
        try {
            MockHttpServletRequestBuilder request = buildRequest(method, url)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON);

            if (body != null) {
                request.content(body);
            }
            if (params != null) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    request.param(entry.getKey(), entry.getValue());
                }
            }

            request = withRoleAuth(request);
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

    protected String dataTableToJson(DataTable table) throws Exception {
        Map<String, String> row = table.asMaps(String.class, String.class).get(0);
        Map<String, Object> normalized = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : row.entrySet()) {
            String value = entry.getValue();
            if (value == null || value.isBlank()) {
                normalized.put(entry.getKey(), null);
                continue;
            }
            if (value.matches("^-?\\d+$")) {
                normalized.put(entry.getKey(), Long.parseLong(value));
            } else if (value.matches("^-?\\d+\\.\\d+$")) {
                normalized.put(entry.getKey(), Double.parseDouble(value));
            } else if ((value.startsWith("[") && value.endsWith("]")) || (value.startsWith("{") && value.endsWith("}"))) {
                normalized.put(entry.getKey(), objectMapper.readTree(value));
            } else {
                normalized.put(entry.getKey(), value);
            }
        }

        return objectMapper.writeValueAsString(normalized);
    }

    protected void setJsonBody(DataTable table) throws Exception {
        context.setRequestBody(dataTableToJson(table));
    }

    protected void assertDataArray() {
        JsonNode root = context.getLastResponseBody();
        assertTrue(root.has("data"), "Campo data ausente");
        assertTrue(root.get("data").isArray(), "Campo data nao eh array");
    }

    protected void assertMaxDataSize(int maxSize, String resourceName) {
        JsonNode root = context.getLastResponseBody();
        assertTrue(root.has("data"), "Campo data ausente");
        assertTrue(root.get("data").isArray(), "Campo data nao eh array");
        assertTrue(root.get("data").size() <= maxSize,
                "Quantidade de " + resourceName + " acima do esperado: " + root.get("data").size());
    }

    protected void assertHasItemWithField(String field, String expectedValue, String resourceName) {
        JsonNode root = context.getLastResponseBody();
        assertTrue(root.has("data"), "Campo data ausente");
        assertTrue(root.get("data").isArray(), "Campo data nao eh array");

        boolean found = false;
        for (JsonNode item : root.get("data")) {
            if (item.has(field) && expectedValue.equals(item.get(field).asText())) {
                found = true;
                break;
            }
        }

        assertTrue(found, "Nenhum " + resourceName + " com " + field + " encontrado: " + expectedValue);
    }

    protected void assertNoItemWithField(String field, String unexpectedValue, String resourceName) {
        JsonNode root = context.getLastResponseBody();
        assertTrue(root.has("data"), "Campo data ausente");
        assertTrue(root.get("data").isArray(), "Campo data nao eh array");

        for (JsonNode item : root.get("data")) {
            if (item.has(field) && unexpectedValue.equals(item.get(field).asText())) {
                fail(resourceName + " com " + field + "=" + unexpectedValue + " nao deveria estar presente na listagem");
            }
        }
    }

    protected void assertDataIdsInOrder(String expectedIds, String resourceName) {
        JsonNode root = context.getLastResponseBody();
        assertTrue(root.has("data"), "Campo data ausente");
        assertTrue(root.get("data").isArray(), "Campo data nao eh array");

        List<Long> expected = Arrays.stream(expectedIds.split(","))
                .map(String::trim)
                .map(Long::parseLong)
                .toList();

        List<Long> actual = new ArrayList<>();
        for (JsonNode item : root.get("data")) {
            actual.add(item.get("id").asLong());
        }

        assertEquals(expected, actual, "Ordem de " + resourceName + " divergente da esperada");
    }

    protected void assertFirstItemIdGreaterThan(long minId, String resourceName) {
        JsonNode root = context.getLastResponseBody();
        assertTrue(root.has("data"), "Campo data ausente");
        assertTrue(root.get("data").isArray(), "Campo data nao eh array");
        assertFalse(root.get("data").isEmpty(), "Lista de " + resourceName + " vazia");
        assertTrue(root.get("data").get(0).has("id"), "Primeiro item sem id");
        assertTrue(root.get("data").get(0).get("id").asLong() > minId,
                "Id do primeiro item nao respeita cursor");
    }

    protected void assertResponseMatchesRequest() throws Exception {
        JsonNode request = objectMapper.readTree(context.getRequestBody());
        JsonNode response = context.getLastResponseBody();

        for (String fieldName : iterable(request.fieldNames())) {
            JsonNode expectedValue = request.get(fieldName);
            assertTrue(response.has(fieldName), "Campo ausente na resposta: " + fieldName);
            assertEquals(expectedValue.asText(), response.get(fieldName).asText(),
                    "Valor divergente para campo: " + fieldName);
        }
    }

    protected void performLoginAs(String role) {
        String username = role.toLowerCase();
        context.setCurrentUser(username);
        context.setCurrentRole(role.toLowerCase());
        context.setCurrentPassword(username);
        context.setCurrentApiKey(null);
        context.setAuthToken(null);
        context.setLastStatusCode(200);
        context.setLastResponseBody("{}");
    }

    protected MockHttpServletRequestBuilder withRoleAuth(MockHttpServletRequestBuilder request) {
        if (context.getCurrentRole() == null || context.getCurrentRole().isBlank()) {
            return request;
        }

        String username = context.getCurrentUser() != null ? context.getCurrentUser() : context.getCurrentRole();
        return request.with(SecurityMockMvcRequestPostProcessors.user(username).roles(toSpringRole(context.getCurrentRole())));
    }

    protected boolean hasRoleAuth() {
        return context.getCurrentRole() != null && !context.getCurrentRole().isBlank();
    }

    private String toSpringRole(String role) {
        return switch (role.toLowerCase()) {
            case "admin" -> "ADMIN";
            case "mecanico" -> "MECHANIC";
            case "atendente" -> "ATTENDANT";
            case "almoxarife" -> "STOREKEEPER";
            case "chatbot" -> "CHATBOT";
            default -> role.toUpperCase();
        };
    }

    protected void performPost(String url, String body, String token) {
        try {
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .with(SecurityMockMvcRequestPostProcessors.csrf());
            if (token != null) request.header("Authorization", "Bearer " + token);
            MvcResult result = mockMvc().perform(request).andReturn();
            context.setLastStatusCode(result.getResponse().getStatus());
            context.setLastResponseBody(result.getResponse().getContentAsString());
            context.setLastResponseContentType(result.getResponse().getContentType());
        } catch (Exception e) {
            context.setLastStatusCode(0);
            context.setLastResponseBody(e.getMessage());
            context.setLastResponseContentType(null);
        }
    }

    protected void performGet(String url, String token) {
        try {
            MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(url);
            if (token != null) request.header("Authorization", "Bearer " + token);
            MvcResult result = mockMvc().perform(request).andReturn();
            context.setLastStatusCode(result.getResponse().getStatus());
            context.setLastResponseBody(result.getResponse().getContentAsString());
            context.setLastResponseContentType(result.getResponse().getContentType());
        } catch (Exception e) {
            context.setLastStatusCode(0);
            context.setLastResponseBody(e.getMessage());
            context.setLastResponseContentType(null);
        }
    }

    protected MockHttpServletRequestBuilder buildRequest(String method, String url) {
        return switch (method.toUpperCase()) {
            case "GET" -> MockMvcRequestBuilders.get(url);
            case "POST" -> MockMvcRequestBuilders.post(url);
            case "PATCH" -> MockMvcRequestBuilders.patch(url);
            case "PUT" -> MockMvcRequestBuilders.put(url);
            case "DELETE" -> MockMvcRequestBuilders.delete(url);
            default -> throw new IllegalArgumentException("Método HTTP não suportado: " + method);
        };
    }

    protected String extractJsonField(JsonNode body, String field) {
        if (body == null || body.isNull()) {
            return null;
        }

        JsonNode valueNode = body.path(field);
        if (valueNode.isMissingNode() || valueNode.isNull()) {
            return null;
        }

        return valueNode.isValueNode() ? valueNode.asText() : valueNode.toString();
    }

    protected void executeWithOptionalAuth(String method, String url, String body) {
        try {
            MockHttpServletRequestBuilder request = withRoleAuth(buildRequest(method, url)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON));

            if (body != null) {
                request.content(body);
            }
            if (hasRoleAuth() && !"GET".equalsIgnoreCase(method)) {
                request.with(SecurityMockMvcRequestPostProcessors.csrf());
            }

            MvcResult result = mockMvc().perform(request).andReturn();
            context.setLastStatusCode(result.getResponse().getStatus());
            context.setLastResponseBody(result.getResponse().getContentAsString());
            context.setLastResponseContentType(result.getResponse().getContentType());
        } catch (Exception e) {
            context.setLastStatusCode(0);
            context.setLastResponseBody(e.getMessage());
            context.setLastResponseContentType(null);
        }
    }

    protected static <T> Iterable<T> iterable(java.util.Iterator<T> iterator) {
        return () -> iterator;
    }
}
