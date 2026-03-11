import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.ResultSet;

@Configuration
@RequiredArgsConstructor
public class DbInfoLogger {

    private final DataSource dataSource;

    @Bean
    ApplicationRunner logDbInfo() {
        return args -> {
            try (var c = dataSource.getConnection();
                 var st = c.createStatement()) {

                ResultSet rs = st.executeQuery("select current_database(), current_user, current_schema()");
                if (rs.next()) {
                    System.out.println("[DBINFO] db=" + rs.getString(1)
                            + ", user=" + rs.getString(2)
                            + ", schema=" + rs.getString(3));
                }
            }
        };
    }
}
