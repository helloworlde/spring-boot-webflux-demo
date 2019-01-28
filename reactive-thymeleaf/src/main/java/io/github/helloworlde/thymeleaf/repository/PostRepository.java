package io.github.helloworlde.thymeleaf.repository;

import io.github.helloworlde.thymeleaf.model.Post;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * @author HelloWood
 * @date 2019-01-08 14:22
 */
public interface PostRepository extends ReactiveMongoRepository<Post, String> {

}
