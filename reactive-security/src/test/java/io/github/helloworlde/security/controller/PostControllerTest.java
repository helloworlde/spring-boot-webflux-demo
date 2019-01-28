package io.github.helloworlde.security.controller;

import com.alibaba.fastjson.JSON;
import io.github.helloworlde.security.SecurityApplicationTests;
import io.github.helloworlde.security.model.Post;
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
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;

public class PostControllerTest extends SecurityApplicationTests {

    @Autowired
    private ApplicationContext context;

    private WebTestClient client;

    private final String POST_URI = "/posts";
    private final String AUTHORIZATION_KEY = "Authorization";
    private final String AUTHORIZATION_ADMIN = "Basic YWRtaW46cGFzc3dvcmQ=";

    @Before
    public void setUp() throws Exception {
        String baseUrl = "http://localhost:8080";
        client = WebTestClient
                .bindToApplicationContext(context)
                .configureClient()
                .baseUrl(baseUrl)
                .build();
    }

    @Test
    public void list() {
        FluxExchangeResult<Post> result = client.get()
                .uri(POST_URI)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Post.class);

        assertNotNull("Result body should not be null", result);
        assertNotNull("Result body content should not be null", result.getResponseBody());

        List<Post> postList = result.getResponseBody().toStream().collect(Collectors.toList());
        assertNotNull("Post list should not be null", postList);
    }

    @Test
    public void get() {
        String title = "Get Title";
        String content = "Get Content";

        URI uri = saveAndValidatePost(title, content);

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
                                .title(newTitle)
                                .content(newContent)
                                .build()
                        )
                )
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
                .expectStatus().isUnauthorized();

        client.delete()
                .uri(uri)
                .header(AUTHORIZATION_KEY, AUTHORIZATION_ADMIN)
                .exchange().expectStatus().isOk();

        client.get()
                .uri(uri)
                .exchange()
                .expectStatus().isNotFound();
    }


    private URI saveAndValidatePost(String title, String content) {
        FluxExchangeResult<Post> result = client.post()
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

        assertNotNull("Result body should not be null", result);
        assertNotNull("Result body content should not be null", result.getResponseBody());

        final String[] postIdArr = new String[1];
        result.getResponseBody().subscribe(post -> postIdArr[0] = post.getId());

        return URI.create(POST_URI.concat("/").concat(postIdArr[0]));
    }

    private Post getAndValidatePost(String title, String content, URI uri) {
        EntityExchangeResult<byte[]> result = client.get()
                .uri(uri)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo(title)
                .jsonPath("$.content").isEqualTo(content)
                .returnResult();


        assertNotNull("Result body should not be null", result);
        assertNotNull("Result body content should not be null", result.getResponseBody());

        String postString = new String(result.getResponseBody());

        Post post = JSON.parseObject(postString, Post.class);
        assertNotNull("The object should not be null", post);
        return post;
    }
}