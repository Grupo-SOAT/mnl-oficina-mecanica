package br.com.fiap.postech;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@org.springframework.test.context.ContextConfiguration(initializers = br.com.fiap.postech.config.PostgresContainerInitializer.class)
class MechanicalWorkshopSystemTests {

    @Test
    void contextLoads() {
    }

}
