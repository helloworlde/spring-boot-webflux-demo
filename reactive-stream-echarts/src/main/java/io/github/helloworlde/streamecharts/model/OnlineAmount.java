package io.github.helloworlde.streamecharts.model;

import lombok.*;

import java.time.Instant;

/**
 * @author HelloWood
 * @date 2019-01-14 22:36
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OnlineAmount {

    private Integer postOneAmount;

    private Integer postTwoAmount;

    private Instant date;
}
