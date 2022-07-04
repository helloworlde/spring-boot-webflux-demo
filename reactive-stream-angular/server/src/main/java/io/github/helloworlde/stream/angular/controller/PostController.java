package io.github.helloworlde.stream.angular.controller;

import io.github.helloworlde.stream.angular.model.Post;
import io.github.helloworlde.stream.angular.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * @author HelloWood
 * @date 2019-01-08 15:22
 */
@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @GetMapping("/block")
    public Flux<Post> list() {
        return postRepository.findAll().delayElements(Duration.ofMillis(500));
    }

    @GetMapping(value = "/nonblock", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Post> online() {
        return postRepository.findAll().delayElements(Duration.ofMillis(500));
    }
}