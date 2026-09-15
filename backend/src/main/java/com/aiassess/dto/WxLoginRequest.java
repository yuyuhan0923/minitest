package com.aiassess.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/** 微信登录请求 */
@Data
public class WxLoginRequest {
    @NotBlank(message = "code不能为空")
    private String code;
    private String nickname;
    private String avatarUrl;
}
