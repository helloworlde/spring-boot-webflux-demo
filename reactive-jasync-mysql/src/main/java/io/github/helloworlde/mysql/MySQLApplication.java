package io.github.helloworlde.mysql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/**
 * @author HelloWood
 */
@SpringBootApplication
@EnableR2dbcRepositories
public class MySQLApplication {
    public static void main(String[] args) {
        SpringApplication.run(MySQLApplication.class, args);
    }
}

