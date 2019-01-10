package io.github.helloworlde.mongo.repository;

import io.github.helloworlde.mongo.model.Post;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * @author HelloWood
 * @date 2019-01-08 14:22
 */
public interface PostRepository extends ReactiveMongoRepository<Post, String> {

}
