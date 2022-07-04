# 使用 Spring WebFlux 构建非阻塞应用 - PostgreSQL

WebFlux 构建的 REST 接口应用，使用 PostgreSQL 作为存储

### 添加依赖 build.gradle 

- 目前WebFlux对关系型数据库支持不够完整，使用[r2dbc](https://github.com/spring-projects/spring-data-r2dbc)作为 DAO

```groovy
dependencies {
    implementation('org.springframework.boot:spring-boot-starter-parent:2.1.3.RELEASE')
    implementation('org.springframework.boot:spring-boot-starter-webflux')
    implementation('org.springframework.data:spring-data-r2dbc:1.0.0.M1')
    implementation('io.r2dbc:r2dbc-postgresql:1.0.0.M6')
    testImplementation('org.springframework.boot:spring-boot-starter-test')
    testImplementation('io.projectreactor:reactor-test')
}
```

### 添加接口 

- Model 

需要注意的是 `@Table("post.post")`需要指定 PostgreSQL 的 schema 名称

```java
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("post.post")
public class Post {

    @Id
    private Long id;

    private String title;

    private String content;

    @CreatedDate
    private Date createDate;
}

```

- REST 接口 

```java
@RestController
@RequestMapping("/posts")
@Slf4j
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @GetMapping
    public Flux<Post> list() {
        return postRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Post> get(@PathVariable("id") Long id) {
        return postRepository.findById(id)
                             .switchIfEmpty(Mono.error(new NotFoundException(String.valueOf(id))));
    }

    @PutMapping("/{id}")
    public Mono<Post> update(@PathVariable("id") Long id, @RequestBody Post post) {
        return postRepository.findById(id)
                             .switchIfEmpty(Mono.error(new NotFoundException(String.valueOf(id))))
                             .map(p -> {
                                 p.setTitle(post.getTitle());
                                 p.setContent(post.getContent());
                                 return p;
                             })
                             .flatMap(p -> postRepository.save(p));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Post> save(@RequestBody Post post) {
        // Couldn't get id in current time after save
        return postRepository.save(post);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable("id") Long id) {
        return postRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException(String.valueOf(id))))
                .then(postRepository.deleteById(id));
    }
}
```

- Repository

```java
public interface PostRepository extends ReactiveCrudRepository<Post, Long> {

}
```

- 初始化数据

脚本分别是 schema.sql 和 data.sql，因为 r2dbc 暂不支持自动执行，需要手动执行

### 添加配置

- application.properties

```properties
spring.datasource.host=localhost
spring.datasource.port=5432
spring.datasource.database=post
spring.datasource.username=root
spring.datasource.password=password
```

- DatabaseConfig.java

r2dbc暂时需要手动注入数据源的 Bean

```java
@Configuration
public class DatabaseConfig extends AbstractR2dbcConfiguration {

    @Value("${spring.datasource.host}")
    private String host;

    @Value("${spring.datasource.port}")
    private Integer port;

    @Value("${spring.datasource.database}")
    private String database;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Override
    @Bean
    public ConnectionFactory connectionFactory() {
        PostgresqlConnectionConfiguration configuration = PostgresqlConnectionConfiguration.builder()
                                                                                           .host(host)
                                                                                           .port(port)
                                                                                           .database(database)
                                                                                           .username(username)
                                                                                           .password(password)
                                                                                           .build();
        return new PostgresqlConnectionFactory(configuration);
    }
}
```

- 开启 r2dbc 支持

需要注意的是 `@EnableR2dbcRepositories` 暂时必须放在 Application 的入口类，否则会提示 Repository 的 Bean 注入失败

```java
@SpringBootApplication
@EnableR2dbcRepositories
public class PostgreApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostgreApplication.class, args);
    }

}
```

### 测试

- 启动应用 

#### 测试接口

- 获取列表

```bash
curl --request GET \
  --url http://localhost:8080/posts
```

- 获取指定 id

```bash
curl --request GET \
  --url http://localhost:8080/posts/1
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
  --url http://localhost:8080/posts/1 \
  --header 'Content-Type: application/json' \
  --data '{"title": "Spring WebFlux","content": "Content of Spring WebFlux"}'
```

- 删除

```bash
curl --request DELETE \
  --url http://localhost:8080/posts/1
```