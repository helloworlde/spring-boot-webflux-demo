package io.github.helloworlde.mongo.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * @author HelloWood
 * @date 2019-01-08 14:20
 */
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
