package io.github.helloworlde.thymeleaf.controller;

import io.github.helloworlde.thymeleaf.common.NotFoundException;
import io.github.helloworlde.thymeleaf.model.Post;
import io.github.helloworlde.thymeleaf.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.spring5.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author HelloWood
 * @date 2019-01-08 15:22
 */
@Slf4j
@Controller
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @GetMapping("")
    public String list(final Model model) {
        Flux<Post> posts = postRepository.findAll();
        model.addAttribute("posts", new ReactiveDataDriverContextVariable(posts));
        model.addAttribute("post", new Post());
        return "post";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public Mono<Post> get(@PathVariable("id") String id) {
        return postRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException(id)));
    }

    @PutMapping("/{id}")
    @ResponseBody
    public Mono<Post> update(@PathVariable("id") String id, @RequestBody Post post) {
        return postRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException(id)))
                .map(p -> {
                    p.setTitle(post.getTitle());
                    p.setContent(post.getContent());
                    return p;
                })
                .flatMap(p -> postRepository.save(p));
    }

    @PostMapping("")
    public String save(@ModelAttribute("post") Post post) {
        postRepository.save(post).subscribe(p -> log.info("{}", p));
        return "redirect:/posts";
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public Mono<Void> delete(@PathVariable("id") String id) {
        return postRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException(id)))
                .then(postRepository.deleteById(id));
    }

}