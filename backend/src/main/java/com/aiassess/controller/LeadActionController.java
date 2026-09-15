package com.aiassess.controller;

import com.aiassess.config.JwtUtil;
import com.aiassess.dto.Result;
import com.aiassess.entity.UserAction;
import com.aiassess.mapper.UserActionMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/**
 * 记录用户点击行为（试听/购买/咨询等）
 * POST /api/lead/action
 */
@Slf4j
@RestController
@RequestMapping("/api/lead")
@RequiredArgsConstructor
public class LeadActionController {

    private final UserActionMapper actionMapper;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @PostMapping("/action")
    public Result<Void> recordAction(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String openid = extractOpenidOptional(request);
        String actionType = (String) body.get("actionType");
        if (!StringUtils.hasText(actionType)) return Result.fail("actionType 不能为空");

        try {
            UserAction action = new UserAction();
            action.setOpenid(openid != null ? openid : "anonymous");
            action.setActionType(actionType);
            Map<String, Object> extra = new LinkedHashMap<>(body);
            extra.remove("actionType");
            if (!extra.isEmpty()) {
                action.setExtra(objectMapper.writeValueAsString(extra));
            }
            actionMapper.insert(action);
        } catch (Exception e) {
            log.warn("记录行为失败: {}", e.getMessage());
        }
        return Result.ok();
    }

    private String extractOpenidOptional(HttpServletRequest request) {
        try {
            String bearer = request.getHeader("Authorization");
            if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
                return jwtUtil.parseToken(bearer.substring(7)).getSubject();
            }
        } catch (Exception ignored) {}
        return null;
    }
}
