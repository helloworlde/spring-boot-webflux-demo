package io.github.helloworlde.mongo.controller;

import com.alibaba.fastjson.JSON;
import io.github.helloworlde.mongo.MongoApplicationTests;
import io.github.helloworlde.mongo.model.Post;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.net.URI;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class PostControllerTest extends MongoApplicationTests {

    @Autowired
    private ApplicationContext context;

    private WebTestClient client;

    private final String POST_URI = "/posts";

    @Before
    public void setUp() throws Exception {
        String baseUrl = "http://localhost:8080";
        client = WebTestClient.bindToApplicationContext(context)
                .configureClient()
                .baseUrl(baseUrl)
                .build();
    }

    @Test
    public void list() {
        EntityExchangeResult<byte[]> result = client.get()
                .uri(POST_URI)
                .exchange()
                .expectStatus().isOk()
                .expectBody().returnResult();

        assertNotNull("Result body should not be null", result);
        assertNotNull("Result body content should not be null", result.getResponseBody());

        String body = new String(result.getResponseBody());
        List<Post> postList = JSON.parseArray(body, Post.class);

        assertNotNull("The result body should not be null", postList);
    }

    @Test
    public void get() {
        String title = "Get Title";
        String content = "Get Content";

        URI uri = saveAndValidatePost(title, content);
        assertNotNull("The uri should not be null", uri);

        getAndValidatePost(title, content, uri);
    }


    @Test
    public void update() {
        String title = "Update Title";
        String content = "Update Content";

        URI uri = saveAndValidatePost(title, content);

        Post post = getAndValidatePost(title, content, uri);

        String newTitle = "New Update Title";
        String newContent = "New Update Content";

        client.put()
                .uri(uri)
                .body(BodyInserters.fromObject(
                        Post.builder()
                                .id(post.getId())
                                .title(newTitle)
                                .content(newContent)
                                .build()
                ))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(newTitle)
                .jsonPath("$.content").isEqualTo(newContent)
                .jsonPath("$.id").isEqualTo(post.getId());
    }

    @Test
    public void save() {
        String title = "Save Title";
        String content = "Save Content";
        saveAndValidatePost(title, content);
    }

    @Test
    public void delete() {
        String title = "Delete Title";
        String content = "Delete Content";
        URI uri = saveAndValidatePost(title, content);

        client.delete()
                .uri(uri)
                .exchange()
                .expectStatus().isOk();

        client.get()
                .uri(uri)
                .exchange()
                .expectStatus().isNotFound();
    }

    private URI saveAndValidatePost(String title, String content) {
        FluxExchangeResult<Post> postResult = client.post()
                .uri(POST_URI)
                .body(BodyInserters.fromObject(
                        Post.builder()
                                .title(title)
                                .content(content)
                                .build()
                        )
                )
                .exchange()
                .expectStatus().isCreated()
                .returnResult(Post.class);

        assertNotNull("Result body should not be null", postResult);
        assertNotNull("Result body content should not be null", postResult.getResponseBody());

        final String[] postIdArr = new String[1];
        postResult.getResponseBody().subscribe(post -> postIdArr[0] = post.getId());

        return URI.create(POST_URI.concat("/").concat(postIdArr[0]));
    }

    private Post getAndValidatePost(String title, String content, URI uri) {
        EntityExchangeResult<byte[]> getResult = client.get()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(title)
                .jsonPath("$.content").isEqualTo(content)
                .returnResult();

        assertNotNull("Result body should not be null", getResult);
        assertNotNull("Result body content should not be null", getResult.getResponseBody());

        String postString = new String(getResult.getResponseBody());

        Post post = JSON.parseObject(postString, Post.class);
        assertNotNull("The object should not be null", post);

        return post;
    }
}
