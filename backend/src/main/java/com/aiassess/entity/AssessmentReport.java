package com.aiassess.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("assessment_report")
public class AssessmentReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private String openid;
    private Integer totalScore;
    private String level;
    private String levelCode;
    private String summary;
    private String categoryScoresJson;
    private String suggestionsJson;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
