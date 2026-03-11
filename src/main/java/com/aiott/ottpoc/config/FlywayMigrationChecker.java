package com.aiott.ottpoc.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Slf4j
@Component
@Order(1)
public class FlywayMigrationChecker implements ApplicationRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) {
        try {
            // flyway_schema_history 테이블이 있는지 확인
            Boolean flywayTableExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'flyway_schema_history')",
                Boolean.class
            );

            if (!Boolean.TRUE.equals(flywayTableExists)) {
                log.warn("=== Flyway 마이그레이션이 실행되지 않았습니다. 수동으로 실행합니다 ===");
                
                // Flyway를 수동으로 실행
                try {
                    Flyway flyway = Flyway.configure()
                            .dataSource(dataSource)
                            .locations("classpath:db/migration")
                            .baselineOnMigrate(true)
                            .load();
                    
                    log.info("Flyway 마이그레이션 실행 중...");
                    flyway.migrate();
                    log.info("Flyway 마이그레이션 완료!");
                } catch (Exception e) {
                    log.error("Flyway 마이그레이션 실행 실패", e);
                    throw e;
                }
            } else {
                // Flyway가 실행되었는지 확인
                Integer migrationCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM flyway_schema_history",
                    Integer.class
                );
                log.info("=== Flyway 마이그레이션 상태 확인 ===");
                log.info("Flyway 스키마 히스토리 테이블 존재: true");
                log.info("실행된 마이그레이션 수: {}", migrationCount);
            }

            // content 테이블이 있는지 확인
            Boolean contentTableExists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'content')",
                Boolean.class
            );
            log.info("content 테이블 존재: {}", contentTableExists);
            
            if (!Boolean.TRUE.equals(contentTableExists)) {
                log.error("content 테이블이 존재하지 않습니다! Flyway 마이그레이션이 실행되지 않았습니다.");
            }
        } catch (Exception e) {
            log.error("Flyway 마이그레이션 상태 확인 실패", e);
        }
    }
}
