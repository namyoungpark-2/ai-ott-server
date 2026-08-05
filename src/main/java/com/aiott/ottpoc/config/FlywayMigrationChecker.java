package com.aiott.ottpoc.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * spring.flyway.enabled=true 로 Boot 가 기동 시 이미 마이그레이션을 수행하므로
 * 이 러너는 사실상 중복이다(ApplicationRunner 라 컨텍스트 refresh 이후에 돈다).
 * 동작을 바꾸지 않기 위해 남겨 두되, 테스트에서는 제외한다 —
 * 마이그레이션이 PostgreSQL 전용 SQL 이어서 테스트용 H2 에서 깨진다.
 */
@Slf4j
@Component
@Profile("!test")
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
