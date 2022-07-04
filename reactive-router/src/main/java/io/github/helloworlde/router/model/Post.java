package io.github.helloworlde.router.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * @author HelloWood
 * @date 2019-01-10 16:47
 */
@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Post {
    @Id
    private String id;

    private String title;

    private String content;

    @CreatedDate
    private LocalDateTime createDate;

}
