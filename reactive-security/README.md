# 使用 Spring WebFlux 构建非阻塞应用 - SpringSecurity 

WebFlux 构建的 REST 接口应用，使用 MongoDB 作为存储，添加了 Spring Security

## 创建应用

### 添加依赖 build.gradle 

```groovy
dependencies {
    implementation('org.springframework.boot:spring-boot-starter-webflux')
    implementation('org.springframework.boot:spring-boot-starter-data-mongodb-reactive')
    implementation('org.springframework.boot:spring-boot-starter-security')
    implementation('org.springframework.session:spring-session-data-mongodb')
    implementation('org.projectlombok:lombok')

    testImplementation('org.springframework.boot:spring-boot-starter-test')
    testImplementation('io.projectreactor:reactor-test')
    testImplementation('org.springframework.security:spring-security-test')    
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
                    return p;
                })
                .flatMap(p -> postRepository.save(p));
    }

    @PostMapping("")
    public Mono<Post> save(@RequestBody Post post) {
        return postRepository.save(post);
    }

    @DeleteMapping("/{id}")
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
private void initPosts() {
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

### 添加安全配置 

- User Model 

```java
@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document
public class User implements UserDetails {

    @Id
    private String id;

    private String username;

    @JsonIgnore
    private String password;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private List<String> roles = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList(roles.toArray(new String[0]));
    }

    @Override
    public boolean isAccountNonExpired() {
        return active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return active;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
```

- UserRepository.java

```java
public interface UserRepository extends ReactiveMongoRepository<User, String> {
    /**
     * Find user by username
     *
     * @param username the username
     * @return the User
     */
    Mono<User> findByUsername(String username);
}
```

- AuthController.java

```java
@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/user")
    public Mono<Map> current(@AuthenticationPrincipal Mono<Principal> principal) {
        return principal
                .map(user -> {
                    Map<String, Object> map = new HashMap<>(2);
                    map.put("name", user.getName());
                    map.put("roles", AuthorityUtils.authorityListToSet(((Authentication) user).getAuthorities()));
                    return map;
                });
    }

}
```

- SecurityConfig 

```java
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .httpBasic()
                .securityContextRepository(new WebSessionServerSecurityContextRepository())
                .and()
                .authorizeExchange()
                .pathMatchers(HttpMethod.GET, "/posts/**").permitAll()
                .pathMatchers(HttpMethod.DELETE, "/posts/**").hasRole("ADMIN")
                .pathMatchers("/post/**").authenticated()
                .pathMatchers("/auth/**").authenticated()
                .pathMatchers("/users/{user}/**").access(this::currentUserMatchesPath)
                .anyExchange().permitAll()
                .and()
                .csrf().disable()
                .build();
    }

    private Mono<AuthorizationDecision> currentUserMatchesPath(Mono<Authentication> authentication, AuthorizationContext authorizationContext) {
        return authentication
                .map(a -> authorizationContext.getVariables().get("user").equals(a.getName()))
                .map(AuthorizationDecision::new);
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(UserRepository userRepository) {
        return (username -> userRepository.findByUsername(username).cast(UserDetails.class));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
```

- 用户信息初始化

```java
private void initUsers() {
        log.info("Start user data initialization ...");
        userRepository.deleteAll()
                .thenMany(
                        Flux.just("user", "admin")
                                .flatMap(username -> {
                                            List<String> roles = "user".equals(username)
                                                    ? Arrays.asList("ROLE_USER")
                                                    : Arrays.asList("ROLE_USER", "ROLE_ADMIN");

                                            User user = User.builder()
                                                    .username(username)
                                                    .password(passwordEncoder.encode("password"))
                                                    .active(true)
                                                    .roles(roles)
                                                    .build();
                                            return userRepository.save(user);
                                        }
                                )
                )
                .log()
                .subscribe(
                        null,
                        null,
                        () -> log.info("Done user data initialization ..."));

    }
```

- 修改方法调用权限

```java
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Mono<Void> delete(@PathVariable("id") String id) {
        return postRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException(id)))
                .then(postRepository.deleteById(id));
    }
```

## 测试

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
  --url http://admin:password@localhost:8080/posts/5c4c4aee0df00c4207b9453f
```

- 获取当前用户信息

```bash
curl --request GET \
  --url http://user:password@localhost:8080/auth/user
```