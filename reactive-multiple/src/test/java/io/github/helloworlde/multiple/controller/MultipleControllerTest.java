package io.github.helloworlde.multiple.controller;

import io.github.helloworlde.multiple.MultipleApplicationTests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.util.Objects;


/**
 * @author HelloWood
 * @date 2019-01-11 16:40
 */
public class MultipleControllerTest extends MultipleApplicationTests {

    @Autowired
    ApplicationContext context;

    private WebTestClient client;

    private File file;

    @Before
    public void setUp() throws Exception {
        client = WebTestClient.bindToApplicationContext(context)
                .configureClient()
                .baseUrl("http://localhost:8080/")
                .build();

        file = new File("TestUploadFile.txt");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void upload() {

        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("fileName", Objects.requireNonNull(file).getName());

        client.post()
                .uri("/upload")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .syncBody(multiValueMap)
                .exchange()
                .expectStatus().isOk();
    }
}