package br.com.fiap.postech.it.cucumber.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import java.util.HashMap;
import java.util.Map;

public class CatalogServicesStepDefinitions extends BaseStepDefinition {
    private static final Map<String, Long> SUPPLY_IDS_BY_SKU = new HashMap<>();

    static {
        SUPPLY_IDS_BY_SKU.put("SKU-001", 1L);
        SUPPLY_IDS_BY_SKU.put("SKU-002", 2L);
    }

    private Long catalogServiceId;

    @Before
    public void initialize() {
        initializeFeature("supplies");
        catalogServiceId = null;
    }

    @Dado("que o id do serviço catalogado seja {long}")
    public void setCatalogServiceId(Long id) {
        catalogServiceId = id;
    }

    @Dado("que o filtro id seja {long}")
    public void setIdFilter(Long value) {
        context.setFilterName("id");
        context.setFilterValue(String.valueOf(value));
    }

    @Dado("que o corpo do novo serviço catalogado seja:")
    public void setCreateBody(DataTable table) throws Exception {
        setCatalogServiceBody(table);
    }

    @Dado("que o corpo de atualização do serviço catalogado seja:")
    public void setUpdateBody(DataTable table) throws Exception {
        setCatalogServiceBody(table);
    }

    private void setCatalogServiceBody(DataTable table) throws Exception {
        setJsonBody(table);
        JsonNode root = objectMapper.readTree(context.getRequestBody());

        if (root instanceof ObjectNode objectNode) {
            remapField(objectNode, "nome", "name");
            remapField(objectNode, "descricao", "description");
            if (objectNode.has("name") && objectNode.get("name").isNull()) {
                objectNode.put("name", "");
            }
            if (objectNode.has("description") && objectNode.get("description").isNull()) {
                objectNode.put("description", "");
            }
            objectNode.set("neededSupplies", parseNeededSupplies(objectNode.path("neededSupplies").asText(null)));
            context.setRequestBody(objectMapper.writeValueAsString(objectNode));
        }
    }

    private void remapField(ObjectNode objectNode, String source, String target) {
        if (objectNode.has(source) && !objectNode.has(target)) {
            objectNode.set(target, objectNode.get(source));
            objectNode.remove(source);
        }
    }

    private ArrayNode parseNeededSupplies(String rawValue) {
        ArrayNode neededSupplies = objectMapper.createArrayNode();
        if (rawValue == null || rawValue.isBlank()) {
            return neededSupplies;
        }

        for (String item : rawValue.split(";")) {
            String trimmedItem = item.trim();
            if (trimmedItem.isEmpty()) {
                continue;
            }

            String[] parts = trimmedItem.split(",", -1);
            String sku = parts.length > 0 ? parts[0].trim() : "";
            ObjectNode neededSupply = objectMapper.createObjectNode();
            Long idSupply = SUPPLY_IDS_BY_SKU.get(sku);
            if (idSupply == null) {
                idSupply = 9999L;
            }
            neededSupply.put("idSupply", idSupply);
            if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                neededSupply.put("note", parts[1].trim());
            }
            if (parts.length > 2 && !parts[2].trim().isEmpty()) {
                neededSupply.put("quantity", Integer.parseInt(parts[2].trim()));
            }
            neededSupplies.add(neededSupply);
        }

        return neededSupplies;
    }

    @Quando("eu listar os serviços catalogados")
    public void listCatalogServices() {
        executeRequest("GET", "/catalog/services", null, paginationParams());
    }

    @Quando("eu consultar o serviço catalogado por id")
    public void getCatalogServiceById() {
        executeRequest("GET", "/catalog/services/" + catalogServiceId, null, null);
    }

    @Quando("eu criar o serviço catalogado")
    public void createCatalogService() {
        executeRequest("POST", "/catalog/services", context.getRequestBody(), null);
    }

    @Quando("eu atualizar o serviço catalogado")
    public void updateCatalogService() {
        executeRequest("PATCH", "/catalog/services/" + catalogServiceId, context.getRequestBody(), null);
    }

    @Quando("eu remover o serviço catalogado")
    public void deleteCatalogService() {
        executeRequest("DELETE", "/catalog/services/" + catalogServiceId, null, null);
    }

    @Então("a resposta deve conter no maximo {int} serviços catalogados")
    public void verifyMaxCatalogServices(int maxSize) {
        assertMaxDataSize(maxSize, "serviços catalogados");
    }

    @Então("a resposta deve conter ao menos um serviço catalogado com nome {string}")
    public void verifyCatalogServiceName(String expectedName) {
        assertHasItemWithField("name", expectedName, "serviço catalogado");
    }
}
