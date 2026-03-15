package com.aiott.ottpoc.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

/**
 * Converts Render's DATABASE_URL (postgres[ql]://user:pass@host/db)
 * to a JDBC URL (jdbc:postgresql://user:pass@host/db) before Spring
 * initializes any beans.
 *
 * Registered via META-INF/spring/org.springframework.boot.env.EnvironmentPostProcessor.imports
 */
public class RenderDatabaseUrlPostProcessor implements EnvironmentPostProcessor {

    private static final String DATABASE_URL = "DATABASE_URL";
    private static final String DATASOURCE_URL = "spring.datasource.url";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {
        String rawUrl = System.getenv(DATABASE_URL);
        if (rawUrl == null || rawUrl.isBlank()) {
            return;
        }

        String jdbcUrl = rawUrl
                .replaceFirst("^postgresql://", "jdbc:postgresql://")
                .replaceFirst("^postgres://",   "jdbc:postgresql://");

        // Inject with highest priority so it overrides application.yml defaults
        environment.getPropertySources().addFirst(
                new MapPropertySource("renderDatabaseUrl", Map.of(DATASOURCE_URL, jdbcUrl))
        );
    }
}
