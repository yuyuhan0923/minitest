package com.aiassess.dto;

import lombok.Data;
import java.util.List;

/** 提交答题请求 */
@Data
public class SubmitExamRequest {
    private Long sessionId;
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        private Long questionId;
        private String selectedOption;
    }
}
