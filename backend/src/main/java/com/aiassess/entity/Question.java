package com.aiassess.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("`question`")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String optionsJson;
    private String correctAnswer;
    private String category;
    private Integer score;
    private Integer sortOrder;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
