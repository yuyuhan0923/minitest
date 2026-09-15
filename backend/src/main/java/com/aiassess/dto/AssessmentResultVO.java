package com.aiassess.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class AssessmentResultVO {
    private int totalScore;
    private int maxScore;
    private String level;
    private String levelDesc;
    private List<String> recommendations;
    private List<CategoryScore> categoryScores;

    @Data
    @Builder
    public static class CategoryScore {
        private String category;
        private int score;
        private int maxScore;
    }
}
