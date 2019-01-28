# 使用 Spring WebFlux 构建非阻塞应用 - Router

WebFlux 构建的 Router 接口应用，使用 MongoDB 作为存储

### 添加依赖 build.gradle 

```groovy
dependencies {
    implementation('org.springframework.boot:spring-boot-starter-webflux')
    implementation('org.springframework.boot:spring-boot-starter-data-mongodb-reactive')
    implementation('org.projectlombok:lombok')
    
    testImplementation('org.springframework.boot:spring-boot-starter-test')
    testImplementation('io.projectreactor:reactor-test')
}
```

### 添加接口 

- Model 

```java
@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Post {
    @Id
    private String id;

    private String title;

    private String content;

    @CreatedDate
    private LocalDateTime createDate;

}
```

- 路由

```java
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
```

- Repository

```java
public interface PostRepository extends ReactiveMongoRepository<Post, String> {

}
```

- Handler 

```java
@Component
@Slf4j
public class PostHandler {

    @Autowired
    private PostRepository postRepository;

    public Mono<ServerResponse> list(ServerRequest request) {
        return ServerResponse
                .ok()
                .body(postRepository.findAll(), Post.class);
    }

    public Mono<ServerResponse> save(ServerRequest request) {
        return request.bodyToMono(Post.class)
                .flatMap(post -> postRepository.save(post))
                .flatMap(post ->
                        ServerResponse.created(
                                URI.create("/posts/" + post.getId())
                        ).build()
                );
    }


    public Mono<ServerResponse> get(ServerRequest request) {
        return postRepository.findById(request.pathVariable("id"))
                .flatMap(post ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(post), Post.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }


    public Mono<ServerResponse> update(ServerRequest request) {
        return Mono.zip((data) -> {
                    // This two object from query DB and request body
                    Post originPost = (Post) data[0];
                    Post newPost = (Post) data[1];
                    originPost.setTitle(newPost.getTitle());
                    originPost.setContent(newPost.getContent());
                    return originPost;
                },
                postRepository.findById(request.pathVariable("id"))
                        .switchIfEmpty(Mono.error(new NotFoundException(request.pathVariable("id")))),
                request.bodyToMono(Post.class)
        )
                .cast(Post.class)
                .flatMap(post -> postRepository.save(post))
                .flatMap(post -> ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        return postRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException(id)))
                .then(postRepository.deleteById(id))
                .then(ServerResponse
                        .noContent()
                        .build(postRepository.deleteById(id)));
    }

}
```

- 初始化数据

```java
@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PostRepository postRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Start post data initialization ...");

        postRepository.deleteAll()
                .thenMany(
                        Flux.just("Post One", "Post Two")
                                .flatMap(title -> postRepository.save(
                                        Post.builder()
                                                .title(title)
                                                .content("Content of " + title)
                                                .build()
                                        )
                                )
                )
                .log()
                .subscribe(
                        null,
                        null,
                        () -> log.info("Done post data initialization ...")
                );
    }
}
```

### 添加配置

```properties
# MongoDB Config
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
#spring.data.mongodb.username=
#spring.data.mongodb.password=
spring.data.mongodb.database=blog
```

### 测试

- 启动应用 

#### 测试接口

- 获取列表

```bash
curl --request GET \
  --url http://localhost:8080/posts
```

- 获取

```bash
curl --request GET \
  --url http://localhost:8080/posts/5c4c49d50df00c4207b9453d
```

- 添加 

```bash
curl --request POST \
  --url http://localhost:8080/posts/ \
  --header 'Content-Type: application/json' \
  --data '{	"title": "WebFlux",	"content": "Content of WebFlux"}'
```

- 修改 
```bash
curl --request PUT \
  --url http://localhost:8080/posts/5c4c4aee0df00c4207b9453f \
  --header 'Content-Type: application/json' \
  --data '{"title": "Spring WebFlux","content": "Content of Spring WebFlux"}'
```

- 删除

```bash
curl --request DELETE \
  --url http://localhost:8080/posts/5c4c4aee0df00c4207b9453f
```