package br.com.fiap.postech.it.cucumber.steps;

import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SuppliesStepDefinitions extends BaseStepDefinition {
    private Long supplyId;

    @Before("@supplies")
    public void initialize() {
        initializeFeature();
        resetSuppliesData();
        supplyId = null;
    }

    private void resetSuppliesData() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("db/seed/canonical-seed.sql"));
        populator.execute(dataSource);
    }

    @Dado("que o tamanho da pagina seja {int}")
    public void setPageSize(Integer value) {
        context.setPageSize(value);
    }

    @Dado("que o cursor seja {string}")
    public void setCursor(String value) {
        context.setCursor(value);
    }

    @Dado("que o filtro sku seja {string}")
    public void setSku(String value) {
        context.setFilterName("sku");
        context.setFilterValue(value);
    }

    @Dado("que o id do insumo seja {long}")
    public void setSupplyId(Long id) {
        supplyId = id;
    }

    @Dado("que o id do insumo seja o ultimo criado")
    public void setSupplyIdAsLastCreated() {
        JsonNode root = context.getLastResponseBody();
        assertTrue(root.has("id"), "Resposta anterior nao contem id para reutilizar");
        supplyId = root.get("id").asLong();
    }

    @Dado("que o corpo do novo insumo seja:")
    public void setCreateBody(DataTable table) throws Exception {
        setJsonBody(table);
    }

    @Dado("que o corpo de atualização do insumo seja:")
    public void setUpdateBody(DataTable table) throws Exception {
        setJsonBody(table);
    }

    @Quando("eu listar os insumos")
    public void listSupplies() {
        executeRequest("GET", "/supplies", null, paginationParams());
    }

    @Quando("eu consultar o insumo por id")
    public void getSupplyById() {
        executeWithOptionalAuth("GET", "/supplies/" + supplyId, null);
    }

    @Quando("eu criar o insumo")
    public void createSupply() {
        executeWithOptionalAuth("POST", "/supplies", context.getRequestBody());
    }

    @Quando("eu atualizar o insumo")
    public void updateSupply() {
        executeWithOptionalAuth("PATCH", "/supplies/" + supplyId, context.getRequestBody());
    }

    @Quando("eu remover o insumo")
    public void deleteSupply() {
        executeWithOptionalAuth("DELETE", "/supplies/" + supplyId, null);
    }

    @Então("a resposta deve conter o campo {string}")
    public void verifyFieldExists(String field) {
        JsonNode root = context.getLastResponseBody();
        assertTrue(root.has(field), "Campo ausente: " + field + " Body: " + context.getLastResponseBodyAsString());
    }

    @Então("a resposta deve conter uma lista de dados")
    public void verifyDataArray() {
        assertDataArray();
    }

    @Então("a resposta deve conter no maximo {int} insumos")
    public void verifyMaxDataSize(int maxSize) {
        assertMaxDataSize(maxSize, "insumos");
    }

    @Então("a resposta deve conter ao menos um insumo com sku {string}")
    public void verifyHasSupplyWithSku(String expectedSku) {
        assertHasItemWithField("sku", expectedSku, "insumo");
    }

    @Então("o primeiro item retornado deve ter id maior que {long}")
    public void verifyFirstSupplyIdGreaterThan(long minId) {
        assertFirstItemIdGreaterThan(minId, "insumos");
    }

    @Então("o campo {string} deve ser {string}")
    public void verifyFieldValue(String field, String expectedValue) {
        JsonNode root = context.getLastResponseBody();
        assertTrue(root.has(field), "Campo ausente: " + field + " em " + context.getLastResponseBodyAsString());
        assertEquals(expectedValue, root.get(field).asText());
    }

    @Então("a resposta deve conter o campo reason com valor {string}")
    public void verifyReason(String reason) {
        JsonNode root = context.getLastResponseBody();
        assertTrue(root.has("reason"), "Campo reason ausente");
        assertEquals(reason, root.get("reason").asText());
    }

    @Então("a resposta deve refletir o payload enviado")
    public void verifyPayloadValues() throws Exception {
        assertResponseMatchesRequest();
    }
}
