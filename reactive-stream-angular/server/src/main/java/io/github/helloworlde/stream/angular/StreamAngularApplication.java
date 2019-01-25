package io.github.helloworlde.stream.angular;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * @author HelloWood
 */
@EnableMongoAuditing
@SpringBootApplication
public class StreamAngularApplication {

    public static void main(String[] args) {
        SpringApplication.run(StreamAngularApplication.class, args);
    }

}

