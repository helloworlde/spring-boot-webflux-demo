package io.github.helloworlde.redis.repository;

import io.github.helloworlde.redis.model.Post;
import org.springframework.data.keyvalue.repository.KeyValueRepository;

/**
 * @author HelloWood
 * @date 2019-01-08 14:22
 */
public interface PostRepository extends KeyValueRepository<Post, String> {

}
