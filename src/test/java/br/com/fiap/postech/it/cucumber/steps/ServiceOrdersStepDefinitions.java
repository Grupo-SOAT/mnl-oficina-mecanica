package br.com.fiap.postech.it.cucumber.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServiceOrdersStepDefinitions extends BaseStepDefinition {
    private Long serviceOrderId;
    private Long serviceId;
    private String budgetDecision;
    private String budgetComment;
    private String budgetRejectedServiceIds;

    @Before
    public void initialize() {
        initializeFeature();
        runCanonicalSeed();
        runDomainSeed("service-orders");

        serviceOrderId = null;
        serviceId = null;
        budgetDecision = null;
        budgetComment = null;
        budgetRejectedServiceIds = null;
    }

    private void runCanonicalSeed() {
        org.springframework.jdbc.datasource.init.ResourceDatabasePopulator populator =
                new org.springframework.jdbc.datasource.init.ResourceDatabasePopulator();
        populator.addScript(new org.springframework.core.io.ClassPathResource("db/seed/canonical-seed.sql"));
        populator.execute(dataSource);
    }

    @Dado("que o filtro status seja {string}")
    public void setStatusFilter(String value) {
        context.setFilterName("status");
        context.setFilterValue(value);
    }

    @Dado("que o filtro clientDocument seja {string}")
    public void setClientDocumentFilter(String value) {
        context.setFilterName("clientDocument");
        context.setFilterValue(value);
    }

    @Dado("que o filtro name seja {string}")
    public void setNameFilter(String value) {
        context.setFilterName("name");
        context.setFilterValue(value);
    }

    @Dado("que o filtro serviceId seja {long}")
    public void setServiceIdFilter(Long value) {
        context.setFilterName("serviceId");
        context.setFilterValue(String.valueOf(value));
    }

    @Dado("que o id da ordem de serviço seja {long}")
    public void setServiceOrderId(Long id) {
        serviceOrderId = id;
    }

    @Dado("que o id do serviço da ordem de serviço seja {long}")
    public void setServiceId(Long id) {
        serviceId = id;
    }

    @Dado("que o corpo da nova ordem de serviço seja:")
    public void setCreateOrderBody(DataTable table) throws Exception {
        setJsonBody(table);
    }

    @Dado("que o corpo de atualização da ordem de serviço seja:")
    public void setUpdateOrderBody(DataTable table) throws Exception {
        setJsonBody(table);
        JsonNode root = objectMapper.readTree(context.getRequestBody());
        if (root instanceof ObjectNode objectNode) {
            objectNode.fields().forEachRemaining(entry -> {
                JsonNode value = entry.getValue();
                if (value != null && value.isTextual() && value.asText().matches("^<.*>$")) {
                    objectNode.putNull(entry.getKey());
                }
            });
            context.setRequestBody(objectMapper.writeValueAsString(objectNode));
        }
    }

    @Dado("que o corpo do novo serviço da ordem de serviço seja:")
    public void setCreateServiceBody(DataTable table) throws Exception {
        setJsonBody(table);
        context.setRequestBody(normalizeNeededSupplies(context.getRequestBody()));
    }

    @Dado("que o corpo de atualização do serviço da ordem de serviço seja:")
    public void setUpdateServiceBody(DataTable table) throws Exception {
        setJsonBody(table);
        context.setRequestBody(normalizeNeededSupplies(context.getRequestBody()));
    }

    @Dado("que o corpo da ação de progresso da ordem de serviço seja:")
    public void setProgressBody(DataTable table) throws Exception {
        setJsonBody(table);
    }

    @Dado("que a decisão do cliente seja {string}")
    public void setBudgetDecision(String decision) {
        budgetDecision = decision;
    }

    @Dado("que a observação da decisão seja {string}")
    public void setBudgetComment(String comment) {
        budgetComment = comment;
    }

    @Dado("que os serviços rejeitados sejam {string}")
    public void setRejectedServiceIds(String rejectedServiceIds) {
        budgetRejectedServiceIds = rejectedServiceIds;
    }

    @Dado("que o orçamento da ordem de serviço de ID {string} foi enviado ao cliente")
    public void setBudgetServiceOrderId(String value) {
        serviceOrderId = Long.parseLong(value);
        performLoginAs("admin");
    }

    @Quando("eu listar as ordens de serviço")
    public void listServiceOrders() {
        executeRequest("GET", "/service-orders", null, paginationParams());
    }

    @Quando("eu consultar a ordem de serviço por id")
    public void getServiceOrderById() {
        executeRequest("GET", "/service-orders/" + serviceOrderId, null, null);
    }

    @Quando("eu criar a ordem de serviço")
    public void createServiceOrder() {
        executeRequest("POST", "/service-orders", context.getRequestBody(), null);
    }

    @Quando("eu criar a ordem de serviço em cascata")
    public void createServiceOrderCascade() {
        executeRequest("POST", "/service-orders/cascade", context.getRequestBody(), null);
    }

    @Quando("eu atualizar a ordem de serviço")
    public void updateServiceOrder() {
        executeRequest("PATCH", "/service-orders/" + serviceOrderId, context.getRequestBody(), null);
    }

    @Quando("eu remover a ordem de serviço")
    public void deleteServiceOrder() {
        executeRequest("DELETE", "/service-orders/" + serviceOrderId, null, null);
    }

    @Quando("eu listar os serviços da ordem de serviço")
    public void listServices() {
        executeRequest("GET", "/service-orders/" + serviceOrderId + "/services", null, paginationParams());
    }

    @Quando("eu consultar o serviço da ordem de serviço por id")
    public void getServiceById() {
        executeRequest("GET", "/service-orders/" + serviceOrderId + "/services/" + serviceId, null, null);
    }

    @Quando("eu incluir o serviço na ordem de serviço")
    public void createService() {
        executeRequest("POST", "/service-orders/" + serviceOrderId + "/services", context.getRequestBody(), null);
    }

    @Quando("eu atualizar o serviço da ordem de serviço")
    public void updateService() {
        executeRequest("PATCH", "/service-orders/" + serviceOrderId + "/services/" + serviceId, context.getRequestBody(), null);
    }

    @Quando("eu remover o serviço da ordem de serviço")
    public void deleteService() {
        executeRequest("DELETE", "/service-orders/" + serviceOrderId + "/services/" + serviceId, null, null);
    }

    @Quando("eu registrar o progresso da ordem de serviço")
    public void registerProgress() {
        executeRequest("POST", "/service-orders/" + serviceOrderId + "/progress", context.getRequestBody(), null);
    }

    @Quando("eu registrar a decisão do cliente sobre o orçamento da ordem de serviço")
    public void registerBudgetDecision() throws Exception {
        java.util.List<Long> rejectedIds = new java.util.ArrayList<>();
        if (budgetRejectedServiceIds != null && !budgetRejectedServiceIds.isBlank()) {
            for (String part : budgetRejectedServiceIds.split(",")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    rejectedIds.add(Long.parseLong(trimmed));
                }
            }
        }

        String body = objectMapper.writeValueAsString(java.util.Map.of(
                "decision", budgetDecision,
                "comment", budgetComment,
                "rejectedServiceIds", rejectedIds
        ));
        executeRequest("POST", "/service-orders/" + serviceOrderId + "/budget", body, null);
    }

    @Então("a resposta deve conter no maximo {int} ordens de serviço")
    public void verifyMaxOrders(int maxSize) throws Exception {
        assertMaxDataSize(maxSize, "ordens de serviço");
    }

    @Então("a resposta deve conter ao menos uma ordem de serviço com statusLabel {string}")
    public void verifyOrderStatusLabel(String expectedLabel) throws Exception {
        assertHasItemWithField("statusLabel", expectedLabel, "ordem de serviço");
    }

    @Então("a resposta deve conter ao menos uma ordem de serviço com status {string}")
    public void verifyOrderStatus(String expectedStatus) throws Exception {
        assertHasItemWithField("status", expectedStatus, "ordem de serviço");
    }

    @Então("a resposta não deve conter nenhuma ordem de serviço com status {string}")
    public void verifyOrderStatusAbsent(String unexpectedStatus) {
        assertNoItemWithField("status", unexpectedStatus, "ordem de serviço");
    }

    @Então("a ordem das ordens de serviço retornadas deve ser {string}")
    public void verifyOrdersSequence(String expectedIds) {
        assertDataIdsInOrder(expectedIds, "ordens de serviço");
    }

    @Então("a resposta deve conter no maximo {int} serviços da ordem de serviço")
    public void verifyMaxServices(int maxSize) throws Exception {
        assertMaxDataSize(maxSize, "serviços da ordem de serviço");
    }

    @Então("a resposta deve conter ao menos um serviço da ordem de serviço com name {string}")
    public void verifyServiceName(String expectedName) throws Exception {
        assertHasItemWithField("name", expectedName, "serviço da ordem de serviço");
    }

    @Então("a resposta deve conter o id do novo veículo")
    public void verifyNewVehicleIdInResponse() {
        JsonNode root = context.getLastResponseBody();
        assertTrue(root.has("vehicleId"), "Campo vehicleId ausente");
        assertTrue(root.get("vehicleId").asLong() > 0, "vehicleId deve ser maior que zero");
    }

    @Então("a resposta deve conter o id do novo cliente")
    public void verifyNewClientIdInResponse() {
        JsonNode root = context.getLastResponseBody();
        assertTrue(root.has("clientId"), "Campo clientId ausente");
        assertTrue(root.get("clientId").asLong() > 0, "clientId deve ser maior que zero");
    }

    private String normalizeNeededSupplies(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        if (!(root instanceof ObjectNode objectNode) || !objectNode.has("neededSupplies")) {
            return body;
        }

        JsonNode neededSuppliesNode = objectNode.get("neededSupplies");
        if (!neededSuppliesNode.isTextual()) {
            return body;
        }

        String raw = neededSuppliesNode.asText().trim();
        ArrayNode neededSupplies = objectMapper.createArrayNode();
        if (!raw.isBlank() && !raw.equals(",")) {
            ObjectNode item = objectMapper.createObjectNode();
            for (String part : raw.split(",")) {
                String[] kv = part.split("=", 2);
                if (kv.length != 2) {
                    continue;
                }
                String key = kv[0].trim();
                String value = kv[1].trim();
                switch (key) {
                    case "sku", "idSupply" -> item.put("idSupply", Long.parseLong(value.replace("SKU-", "")));
                    case "quantity" -> item.put("quantity", Integer.parseInt(value));
                    case "note" -> item.put("note", value);
                    default -> {
                    }
                }
            }
            if (!item.isEmpty()) {
                neededSupplies.add(item);
            }
        }

        objectNode.set("neededSupplies", neededSupplies);
        return objectMapper.writeValueAsString(objectNode);
    }
}
