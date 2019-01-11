package io.github.helloworlde.multiplerouter.router;

import io.github.helloworlde.multiplerouter.handler.MultipleHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * @author HelloWood
 * @date 2019-01-11 17:34
 */
@Configuration
public class MultipleRouter {

    @Bean
    public RouterFunction<ServerResponse> routes(MultipleHandler multipleHandler) {
        return route(POST("/upload"), multipleHandler::upload);
    }
}
