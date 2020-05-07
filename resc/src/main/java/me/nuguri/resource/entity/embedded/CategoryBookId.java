package me.nuguri.resource.entity.embedded;


import lombok.*;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"categoryId", "bookId"})
public class CategoryBookId implements Serializable {

    private Long categoryId;

    private Long bookId;

}
