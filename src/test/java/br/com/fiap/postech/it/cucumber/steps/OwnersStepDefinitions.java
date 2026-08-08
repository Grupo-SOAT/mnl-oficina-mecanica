package br.com.fiap.postech.it.cucumber.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public class OwnersStepDefinitions extends BaseStepDefinition {
    private Long clientId;

    @Before
    public void initialize() {
        initializeFeature("owners");
        clientId = null;
    }

    @Dado("que o filtro document seja {string}")
    public void setDocumentFilter(String value) {
        context.setFilterName("document");
        context.setFilterValue(value);
    }

    @Dado("que o id do cliente seja {long}")
    public void setClientId(Long id) {
        clientId = id;
    }

    @Dado("que o corpo do novo cliente seja:")
    public void setCreateBody(DataTable table) throws Exception {
        setJsonBody(table);
    }

    @Dado("que o corpo de atualização do cliente seja:")
    public void setUpdateBody(DataTable table) throws Exception {
        setJsonBody(table);
    }

    @Quando("eu listar os clientes")
    public void listClients() {
        executeRequest("GET", "/owners", null, paginationParams());
    }

    @Quando("eu consultar o cliente por id")
    public void getClientById() {
        executeRequest("GET", "/owners/" + clientId, null, null);
    }

    @Quando("eu criar o cliente")
    public void createClient() {
        executeRequest("POST", "/owners", context.getRequestBody(), null);
    }

    @Quando("eu atualizar o cliente")
    public void updateClient() {
        executeRequest("PATCH", "/owners/" + clientId, context.getRequestBody(), null);
    }

    @Quando("eu remover o cliente")
    public void deleteClient() {
        executeRequest("DELETE", "/owners/" + clientId, null, null);
    }

    @Então("a resposta deve conter no maximo {int} clientes")
    public void verifyMaxClients(int maxSize) throws Exception {
        assertMaxDataSize(maxSize, "clientes");
    }

    @Então("a resposta deve conter ao menos um cliente com document {string}")
    public void verifyClientDocument(String expectedDocument) throws Exception {
        assertHasItemWithField("document", expectedDocument, "cliente");
    }
}