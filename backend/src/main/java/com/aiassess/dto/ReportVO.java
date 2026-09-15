package com.aiassess.dto;

import lombok.Data;
import java.util.List;

/** 测评报告响应 */
@Data
public class ReportVO {
    private Long sessionId;
    private Integer totalScore;
    private Integer maxScore;
    private String level;
    private String levelCode;
    private String summary;
    private List<CategoryScore> categoryScores;
    private List<String> suggestions;
    private Boolean hasLead;

    @Data
    public static class CategoryScore {
        private String category;
        private Integer score;
        private Integer total;
        private Integer percentage;
    }
}
