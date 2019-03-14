package io.github.helloworlde.h2.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

/**
 * @author HelloWood
 * @date 2019-01-08 14:20
 */
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("post")
public class Post {

    @Id
    private Long id;

    private String title;

    private String content;

    @CreatedDate
    private Date createDate;
}
