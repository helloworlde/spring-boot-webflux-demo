package io.github.helloworlde.stream.angular.config;

import io.github.helloworlde.stream.angular.model.Post;
import io.github.helloworlde.stream.angular.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

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
        log.info("Start post data initialization ...");

        initPosts();

    }

    private void initPosts() {
        postRepository.deleteAll()
                .thenMany(
                        Flux.range(1, 10)
                                .flatMap(title -> postRepository.save(
                                        Post.builder()
                                                .title("Post " + title)
                                                .content("Content of " + title)
                                                .build()
                                        )
                                )
                )
                .log()
                .subscribe(
                        null,
                        null,
                        () -> log.info("Done post data initialization ...")
                );
    }
}
