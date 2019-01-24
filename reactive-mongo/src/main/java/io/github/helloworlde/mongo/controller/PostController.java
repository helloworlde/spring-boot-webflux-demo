package io.github.helloworlde.mongo.controller;

import io.github.helloworlde.mongo.common.NotFoundException;
import io.github.helloworlde.mongo.model.Post;
import io.github.helloworlde.mongo.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author HelloWood
 * @date 2019-01-08 15:22
 */
@RestController
@RequestMapping("/posts")
@CrossOrigin(origins = "http://localhost:4200")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @GetMapping("")
    public Flux<Post> list() {
        return postRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Post> get(@PathVariable("id") String id) {
        return postRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException(id)));
    }

    @PutMapping("/{id}")
    public Mono<Post> update(@PathVariable("id") String id, @RequestBody Post post) {
        return postRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException(id)))
                .map(p -> {
                    p.setTitle(post.getTitle());
                    p.setContent(post.getContent());
                    p.setAuthor(post.getAuthor());
                    return p;
                })
                .flatMap(p -> postRepository.save(p));
    }

    @PostMapping("")
    public Mono<Post> save(@RequestBody Post post) {
        return postRepository.save(post);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable("id") String id) {
        return postRepository
                .findById(id)
                .then(postRepository.deleteById(id));
    }

}