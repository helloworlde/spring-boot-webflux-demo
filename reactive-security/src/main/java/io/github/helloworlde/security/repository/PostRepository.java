package io.github.helloworlde.security.repository;

import io.github.helloworlde.security.model.Post;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * @author HelloWood
 * @date 2019-01-08 15:21
 */
public interface PostRepository extends ReactiveMongoRepository<Post, String> {

}
