package com.aiassess.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** 留资请求 */
@Data
public class LeadRequest {
    private Long sessionId;
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    private String nickname;
    private String avatarUrl;
}
