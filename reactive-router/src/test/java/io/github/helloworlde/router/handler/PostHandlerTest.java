package io.github.helloworlde.router.handler;

import com.alibaba.fastjson.JSON;
import io.github.helloworlde.router.ReactiveRouterApplicationTests;
import io.github.helloworlde.router.model.Post;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.net.URI;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author HelloWood
 * @date 2019-01-10 18:08
 */
public class PostHandlerTest extends ReactiveRouterApplicationTests {

    @Autowired
    ApplicationContext context;

    WebTestClient client;

    /**
     * Set configuration
     *
     * @throws Exception throw exception when failed
     */
    @Before
    public void setUp() throws Exception {
        client = WebTestClient
                .bindToApplicationContext(context)
                .configureClient()
                .baseUrl("http://locahost:8080/")
                .build();
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test list interface
     */
    @Test
    public void list() {
        client.get()
                .uri("/posts/")
                .exchange()
                .expectStatus().isOk();
    }

    /**
     * Test save interface
     */
    @Test
    public void save() {
        String title = "Test Post interface";
        String content = "Content of Test Post interface";

        URI location = saveAndValidatePost(title, content);

        getAndValidatePost(title, content, location);
    }

    /**
     * Test get interface
     */
    @Test
    public void get() {
        String title = "Test Get interface";
        String content = "Content of Test Get interface";

        URI location = saveAndValidatePost(title, content);

        getAndValidatePost(title, content, location);


        client.get()
                .uri("/posts/ABC")
                .exchange()
                .expectStatus().isNotFound();
    }


    /**
     * Test update interface
     */
    @Test
    public void update() {
        String title = "Test Update interface";
        String content = "Content of Test Update interface";

        URI location = saveAndValidatePost(title, content);

        String newTitle = "Test Update interface";
        String newContent = "Content of Test Update interface";


        client.put()
                .uri(location)
                .body(BodyInserters.fromObject(
                        Post.builder()
                                .title(newTitle)
                                .content(newContent)
                                .build()
                        )
                )
                .exchange()
                .expectStatus().isNoContent();

        getAndValidatePost(title, content, location);
    }

    /**
     * Test delete interface
     */
    @Test
    public void delete() {
        String title = "Test Update interface";
        String content = "Content of Test Update interface";

        URI location = saveAndValidatePost(title, content);

        client.delete()
                .uri(location)
                .exchange()
                .expectStatus().isNoContent();

        client.get()
                .uri(location)
                .exchange()
                .expectStatus().isNotFound();
    }


    /**
     * Save and validate save post interface
     *
     * @param title   the post title
     * @param content the post content
     * @return the URI of saved post
     */
    private URI saveAndValidatePost(String title, String content) {
        FluxExchangeResult<Void> postResult = client.post()
                .uri("/posts")
                .body(BodyInserters.fromObject(
                        Post.builder()
                                .title(title)
                                .content(content)
                                .build()
                        )
                )
                .exchange()
                .expectStatus().isCreated()
                .returnResult(Void.class);

        URI location = postResult.getResponseHeaders().getLocation();

        assertNotNull("Result should not be null if create success", location);

        return location;
    }

    /**
     * Get and validate the post by URI
     *
     * @param title    the expected title
     * @param content  the expected content
     * @param location the URI of saved post
     * @return the post entity get by URI
     */
    private Post getAndValidatePost(String title, String content, URI location) {
        EntityExchangeResult<byte[]> getResult = client
                .get()
                .uri(location)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.title").isEqualTo(title)
                .jsonPath("$.content").isEqualTo(content)
                .returnResult();
        assertNotNull("Result body should not be null", getResult);

        assertNotNull("Result body content should not be null", getResult.getResponseBody());

        String getPost = new String(getResult.getResponseBody());

        assertTrue("Result body should contains title", getPost.contains(title));

        return JSON.parseObject(getPost, Post.class);
    }

}