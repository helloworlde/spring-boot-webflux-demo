package io.github.helloworlde.streamecharts.util;

import io.github.helloworlde.streamecharts.model.OnlineAmount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;


/**
 * @author HelloWood
 * @date 2019-01-14 22:30
 */
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
