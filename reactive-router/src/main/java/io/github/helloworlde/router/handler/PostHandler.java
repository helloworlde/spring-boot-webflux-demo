package io.github.helloworlde.router.handler;

import io.github.helloworlde.router.common.NotFoundException;
import io.github.helloworlde.router.model.Post;
import io.github.helloworlde.router.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * @author HelloWood
 * @date 2019-01-10 16:49
 */
@Component
@Slf4j
public class PostHandler {

    @Autowired
    private PostRepository postRepository;

    public Mono<ServerResponse> list(ServerRequest request) {
        return ServerResponse
                .ok()
                .body(postRepository.findAll(), Post.class);
    }

    public Mono<ServerResponse> save(ServerRequest request) {
        return request.bodyToMono(Post.class)
                .flatMap(post -> postRepository.save(post))
                .flatMap(post ->
                        ServerResponse.created(
                                URI.create("/posts/" + post.getId())
                        ).build()
                );
    }


    public Mono<ServerResponse> get(ServerRequest request) {
        return postRepository.findById(request.pathVariable("id"))
                .flatMap(post ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(post), Post.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }


    public Mono<ServerResponse> update(ServerRequest request) {
        return Mono.zip((data) -> {
                    // This two object from query DB and request body
                    Post originPost = (Post) data[0];
                    Post newPost = (Post) data[1];
                    originPost.setTitle(newPost.getTitle());
                    originPost.setContent(newPost.getContent());
                    return originPost;
                },
                postRepository.findById(request.pathVariable("id"))
                        .switchIfEmpty(Mono.error(new NotFoundException(request.pathVariable("id")))),
                request.bodyToMono(Post.class)
        )
                .cast(Post.class)
                .flatMap(post -> postRepository.save(post))
                .flatMap(post -> ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        return postRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException(id)))
                .then(postRepository.deleteById(id))
                .then(ServerResponse
                        .noContent()
                        .build(postRepository.deleteById(id)));
    }

}
