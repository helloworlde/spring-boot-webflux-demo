# 使用 Spring WebFlux 构建非阻塞应用 - 集成 Thymeleaf

使用 WebFlux 和 Thymeleaf 构建一个简单的增加 Post 和显示 Post 列表的页面

![Demo](/img/reactive-thymeleaf-demo.png)

### 添加依赖 build.gradle 

```groovy
dependencies {
    implementation('org.springframework.boot:spring-boot-starter-webflux')
    implementation('org.springframework.boot:spring-boot-starter-data-mongodb-reactive')
    implementation('org.springframework.boot:spring-boot-starter-thymeleaf')
    implementation('org.webjars:bootstrap:4.2.1')

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
@Slf4j
@Controller
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @GetMapping("")
    public String list(final Model model) {
        Flux<Post> posts = postRepository.findAll();
        model.addAttribute("posts", new ReactiveDataDriverContextVariable(posts));
        model.addAttribute("post", new Post());
        return "post";
    }

    @PostMapping("")
    public String save(@ModelAttribute("post") Post post) {
        postRepository.save(post).subscribe(p -> log.info("{}", p));
        return "redirect:/posts";
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

- 访问 [http://localhost:8080](http://localhost:8080)