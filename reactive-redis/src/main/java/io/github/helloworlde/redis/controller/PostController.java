package io.github.helloworlde.redis.controller;

import io.github.helloworlde.redis.model.Post;
import io.github.helloworlde.redis.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author HelloWood
 * @date 2019-01-08 14:28
 */
@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @GetMapping("")
    public Flux<Iterable<Post>> list() {
        return Flux.just(postRepository.findAll());
    }

}
