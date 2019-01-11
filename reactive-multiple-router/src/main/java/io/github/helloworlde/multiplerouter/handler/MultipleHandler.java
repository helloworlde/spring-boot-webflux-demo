package io.github.helloworlde.multiplerouter.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.File;

/**
 * @author HelloWood
 * @date 2019-01-11 17:35
 */

@Component
@Slf4j
public class MultipleHandler {
    public Mono<ServerResponse> upload(ServerRequest request) {
        return request
                .body(BodyExtractors.toMultipartData())
                .log()
                .flatMap(parts -> Mono.just((FilePart) parts.toSingleValueMap().get("uploadFile")))
                .flatMap(filePart -> filePart.transferTo(new File(filePart.filename())))
                .flatMap(response -> ServerResponse.ok().body(BodyInserters.fromObject("Upload success")));
    }
}