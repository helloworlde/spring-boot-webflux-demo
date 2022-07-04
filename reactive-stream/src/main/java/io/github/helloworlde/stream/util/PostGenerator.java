package io.github.helloworlde.stream.util;

import io.github.helloworlde.stream.model.OnlineAmount;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;


/**
 * @author HelloWood
 * @date 2019-01-14 22:30
 */
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
