package com.fraudintel.config;

import com.pgvector.PGvector;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration
@Profile("!test")
public class PgvectorConfig {

    @Bean
    ApplicationRunner pgvectorTypeRegistration(DataSource dataSource) {
        return args -> {
            try (Connection connection = dataSource.getConnection()) {
                PGvector.registerTypes(connection);
            }
        };
    }
}
