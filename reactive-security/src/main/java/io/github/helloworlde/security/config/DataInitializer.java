package io.github.helloworlde.security.config;

import io.github.helloworlde.security.model.Post;
import io.github.helloworlde.security.model.User;
import io.github.helloworlde.security.repository.PostRepository;
import io.github.helloworlde.security.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

/**
 * @author HelloWood
 * @date 2019-01-08 15:24
 */
@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initPosts();
        initUsers();
    }

    private void initUsers() {
        log.info("Start user data initialization ...");
        userRepository.deleteAll()
                .thenMany(
                        Flux.just("user", "admin")
                                .flatMap(username -> {
                                            List<String> roles = "user".equals(username)
                                                    ? Arrays.asList("ROLE_USER")
                                                    : Arrays.asList("ROLE_USER", "ROLE_ADMIN");

                                            User user = User.builder()
                                                    .username(username)
                                                    .password(passwordEncoder.encode("password"))
                                                    .active(true)
                                                    .roles(roles)
                                                    .build();
                                            return userRepository.save(user);
                                        }
                                )
                )
                .log()
                .subscribe(
                        null,
                        null,
                        () -> log.info("Done user data initialization ..."));

    }

    private void initPosts() {
        log.info("Start post data initialization ...");

        postRepository.deleteAll()
                .thenMany(
                        Flux.just("Post One", "Post Two")
                                .flatMap(title -> postRepository.save(
                                        Post.builder()
                                                .title(title)
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
