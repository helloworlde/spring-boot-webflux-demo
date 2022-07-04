package io.github.helloworlde.router.repository;

import io.github.helloworlde.router.model.Post;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * @author HelloWood
 * @date 2019-01-10 16:49
 */
public interface PostRepository extends ReactiveMongoRepository<Post, String> {
}
