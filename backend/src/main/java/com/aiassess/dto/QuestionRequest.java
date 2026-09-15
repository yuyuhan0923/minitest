package com.aiassess.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 题目请求DTO */
@Data
public class QuestionRequest {
    @NotBlank(message = "题目内容不能为空")
    private String title;
    @NotBlank(message = "选项不能为空")
    private String optionsJson;
    @NotBlank(message = "正确答案不能为空")
    private String correctAnswer;
    @NotBlank(message = "题目分类不能为空")
    private String category;
    @NotNull(message = "分值不能为空")
    private Integer score;
    private Integer sortOrder = 0;
    private Integer status = 1;
}
