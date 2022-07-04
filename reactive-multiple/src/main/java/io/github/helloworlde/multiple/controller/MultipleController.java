package io.github.helloworlde.multiple.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * @author HelloWood
 * @date 2019-01-10 21:37
 */

@RestController
@RequestMapping("/upload")
@Slf4j
public class MultipleController {

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Flux<String> upload(@RequestBody Flux<Part> parts) {
        return parts
                .filter(part -> part instanceof FilePart)
                .ofType(FilePart.class)
                .flatMap(filePart -> filePart.transferTo(new File(filePart.filename()))
                        .log()
                        .thenReturn(filePart.filename() + " Upload success"));
    }

}
