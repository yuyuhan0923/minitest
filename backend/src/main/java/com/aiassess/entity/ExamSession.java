package com.aiassess.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exam_session")
public class ExamSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String openid;
    private Integer totalScore;
    private Integer totalQuestions;
    private Integer correctCount;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
