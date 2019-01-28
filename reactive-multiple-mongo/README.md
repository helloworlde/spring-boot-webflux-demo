# 使用 Spring WebFlux REST 接口上传文件并保存到 MongoDB 中

WebFlux 构建的文件上传应用，使用REST 接口，将文件保存到 MongoDB

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

- REST 接口 

```java
@RestController
@RequestMapping("/upload")
@Slf4j
public class MultipleController {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Flux<String> upload(@RequestBody Flux<Part> parts) {

        return parts
                .filter(part -> part instanceof FilePart)
                .ofType(FilePart.class)
                .flatMap(filePart -> {
                    String name = UUID.randomUUID().toString() + "-" + filePart.filename();
                    String contentType = filePart.headers().getContentType().toString();

                    File file = new File(filePart.filename());
                    filePart.transferTo(file);

                    InputStream inputStream = null;
                    try {
                        inputStream = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    ObjectId id = gridFsTemplate.store(inputStream, name, contentType);
                    return Mono.just(String.format("The upload file id is %s", id.toString()));
                });
    }
}
```

- 添加上传页面

index.html

```html
<form enctype="multipart/form-data" action="/upload" method="post">
    <input type="file" name="uploadFile">
    <br>
    <button type="submit">Submit</button>
</form>

```

### 添加配置

- CustomWebFilter.java 访问时跳转到首页

```java
@Component
public class CustomWebFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if ("/".equals(exchange.getRequest().getURI().getPath())) {
            return chain.filter(
                    exchange.mutate()
                            .request(
                                    exchange.getRequest()
                                            .mutate()
                                            .path("/index.html")
                                            .build()
                            )
                            .build()
            );
        }
        return chain.filter(exchange);
    }
}
```

### 测试

- 启动应用 

- 访问 [http://localhost:8080](http://localhost:8080)，上传文件成功后可以在 MongoDB 中看到这个文件

![文件上传到 MongoDB](/img/reactive-multiple-mongo.png)
