package br.com.fiap.postech.it.cucumber.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public class PurchaseOrdersStepDefinitions extends BaseStepDefinition {
    private Long purchaseOrderId;

    @Before
    public void initialize() {
        initializeFeature("purchase-orders", "supplies");
        purchaseOrderId = null;
    }

    @Dado("com o parametro de busca {string} igual a {string}")
    public void setSearchParameter(String field, String value) {
        context.setFilterName(field);
        context.setFilterValue(value);
    }

    @Dado("que o filtro supplierId seja {string}")
    public void setSupplierIdFilter(String value) {
        context.setFilterName("supplierId");
        context.setFilterValue(value);
    }

    @Dado("que o id do pedido de compra seja {long}")
    public void setPurchaseOrderId(Long id) {
        purchaseOrderId = id;
    }

    @Dado("que o corpo do novo pedido de compra seja:")
    public void setCreateBody(DataTable table) throws Exception {
        setJsonBody(table);
    }

    @Dado("que o corpo de atualização do pedido de compra seja:")
    public void setUpdateBody(DataTable table) throws Exception {
        setJsonBody(table);
    }

    @Quando("eu listar os pedidos de compra")
    public void listPurchaseOrders() {
        executeRequest("GET", "/purchase-orders", null, paginationParams());
    }

    @Quando("eu consultar o pedido de compra por id")
    public void getPurchaseOrderById() {
        executeRequest("GET", "/purchase-orders/" + purchaseOrderId, null, null);
    }

    @Quando("eu criar o pedido de compra")
    public void createPurchaseOrder() {
        executeRequest("POST", "/purchase-orders", context.getRequestBody(), null);
    }

    @Quando("eu atualizar o pedido de compra")
    public void updatePurchaseOrder() {
        executeRequest("PATCH", "/purchase-orders/" + purchaseOrderId, context.getRequestBody(), null);
    }

    @Quando("eu remover o pedido de compra")
    public void deletePurchaseOrder() {
        executeRequest("DELETE", "/purchase-orders/" + purchaseOrderId, null, null);
    }

    @Então("a resposta deve conter no maximo {int} pedidos de compra")
    public void verifyMaxPurchaseOrders(int maxSize) throws Exception {
        assertMaxDataSize(maxSize, "pedidos de compra");
    }
}