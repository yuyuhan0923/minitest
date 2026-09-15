package com.aiassess.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exam_answer")
public class ExamAnswer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private Long questionId;
    private String selectedOption;
    private String correctAnswer;
    private Integer isCorrect;
    private Integer score;
    private String category;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
