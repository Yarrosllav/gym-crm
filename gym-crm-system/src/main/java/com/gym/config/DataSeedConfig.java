package com.gym.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class DataSeedConfig {

    @Bean
    CommandLineRunner seedTrainingTypes(DataSource dataSource) {
        log.debug("Seeding default training types");
        return args -> {
            try (var connection = dataSource.getConnection();
                 var statement = connection.createStatement()) {
                var rs = statement.executeQuery("SELECT COUNT(*) FROM training_types");
                rs.next();
                if (rs.getInt(1) == 0) {
                    statement.executeUpdate("""
                            INSERT INTO training_types (training_type_name) VALUES
                            ('Cardio'), ('Strength'), ('Yoga'), ('CrossFit')
                            """);
                }
            }
        };
    }
}
