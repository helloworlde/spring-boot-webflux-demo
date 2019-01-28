# 使用 Spring WebFlux 开发 SSE(Server-Sent Events)应用 - Highcharts 

WebFlux 构建的 Stream 接口应用，使用 HighCharts 作为图表

- 效果
![效果演示](/img/reactive-stream-highcharts-demo.gif)

> SSE 是服务器向客户端声明，接下来要发送的是流信息(Streaming)；发送的不是一次性的数据包，而是一个数据流，会连续不断地发送过来，这时客户端不会关闭连接，会一直等着服务器发过来的新的数据流

> SSE 与 WebSocket 作用相似，都是建立浏览器和服务器之间的通信渠道，然后服务器向浏览器推送信息

> WebSocket 更强大灵活，因为是全双工通信，可以双向通信；SSE是单向通道，只能由服务器向浏览器发送，因为流的本质是下载

## 使用 

- Clone 代码并编译

```bash
git clone https://github.com/helloworlde/SpringBootReactive.git
cd SpringBootReactive/reactive-stream/ && ./gradlew bootRun
```

- 访问 `http://localhost:8080`

---

## 创建应用

### 添加依赖

```groovy
dependencies {
    implementation('org.springframework.boot:spring-boot-starter-webflux')
    implementation('org.springframework.boot:spring-boot-starter-data-mongodb-reactive')
    implementation('org.projectlombok:lombok')

    implementation('org.webjars:bootstrap:4.2.1')
    implementation('org.webjars:highcharts:7.0.1')

    testImplementation('org.springframework.boot:spring-boot-starter-test')
    testImplementation('io.projectreactor:reactor-test') 
}    
```

### 添加接口

- 添加Model

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OnlineAmount {
    private String name;

    private Integer amount;

    private Instant time;
}

```

- 添加接口

对每一个数据延时 500ms 后返回

```java
@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostGenerator postGenerator;

    @GetMapping(value = "/online/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<OnlineAmount> online() {
        return postGenerator.fetchPostStream(Duration.ofMillis(500))
                .share();
    }

}
```

- 生成数据

```java
@Component
public class PostGenerator {

    private Random random = new Random();

    private final List<String> posts = Arrays.asList("Post One", "Post Two", "Post Three", "Post Four", "Post Five", "Post Six");

    private Instant instant = Instant.now();

    public Flux<OnlineAmount> fetchPostStream(Duration period) {
        return Flux.generate(() -> 0,
                (BiFunction<Integer, SynchronousSink<OnlineAmount>, Integer>) (index, sink) -> {

                    instant = instant.plusSeconds(1);

                    OnlineAmount onlineAmount = OnlineAmount.builder()
                            .name(posts.get(index))
                            .amount(random.nextInt(100) % (101) + 100)
                            .time(instant)
                            .build();

                    sink.next(onlineAmount);
                    return ++index % posts.size();
                })
                .zipWith(Flux.interval(period))
                .map(Tuple2::getT1)
                .share()
                .log();
    }
}
```

- 页面 

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Spring WebFlux Streaming</title>
    <link rel="stylesheet" href="/webjars/bootstrap/4.2.1/css/bootstrap.min.css"/>
    <link rel="stylesheet" href="/webjars/highcharts/7.0.1/css/highcharts.css"/>
</head>
<body>
<div class="container wrapper">
    <div id="chart" style="height: 400px; min-width: 310px"></div>
</div>
</body>
<script type="text/javascript" src="/webjars/highcharts/7.0.1/highcharts.js"></script>
<script type="text/javascript">
    var chart = new Highcharts.chart('chart', {
        title: {
            text: 'Post Online Amount'
        },
        yAxis: {
            title: {
                text: 'Online Amount'
            }
        },
        legend: {
            layout: 'vertical',
            align: 'right',
            verticalAlign: 'middle'
        },
        xAxis: {
            type: 'datetime'
        },
        series: [{
            name: 'Post One',
            data: []
        }, {
            name: 'Post Two',
            data: []
        }, {
            name: 'Post Three',
            data: []
        }, {
            name: 'Post Four',
            data: []
        }, {
            name: 'Post Five',
            data: []
        }, {
            name: 'Post Six',
            data: []
        }]
    });
    var appendStockData = function (onlineAmount) {
        chart.series
            .filter(function (series) {
                return series.name === onlineAmount.name
            })
            .forEach(function (series) {
                var shift = series.data.length > 40;
                series.addPoint([new Date(onlineAmount.time).toLocaleTimeString(), onlineAmount.amount], true, shift);
            });
    };

    var stockEventSource = new EventSource("/posts/online/stream");
    stockEventSource.onmessage = function (e) {
        appendStockData(JSON.parse(e.data));
    };

    stockEventSource.onerror = function (evt) {
        if (stockEventSource.readyState === 0) {
            console.log('The steam has been closed by the server');
            stockEventSource.close();
        }
    };
</script>
</html>
```

### 修改配置

- CorsFilter.java

访问时跳转到 index 页面

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

- 访问 [http://localhost:8080](http://localhost:8080)

--- 

#### 参考文章 

- [Server-Sent Events 教程](http://www.ruanyifeng.com/blog/2017/05/server-sent_events.html)
