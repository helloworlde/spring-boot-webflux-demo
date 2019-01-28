# 使用 Spring WebFlux 构建非阻塞应用 - MongoDB

WebFlux 构建的 REST 接口应用，使用 MongoDB 作为存储

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
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    private String id;

    private String title;

    private String content;

    @CreatedDate
    private LocalDateTime createDate;
    
}
```

- REST 接口 

```java
@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @GetMapping("")
    public Flux<Post> list() {
        return postRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Post> get(@PathVariable("id") String id) {
        return postRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException(id)));
    }

    @PutMapping("/{id}")
    public Mono<Post> update(@PathVariable("id") String id, @RequestBody Post post) {
        return postRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException(id)))
                .map(p -> {
                    p.setTitle(post.getTitle());
                    p.setContent(post.getContent());
                    p.setAuthor(post.getAuthor());
                    return p;
                })
                .flatMap(p -> postRepository.save(p));
    }

    @PostMapping("")
    public Mono<Post> save(@RequestBody Post post) {
        return postRepository.save(post);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable("id") String id) {
        return postRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException(id)))
                .then(postRepository.deleteById(id));
    }
}    
```

- Repository

```java
public interface PostRepository extends ReactiveMongoRepository<Post, String> {

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
  --data '{"title": "WebFlux","content": "Content of WebFlux"}'
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