package br.com.fiap.postech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MechanicalWorkshopSystem {
    public static void main(String[] args) {
        SpringApplication.run(MechanicalWorkshopSystem.class, args);
    }
}
