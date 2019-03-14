package io.github.helloworlde.postgre;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/**
 * @author HelloWood
 */
@SpringBootApplication
@EnableR2dbcRepositories
public class PostgreApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostgreApplication.class, args);
    }

}

