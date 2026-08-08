package br.com.fiap.postech.it.cucumber.context;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.test.web.servlet.MockMvc;

public class CucumberTestContext {
    private static final CucumberTestContext INSTANCE = new CucumberTestContext();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public CucumberTestContext() {
    }

    private String currentUser;
    private String currentRole;
    private String currentPassword;
    private String currentApiKey;
    private String authToken;
    private int lastStatusCode;
    private String lastErrorMessage;
    private JsonNode lastResponseBody = NullNode.getInstance();
    private Integer pageSize;
    private String cursor;
    private String filterName;
    private String filterValue;
    private String requestBody;
    private String lastResponseContentType;
    private String lastResponseContentDisposition;
    private Long catalogServiceId;
    private MockMvc mockMvc;

    public static synchronized CucumberTestContext getInstance() {
        return INSTANCE;
    }

    public void reset() {
        this.currentUser = null;
        this.currentRole = null;
        this.currentPassword = null;
        this.currentApiKey = null;
        this.authToken = null;
        this.lastStatusCode = 0;
        this.lastErrorMessage = null;
        this.lastResponseBody = NullNode.getInstance();
        this.pageSize = null;
        this.cursor = null;
        this.filterName = null;
        this.filterValue = null;
        this.requestBody = null;
        this.lastResponseContentType = null;
        this.lastResponseContentDisposition = null;
        this.catalogServiceId = null;
    }

    public void setLastResponseBody(String rawBody) {
        if (rawBody == null) {
            this.lastResponseBody = NullNode.getInstance();
            return;
        }

        try {
            this.lastResponseBody = OBJECT_MAPPER.readTree(rawBody);
        } catch (Exception ignored) {
            this.lastResponseBody = TextNode.valueOf(rawBody);
        }
    }

    public String getLastResponseBodyAsString() {
        if (lastResponseBody == null || lastResponseBody.isNull()) {
            return "";
        }

        if (lastResponseBody.isTextual()) {
            return lastResponseBody.asText();
        }

        return lastResponseBody.toString();
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public void setCurrentRole(String currentRole) {
        this.currentRole = currentRole;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getCurrentApiKey() {
        return currentApiKey;
    }

    public void setCurrentApiKey(String currentApiKey) {
        this.currentApiKey = currentApiKey;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public int getLastStatusCode() {
        return lastStatusCode;
    }

    public void setLastStatusCode(int lastStatusCode) {
        this.lastStatusCode = lastStatusCode;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
    }

    public JsonNode getLastResponseBody() {
        return lastResponseBody;
    }

    public void setLastResponseBody(JsonNode lastResponseBody) {
        this.lastResponseBody = lastResponseBody;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getLastResponseContentType() {
        return lastResponseContentType;
    }

    public void setLastResponseContentType(String lastResponseContentType) {
        this.lastResponseContentType = lastResponseContentType;
    }

    public String getLastResponseContentDisposition() {
        return lastResponseContentDisposition;
    }

    public void setLastResponseContentDisposition(String lastResponseContentDisposition) {
        this.lastResponseContentDisposition = lastResponseContentDisposition;
    }

    public Long getCatalogServiceId() {
        return catalogServiceId;
    }

    public void setCatalogServiceId(Long catalogServiceId) {
        this.catalogServiceId = catalogServiceId;
    }

    public MockMvc getMockMvc() {
        return mockMvc;
    }

    public void setMockMvc(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }
}
