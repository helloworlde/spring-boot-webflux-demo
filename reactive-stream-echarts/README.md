# 使用 Spring WebFlux 开发 SSE(Server-Sent Events)应用 - Echarts 

WebFlux 构建的 Stream 接口应用，使用 Echarts 作为图表

- 效果
![效果演示](/img/reactive-stream-echarts-demo.gif)

和[reactive-stream](./reactive-stream/README.md)类似，区别是将图表由 HighCharts 换成了 Echarts，一次返回多行数据

## 使用 

- Clone 代码并编译

```bash
git clone https://github.com/helloworlde/SpringBootReactive.git
cd SpringBootReactive/reactive-stream-echarts/ && ./gradlew bootRun
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
    implementation('org.webjars.bower:echarts:4.1.0-release')

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
        return postGenerator
                .fetchPostStream(Duration.ofMillis(500))
                .share();
    }
}
```

- 生成数据

```java
@Slf4j
@Component
public class PostGenerator {

    private Random random = new Random();

    private final List<String> posts = Arrays.asList("Post One", "Post Two");

    private Instant instant = Instant.now();

    public Flux<OnlineAmount> fetchPostStream(Duration period) {

        return Flux.generate(() -> 0,
                (BiFunction<Integer, SynchronousSink<OnlineAmount>, Integer>) (index, sink) -> {
                    instant = instant.plus(1, ChronoUnit.SECONDS);
                    OnlineAmount onlineAmount = OnlineAmount.builder()
                            .postOneAmount(random.nextInt(40) % (40) + 80)
                            .postTwoAmount(random.nextInt(20) % (20) + 100)
                            .date(instant)
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
</head>
<body>
<div class="container wrapper">
    <div id="container" style="height: 600px; min-width: 310px"></div>
</div>
</body>
<script type="text/javascript" src="/webjars/echarts/4.1.0-release/dist/echarts.js"></script>
<script type="text/javascript">
    var dom = document.getElementById("container");
    var myChart = echarts.init(dom);
    option = null;

    var colors = ['#62e0e4', '#e78ed2', '#ff9f7c', '#9de7b7'];

    var dateData = [];
    var postOneAmountData = [];
    var postTwoAmountData = [];

    // Chart
    option = {
        color: colors,
        title: {
            text: 'Post Online Amount'
        },
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'cross',
                label: {
                    backgroundColor: '#6a7985'
                }
            }
        },
        legend: {
            data: ['Post One', 'Post Two']
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        toolbox: {
            feature: {
                magicType: {show: true, type: ['stack', 'tiled']},
                saveAsImage: {}
            }
        },
        xAxis: {
            type: 'category',
            boundaryGap: false,
            data: dateData
        },
        yAxis: {
            type: 'value'
        },
        series: [
            {
                name: 'Post One',
                type: 'line',
                smooth: true,
                stack: 'Total',
                data: postOneAmountData
            },
            {
                name: 'Post Two',
                type: 'line',
                smooth: true,
                stack: 'Total',
                data: postTwoAmountData
            }
        ]
    };


    if (option && typeof option === "object") {
        myChart.setOption(option, true);
    }

    // Get data from server by event stream
    var postEventSource = new EventSource("/posts/online/stream");
    postEventSource.onmessage = function (evt) {
        appendOnlineAmount(JSON.parse(evt.data));
    };

    postEventSource.onerror = function (evt) {
        if (postEventSource.readyState === 0) {
            console.log('The steam has been closed by the server');
            postEventSource.close();
        }
    };

    var appendOnlineAmount = function (onlineAmount) {
        // Only save 60 data
        if (dateData.length > 60) {
            dateData.shift();
            postOneAmountData.shift();
            postTwoAmountData.shift();
        }


        dateData.push(new Date(onlineAmount.date).toLocaleTimeString());
        postOneAmountData.push(onlineAmount.postOneAmount);
        postTwoAmountData.push(onlineAmount.postTwoAmount);

        // Update chart data
        myChart.setOption({
            xAxis: {
                data: dateData
            },
            series: [
                {
                    data: postOneAmountData
                },
                {
                    data: postTwoAmountData
                }
            ]
        });
    }
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