package br.com.fiap.postech.it.cucumber.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

public class VehiclesStepDefinitions extends BaseStepDefinition {
    private Long vehicleId;

    @Before
    public void initialize() {
        initializeFeature("vehicles");
        vehicleId = null;
    }

    @Dado("que o filtro licensePlate seja {string}")
    public void setLicensePlateFilter(String value) {
        context.setFilterName("licensePlate");
        context.setFilterValue(value);
    }

    @Dado("que o id do veiculo seja {long}")
    public void setVehicleId(Long id) {
        vehicleId = id;
    }

    @Dado("que o corpo do novo veiculo seja:")
    public void setCreateBody(DataTable table) throws Exception {
        setJsonBody(table);
    }

    @Dado("que o corpo de atualização do veiculo seja:")
    public void setUpdateBody(DataTable table) throws Exception {
        setJsonBody(table);
    }

    @Quando("eu listar os veiculos")
    public void listVehicles() {
        executeRequest("GET", "/vehicles", null, paginationParams());
    }

    @Quando("eu consultar o veiculo por id")
    public void getVehicleById() {
        executeRequest("GET", "/vehicles/" + vehicleId, null, null);
    }

    @Quando("eu criar o veiculo")
    public void createVehicle() {
        executeRequest("POST", "/vehicles", context.getRequestBody(), null);
    }

    @Quando("eu atualizar o veiculo")
    public void updateVehicle() {
        executeRequest("PATCH", "/vehicles/" + vehicleId, context.getRequestBody(), null);
    }

    @Quando("eu remover o veiculo")
    public void deleteVehicle() {
        executeRequest("DELETE", "/vehicles/" + vehicleId, null, null);
    }

    @Então("a resposta deve conter no maximo {int} veiculos")
    public void verifyMaxVehicles(int maxSize) throws Exception {
        assertMaxDataSize(maxSize, "veiculos");
    }

    @Então("a resposta deve conter ao menos um veiculo com licensePlate {string}")
    public void verifyLicensePlate(String expectedLicensePlate) throws Exception {
        assertHasItemWithField("licensePlate", expectedLicensePlate, "veiculo");
    }
}