package io.github.helloworlde.router.router;

import io.github.helloworlde.router.handler.PostHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * @author HelloWood
 * @date 2019-01-10 17:11
 */
@Component
public class RouterConfig {

    @Bean
    public RouterFunction<ServerResponse> routes(PostHandler postHandler) {
        return route(GET("/posts"), postHandler::list)
                .andRoute(POST("/posts").and(contentType(MediaType.APPLICATION_JSON)), postHandler::save)
                .andRoute(GET("/posts/{id}"), postHandler::get)
                .andRoute(PUT("/posts/{id}").and(contentType(MediaType.APPLICATION_JSON)), postHandler::update)
                .andRoute(DELETE("/posts/{id}"), postHandler::delete);
    }
}
