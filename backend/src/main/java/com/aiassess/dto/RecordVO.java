package com.aiassess.dto;

import lombok.Data;
import java.time.LocalDateTime;

/** 测评记录列表条目 */
@Data
public class RecordVO {
    private Long sessionId;
    private Integer totalScore;
    private Integer maxScore;
    private String level;
    private String levelCode;
    private LocalDateTime finishedAt;
    private String examType;   // "AI能力测评"
}
