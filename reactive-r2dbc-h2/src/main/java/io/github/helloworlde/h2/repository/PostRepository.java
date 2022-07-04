package io.github.helloworlde.h2.repository;

import io.github.helloworlde.h2.model.Post;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 * @author HelloWood
 * @date 2019-01-08 14:22
 */
public interface PostRepository extends ReactiveCrudRepository<Post, Long> {

}
