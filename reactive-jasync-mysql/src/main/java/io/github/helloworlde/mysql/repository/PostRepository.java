package io.github.helloworlde.mysql.repository;

import io.github.helloworlde.mysql.model.Post;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 * @author HelloWood
 * @date 2019-01-08 14:22
 */
public interface PostRepository extends ReactiveCrudRepository<Post, Long> {

}
