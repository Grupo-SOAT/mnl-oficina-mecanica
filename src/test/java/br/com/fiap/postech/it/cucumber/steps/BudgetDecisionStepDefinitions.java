package br.com.fiap.postech.it.cucumber.steps;

import br.com.fiap.postech.adapter.input.serviceorder.message.event.BudgetDecisionEvent;
import br.com.fiap.postech.it.cucumber.util.KafkaTestHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BudgetDecisionStepDefinitions extends BaseStepDefinition {

    @Autowired
    private KafkaTestHelper kafkaTestHelper;

    private Long serviceOrderId;

    @Before("@asyncBudget")
    public void initialize() {
        initializeFeature();
        runCanonicalSeed();
        runDomainSeed("service-orders");
        kafkaTestHelper.drainAll();
        serviceOrderId = null;
    }

    private void runCanonicalSeed() {
        var populator = new org.springframework.jdbc.datasource.init.ResourceDatabasePopulator();
        populator.addScript(new org.springframework.core.io.ClassPathResource("db/seed/canonical-seed.sql"));
        populator.execute(dataSource);
    }

    // -- SETUP STEPS --

    @Dado("que exista uma ordem de serviço de ID {string} com status {string}")
    public void ensureServiceOrderInStatus(String id, String status) throws Exception {
        serviceOrderId = Long.parseLong(id);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE service_orders SET status = ? WHERE service_order_id = ?")) {
            stmt.setString(1, status);
            stmt.setLong(2, serviceOrderId);
            int updated = stmt.executeUpdate();
            assertTrue(updated > 0, "Ordem de serviço " + id + " não encontrada");
        }
    }

    @Dado("que um evento de decisão seja publicado no tópico {string}")
    public void publishDecisionEvent(String topic, DataTable table) throws Exception {
        Map<String, String> row = table.asMaps(String.class, String.class).get(0);
        BudgetDecisionEvent event = new BudgetDecisionEvent();
        event.setServiceOrderId(Long.parseLong(row.get("serviceOrderId")));
        event.setDecision(row.get("decision"));
        kafkaTestHelper.send(topic, row.get("serviceOrderId"), event);
    }

    // -- ACT STEPS --

    @Quando("o sistema consumir o evento do tópico {string}")
    public void awaitEventProcessing(String topic) {
        long deadline = System.currentTimeMillis() + 10_000;
        while (System.currentTimeMillis() < deadline) {
            executeRequest("GET", "/service-orders/" + serviceOrderId, null, null);
            if (context.getLastStatusCode() == 200) {
                JsonNode body = context.getLastResponseBody();
                if (body.has("status") && !"AWAITING_APPROVAL".equals(body.get("status").asText())) {
                    return;
                }
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // -- ASSERT STEPS --

    @Então("um evento deve ser publicado no tópico {string} com o {string} igual a {string}")
    public void assertEventPublished(String topic, String field, String expectedValue) throws Exception {
        ConsumerRecord<String, String> record = kafkaTestHelper.receive(topic, 10);
        assertNotNull(record, "Nenhum evento encontrado no tópico " + topic);
        JsonNode event = new ObjectMapper().readTree(record.value());
        assertTrue(event.has(field), "Campo " + field + " ausente no evento");
        String actual = event.get(field).asText();
        assertTrue(expectedValue.equals(actual),
                "Campo " + field + " esperado " + expectedValue + " mas era " + actual);
    }

    @Então("o evento publicado no tópico {string} deve conter o campo {string} com um valor não vazio")
    public void assertEventFieldNonEmpty(String topic, String field) throws Exception {
        ConsumerRecord<String, String> record = kafkaTestHelper.receive(topic, 10);
        assertNotNull(record, "Nenhum evento encontrado no tópico " + topic);
        JsonNode event = new ObjectMapper().readTree(record.value());
        assertTrue(event.has(field), "Campo " + field + " ausente no evento");
        String value = event.get(field).asText();
        assertTrue(value != null && !value.isBlank(), "Campo " + field + " está vazio");
    }

    @Então("o statusLabel da ordem de serviço de ID {string} deve ser {string}")
    public void assertServiceOrderStatusLabel(String id, String expectedLabel) {
        executeRequest("GET", "/service-orders/" + id, null, null);
        assertTrue(context.getLastStatusCode() == 200,
                "Status HTTP " + context.getLastStatusCode() + " ao consultar OS " + id);
        JsonNode body = context.getLastResponseBody();
        assertTrue(body.has("statusLabel"), "Campo statusLabel ausente na resposta da OS " + id);
        assertTrue(expectedLabel.equals(body.get("statusLabel").asText()),
                "StatusLabel esperado " + expectedLabel + " mas era " + body.get("statusLabel").asText());
    }

    @Então("o status da ordem de serviço de ID {string} deve ser {string}")
    public void assertServiceOrderStatus(String id, String expectedStatus) {
        executeRequest("GET", "/service-orders/" + id, null, null);
        assertTrue(context.getLastStatusCode() == 200,
                "Status HTTP " + context.getLastStatusCode() + " ao consultar OS " + id);
        JsonNode body = context.getLastResponseBody();
        assertTrue(body.has("status"), "Campo status ausente na resposta da OS " + id);
        assertTrue(expectedStatus.equals(body.get("status").asText()),
                "Status esperado " + expectedStatus + " mas era " + body.get("status").asText());
    }

    @Então("o campo decimal {string} da ordem de serviço de ID {string} deve ser {string}")
    public void assertServiceOrderDecimalField(String field, String id, String expectedValue) {
        executeRequest("GET", "/service-orders/" + id, null, null);
        assertTrue(context.getLastStatusCode() == 200,
                "Status HTTP " + context.getLastStatusCode() + " ao consultar OS " + id);
        JsonNode body = context.getLastResponseBody();
        assertTrue(body.has(field), "Campo " + field + " ausente na resposta da OS " + id);
        assertEquals(0, new BigDecimal(expectedValue).compareTo(body.get(field).decimalValue()),
                "Valor do campo " + field + " divergente: esperado " + expectedValue + " mas era " + body.get(field));
    }
}
