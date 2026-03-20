package com.aiott.ottpoc.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Slf4j
@Component
@Order(1)
public class FlywayMigrationChecker implements ApplicationRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .locations("classpath:db/migration")
                    .baselineOnMigrate(true)
                    .load();

            var result = flyway.migrate();
            log.info("Flyway 마이그레이션 완료: {}개 적용", result.migrationsExecuted);
        } catch (Exception e) {
            log.error("Flyway 마이그레이션 실패", e);
            throw new RuntimeException("Flyway 마이그레이션 실패", e);
        }
    }
}
