package br.com.fiap.postech.it.cucumber.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public class SuppliersStepDefinitions extends BaseStepDefinition {
    private Long supplierId;

    @Before
    public void initialize() {
        initializeFeature("suppliers");
        supplierId = null;
    }

    @Dado("que o id do fornecedor seja {long}")
    public void setSupplierId(Long id) {
        supplierId = id;
    }

    @Dado("que o corpo do novo fornecedor seja:")
    public void setCreateBody(DataTable table) throws Exception {
        setJsonBody(table);
    }

    @Dado("que o corpo de atualização do fornecedor seja:")
    public void setUpdateBody(DataTable table) throws Exception {
        setJsonBody(table);
    }

    @Quando("eu listar os fornecedores")
    public void listSuppliers() {
        executeRequest("GET", "/suppliers", null, paginationParams());
    }

    @Quando("eu consultar o fornecedor por id")
    public void getSupplierById() {
        executeRequest("GET", "/suppliers/" + supplierId, null, null);
    }

    @Quando("eu criar o fornecedor")
    public void createSupplier() {
        executeRequest("POST", "/suppliers", context.getRequestBody(), null);
    }

    @Quando("eu atualizar o fornecedor")
    public void updateSupplier() {
        executeRequest("PATCH", "/suppliers/" + supplierId, context.getRequestBody(), null);
    }

    @Quando("eu remover o fornecedor")
    public void deleteSupplier() {
        executeRequest("DELETE", "/suppliers/" + supplierId, null, null);
    }

    @Então("a resposta deve conter no maximo {int} fornecedores")
    public void verifyMaxSuppliers(int maxSize) throws Exception {
        assertMaxDataSize(maxSize, "fornecedores");
    }

    @Então("a resposta deve conter ao menos um fornecedor com document {string}")
    public void verifySupplierDocument(String expectedDocument) throws Exception {
        assertHasItemWithField("document", expectedDocument, "fornecedor");
    }
}