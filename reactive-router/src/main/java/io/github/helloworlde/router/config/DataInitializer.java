package io.github.helloworlde.router.config;

import io.github.helloworlde.router.model.Post;
import io.github.helloworlde.router.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * @author HelloWood
 * @date 2019-01-10 17:21
 */

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PostRepository postRepository;

    @Async
    @Override
    public void run(String... args) throws Exception {
        log.info("Start post data initialization ...");

        postRepository.deleteAll()
                .thenMany(
                        Flux.just("Post One", "Post Two")
                                .flatMap(title -> postRepository.save(
                                        Post.builder()
                                                .title(title)
                                                .content("Content of " + title)
                                                .build()
                                ))
                )
                .log()
                .subscribe(
                        null,
                        null,
                        () -> log.info("Done post data initialization ...")
                );
    }
}
