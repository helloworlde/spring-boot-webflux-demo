package io.github.helloworlde.stream.angular.repository;

import io.github.helloworlde.stream.angular.model.Post;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * @author HelloWood
 * @date 2019-01-08 14:22
 */
public interface PostRepository extends ReactiveMongoRepository<Post, String> {

}
