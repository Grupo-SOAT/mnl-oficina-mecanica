package br.com.fiap.postech.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
@Profile({"local", "test", "it"})
public class CanonicalSeedRunner implements ApplicationRunner {

    private final DataSource dataSource;
    private final boolean seedEnabled;
    private final Resource[] seedLocations;

    public CanonicalSeedRunner(
            DataSource dataSource,
            @Value("${app.seed.enabled:false}") boolean seedEnabled,
            @Value("${app.seed.locations:}") Resource[] seedLocations) {
        this.dataSource = dataSource;
        this.seedEnabled = seedEnabled;
        this.seedLocations = seedLocations;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!seedEnabled || seedLocations == null || seedLocations.length == 0) {
            return;
        }

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        for (Resource resource : seedLocations) {
            if (resource.exists()) {
                populator.addScript(resource);
            }
        }
        populator.execute(dataSource);
    }
}
