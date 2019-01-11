package io.github.helloworlde.multiplemongo.controller;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

/**
 * @author HelloWood
 * @date 2019-01-10 21:37
 */

@RestController
@RequestMapping("/upload")
@Slf4j
public class MultipleController {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Flux<String> upload(@RequestBody Flux<Part> parts) {

        return parts
                .filter(part -> part instanceof FilePart)
                .ofType(FilePart.class)
                .flatMap(filePart -> {
                    String name = UUID.randomUUID().toString() + "-" + filePart.filename();
                    String contentType = filePart.headers().getContentType().toString();

                    // TODO This is not best method to get InputStream
                    File file = new File(filePart.filename());
                    filePart.transferTo(file);

                    InputStream inputStream = null;
                    try {
                        inputStream = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    ObjectId id = gridFsTemplate.store(inputStream, name, contentType);
                    return Mono.just(String.format("The upload file id is %s", id.toString()));
                });
    }
}
