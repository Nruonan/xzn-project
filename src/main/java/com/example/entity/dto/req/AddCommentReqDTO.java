package com.example.entity.dto.req;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * @author Nruonan
 * @description
 */
@Data
public class AddCommentReqDTO {
    @Min(1)
    int tid;

    String content;
    @Min(-1)
    int quote;
    @Min(-1)
    int root;
}
