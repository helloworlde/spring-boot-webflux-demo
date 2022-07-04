# 使用 Spring WebFlux 构建非阻塞应用 - Redis

WebFlux 构建的 REST 接口应用，使用 Redis 作为存储

### 添加依赖 build.gradle 

```groovy
dependencies {
    implementation('org.springframework.boot:spring-boot-starter-webflux')
    implementation('org.springframework.boot:spring-boot-starter-data-redis-reactive')
    implementation('org.projectlombok:lombok')
    
    testImplementation('org.springframework.boot:spring-boot-starter-test')
    testImplementation('io.projectreactor:reactor-test')
}
```

### 添加接口 

- Model 

```java
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("posts")
public class Post {

    @Id
    private String id;

    private String title;

    private String content;

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
    public Flux<Iterable<Post>> list() {
        return Flux.just(postRepository.findAll());
    }

}  
```

- Repository

```java
public interface PostRepository extends KeyValueRepository<Post, String> {

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
        postRepository.deleteAll();
        Stream.of("Post One", "Post Two")
                .forEach(title -> postRepository.save(
                        Post.builder()
                                .id(UUID.randomUUID().toString())
                                .title(title)
                                .content("Content of " + title)
                                .build()
                ));
    }
}

```

### 添加配置

```properties
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=123456
```

### 测试

- 启动应用 

#### 测试接口

- 获取列表

```bash
curl --request GET \
  --url http://localhost:8080/posts
```