package io.github.helloworlde.router;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * @author HelloWood
 */
@EnableMongoAuditing
@SpringBootApplication
public class ReactiveRouterApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReactiveRouterApplication.class, args);
    }

}

