# 使用 Spring WebFlux Router 路由上传文件

WebFlux 构建的文件上传应用，使用 Router 接口

### 添加依赖 build.gradle 

```groovy
dependencies {
    implementation('org.springframework.boot:spring-boot-starter-webflux')
    implementation('org.projectlombok:lombok')

    testImplementation('org.springframework.boot:spring-boot-starter-test')
    testImplementation('io.projectreactor:reactor-test')
}
```

### 添加接口 

- 路由

```java
@Configuration
public class MultipleRouter {

    @Bean
    public RouterFunction<ServerResponse> routes(MultipleHandler multipleHandler) {
        return route(POST("/upload"), multipleHandler::upload);
    }
}

```

- Handler

```java
@Component
@Slf4j
public class MultipleHandler {
    public Mono<ServerResponse> upload(ServerRequest request) {
        return request
                .body(BodyExtractors.toMultipartData())
                .log()
                .flatMap(parts -> Mono.just((FilePart) parts.toSingleValueMap().get("uploadFile")))
                .flatMap(filePart -> filePart.transferTo(new File(filePart.filename())))
                .flatMap(response -> ServerResponse.ok().body(BodyInserters.fromObject("Upload success")));
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

- 访问 [http://localhost:8080](http://localhost:8080)，上传文件成功后可以在当前目录下看到文件
