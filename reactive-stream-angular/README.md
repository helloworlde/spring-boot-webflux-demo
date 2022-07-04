# 使用 Spring WebFlux 和 Angular 开发响应式应用

WebFlux 构建的 Stream 接口应用，前端使用Angular

> 如果使用阻塞式 API，当请求的数据较多时等待时间会很长，影响使用体验；如果使用非阻塞的 API，可以分多次加载数据，明显提升应用响应速度


- 效果

![效果演示](/img/reactive-stream-angular-demo.gif)


## 使用 

- Clone 代码并编译

```bash
git clone https://github.com/helloworlde/SpringBootReactive.git
cd SpringBootReactive/reactive-stream-angular/client/ && npm install && ng build && cd ../server/ && ./gradlew bootRun
```

- 访问 `http://localhost:8080`

---

## 创建服务端应用

### 添加依赖

```groovy
dependencies {
    implementation('org.springframework.boot:spring-boot-starter-webflux')
    implementation('org.springframework.boot:spring-boot-starter-data-mongodb-reactive')    
    implementation('org.projectlombok:lombok')
}    
```

### 添加接口

- 添加Model

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

- 添加 Repository

```java
public interface PostRepository extends ReactiveMongoRepository<Post, String> {

}
``` 

- 添加接口

对每一个数据延时500ms 后返回

```java
@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @GetMapping("/block")
    public Flux<Post> list() {
        return postRepository.findAll().delayElements(Duration.ofMillis(500));
    }

    @GetMapping(value = "/nonblock", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Post> online() {
        return postRepository.findAll().delayElements(Duration.ofMillis(500));
    }
}
```

### 修改配置

- application.properties

```properties
# MongoDB Config
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
#spring.data.mongodb.username=
#spring.data.mongodb.password=
spring.data.mongodb.database=blog
```

- CorsFilter.java

允许客户端进行跨域访问

```java
@Component
public class CorsFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        exchange.getResponse().getHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponse().getHeaders().add("Access-Control-Allow-Headers", "*");
        return chain.filter(exchange);
    }
}
```

- DataInitializer.java
初始化数据

```java
private void initPosts() {
        postRepository.deleteAll()
                .thenMany(
                        Flux.range(1, 10)
                                .flatMap(title -> postRepository.save(
                                        Post.builder()
                                                .title("Post " + title)
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

## 创建客户端应用

需要使用到 [Angular-Cli](https://cli.angular.io/) 

### 创建应用

```bash
ng new client
```

- 添加 Bootstrap 并引入

```bash
npm install bootstrap
```
在 `style.css` 中添加

```css
@import "~bootstrap";
```

- 添加 EventSourcePolyfill
Angular 新版本中如果直接使用 EventSource 异步加载数据到页面上会有问题，改用 EventSourcePolyfill 代替 EventSource

```bash
npm install ng-event-source
```

### 添加组件

#### Post Model

```typescript
export class Post {

  public id: string;
  public title: string;
  public content: string;
  public createDate: string;

  constructor(id: string, title: string, content: string, createDate: string) {
    this.id = id;
    this.title = title;
    this.content = content;
    this.createDate = createDate;
  }
}
```

#### Post List Component

- 添加组件

```bash
ng generate component post/post-list
```

#### Post Detail Component

- 添加组件

```bash
ng generate component post/post-detail
```

#### Post Service 

- 添加组件

```bash
ng generate service post/post
```

- post-service.ts

```typescript
  getPostNonBlock(): Observable<Array<Post>> {
    this.posts = [];

    return Observable.create((observer) => {
      const url = `${this.postUrl}/stream`;
      const eventSource = new EventSourcePolyfill(url, {
        heartbeatTimeout: 5000,
        connectionTimeout: 5000
      });

      eventSource.onmessage = evt => {
        this.posts.push(JSON.parse(evt.data));
        observer.next(this.posts);
      };

      eventSource.onerror = error => {
        if (eventSource.readyState === 0) {
          console.log('The steam has been closed by the server');
          eventSource.close();
          observer.complete();
        } else {
          observer.error('EventSource error ' + error);
        }
      };
    });
  }

  getPostBlocked(): Observable<Array<Post>> {
    return this.http.get<Array<Post>>(`${this.postUrl}`);
  }
```


#### 添加路由 
- app-routing.module.ts

```typescript
const routes: Routes = [
  {path: '', redirectTo: '/post', pathMatch: 'full'},
  {path: 'post', component: PostListComponent}
];
```

- environment.ts

```typescript
export const environment = {
  production: false,
  baseUrl: 'http://localhost:8080'
};
```

### 打包

- 修改 angular.json 文件，将编译后的文件添加到 SpringBoot 项目的 `src/main/resources/static/`目录下

```json
"outputPath": "../server/src/main/resources/static/"
```

---

### 问题

- EventSource 数据不显示问题

使用 EventSource 获取数据，页面加载完数据后没有显示；经过一系列对比之后，发现[full-reactive-stack(Angular 4.2.4)](https://github.com/mechero/full-reactive-stack)这个 Demo 在 service 有一个特殊的引入：
```typescript
import * as EventSource from 'eventsource';
```

在当前项目(Angular 7.2.0)里不需要引入就可以使用；这就是问题所在，如果直接使用 `EventSource`，异步加载数据的时候无法显示到页面上，在 [EventSource not working in angular 6 due to module http and https](https://stackoverflow.com/questions/52352532/eventsource-not-working-in-angular-6-due-to-module-http-and-https)找到了答案，需要将`EventSource`替换为 `EventSourcePolyfill`

```typescript
import {EventSourcePolyfill} from 'ng-event-source';

// ...

const eventSource = new EventSourcePolyfill(url, {
        heartbeatTimeout: 5000,
        connectionTimeout: 5000
      });
```


--- 

#### 参考文章 
- [full-reactive-stack](https://github.com/mechero/full-reactive-stack)
- [EventSource not working in angular 6 due to module http and https](https://stackoverflow.com/questions/52352532/eventsource-not-working-in-angular-6-due-to-module-http-and-https)
