package com.aiassess.dto;

import lombok.Data;
import java.util.Map;

@Data
public class AssessmentSubmitRequest {
    /** key: 题目编号(1-18)，value: 选项字母 A/B/C/D */
    private Map<Integer, String> answers;
    /** 可选，关联用户 openid（从 token 中取，此字段备用） */
    private String openid;
}
