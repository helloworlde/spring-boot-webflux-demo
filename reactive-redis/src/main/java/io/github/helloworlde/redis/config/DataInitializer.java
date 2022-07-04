package io.github.helloworlde.redis.config;

import io.github.helloworlde.redis.model.Post;
import io.github.helloworlde.redis.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Stream;

/**
 * @author HelloWood
 * @date 2019-01-08 14:24
 */
@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PostRepository postRepository;

    @Override
    public void run(String... args) throws Exception {
        postRepository.deleteAll();
        Stream.of("Post One", "Post Two")
                .forEach(title -> postRepository.save(
                        Post.builder()
                                .id(UUID.randomUUID().toString())
                                .title(title)
                                .content("Content of " + title)
                                .build()
                ));
    }
}
