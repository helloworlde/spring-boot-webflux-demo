package io.github.helloworlde.security.repository;

import io.github.helloworlde.security.model.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

/**
 * @author HelloWood
 * @date 2019-01-08 16:38
 */
public interface UserRepository extends ReactiveMongoRepository<User, String> {
    /**
     * Find user by username
     *
     * @param username the username
     * @return the User
     */
    Mono<User> findByUsername(String username);
}
