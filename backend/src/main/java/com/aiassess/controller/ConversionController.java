package com.aiassess.controller;

import com.aiassess.config.JwtUtil;
import com.aiassess.dto.Result;
import com.aiassess.entity.Lead;
import com.aiassess.entity.UserAction;
import com.aiassess.mapper.LeadMapper;
import com.aiassess.mapper.UserActionMapper;
import com.aiassess.service.SysConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/mini/conversion")
@RequiredArgsConstructor
public class ConversionController {

    private final SysConfigService configService;
    private final LeadMapper leadMapper;
    private final UserActionMapper actionMapper;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    // ── 8.1 资料领取 ─────────────────────────────────────────────────────

    /**
     * 获取资料包信息 + 老师微信配置（展示用，无需登录）
     */
    @GetMapping("/material/info")
    public Result<Map<String, Object>> getMaterialInfo() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("teacherWechat",   configService.get("teacher_wechat", "AI_Teacher_001"));
        data.put("teacherQrcode",   configService.get("teacher_qrcode_url", ""));
        data.put("materialList",    parseMaterialList());
        return Result.ok(data);
    }

    /**
     * 提交资料领取（收集手机号/微信号，记录领取行为）
     * token 可选：有登录则关联 openid，无登录也允许提交
     */
    @PostMapping("/material/claim")
    public Result<Map<String, Object>> claimMaterial(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String openid = extractOpenidOptional(request);
        String phone    = (String) body.get("phone");
        String wechat   = (String) body.get("wechat");
        String nickname = (String) body.get("nickname");

        if (!StringUtils.hasText(phone) && !StringUtils.hasText(wechat)) {
            return Result.fail("请填写手机号或微信号");
        }

        // 有 openid 才写 lead 表，匿名用户只记录行为
        if (StringUtils.hasText(openid)) {
            try {
                Lead lead = findOrCreateLead(openid, phone, wechat, nickname);
                lead.setMaterialClaimed(1);
                lead.setMaterialClaimedAt(LocalDateTime.now());
                if (StringUtils.hasText(phone))    lead.setPhone(phone);
                if (StringUtils.hasText(wechat))   lead.setWechat(wechat);
                if (StringUtils.hasText(nickname)) lead.setNickname(nickname);
                leadMapper.updateById(lead);
            } catch (Exception e) {
                log.warn("lead 写入失败: {}", e.getMessage());
            }
        }

        // 记录行为（表不存在时静默忽略）
        recordAction(openid, "claim_material",
            Map.of("phone",  phone  != null ? phone  : "",
                   "wechat", wechat != null ? wechat : ""));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("teacherWechat", configService.get("teacher_wechat", "AI_Teacher_001"));
        result.put("teacherQrcode", configService.get("teacher_qrcode_url", ""));
        result.put("materialList",  parseMaterialList());
        return Result.ok(result);
    }

    // ── 8.2 老师微信 / 学习群 ─────────────────────────────────────────────

    /**
     * 获取老师微信 + 群活码配置
     */
    @GetMapping("/contact/info")
    public Result<Map<String, Object>> getContactInfo() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("teacherWechat",  configService.get("teacher_wechat", "AI_Teacher_001"));
        data.put("teacherQrcode",  configService.get("teacher_qrcode_url", ""));
        data.put("groupQrcode",    configService.get("group_qrcode_url", ""));
        data.put("groupExpireTip", configService.get("group_expire_tip", "活码每7天更新，扫码加群即可"));
        return Result.ok(data);
    }

    /**
     * 记录用户点击行为（加微信/扫群码/复制微信号/保存二维码）
     */
    @PostMapping("/action/track")
    public Result<Void> trackAction(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String openid = extractOpenidOptional(request);
        String actionType = (String) body.get("actionType");
        if (!StringUtils.hasText(actionType)) return Result.fail("actionType 不能为空");

        recordAction(openid, actionType, body);
        return Result.ok();
    }

    // ── 8.3 试听课 / 购买 ────────────────────────────────────────────────

    /**
     * 获取课程配置（名称/价格/简介/链接/优惠文案）
     */
    @GetMapping("/course/info")
    public Result<Map<String, Object>> getCourseInfo() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("courseName",    configService.get("course_name",     "AI算法工程师系统课"));
        data.put("coursePrice",   configService.get("course_price",    "1299"));
        data.put("courseDiscount",configService.get("course_discount", "限时优惠价 ¥799"));
        data.put("courseDesc",    configService.get("course_desc",     "从Python基础到模型部署，30天掌握AI算法核心技能"));
        data.put("trialUrl",      configService.get("trial_url",       ""));
        data.put("buyUrl",        configService.get("buy_url",         ""));
        return Result.ok(data);
    }

    /**
     * 记录试听/购买点击（用于筛选高意向线索）
     */
    @PostMapping("/course/click")
    public Result<Void> courseClick(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        String openid = extractOpenidOptional(request);
        String type = (String) body.get("type"); // trial / buy
        recordAction(openid, "course_" + (type != null ? type : "unknown"), body);
        return Result.ok();
    }

    // ── 私有方法 ──────────────────────────────────────────────────────────

    private Lead findOrCreateLead(String openid, String phone, String wechat, String nickname) {
        Lead lead = leadMapper.selectOne(
            new LambdaQueryWrapper<Lead>().eq(Lead::getOpenid, openid).last("LIMIT 1"));
        if (lead == null) {
            lead = new Lead();
            lead.setOpenid(openid);
            lead.setPhone(phone);
            lead.setWechat(wechat);
            lead.setNickname(nickname);
            lead.setSource("miniprogram");
            lead.setFollowStatus(0);
            lead.setMaterialClaimed(0);
            leadMapper.insert(lead);
        }
        return lead;
    }

    private void recordAction(String openid, String actionType, Map<String, Object> extra) {
        try {
            UserAction action = new UserAction();
            action.setOpenid(openid != null ? openid : "anonymous");
            action.setActionType(actionType);
            // 过滤掉 actionType 字段本身，只存附加信息
            Map<String, Object> filtered = new LinkedHashMap<>(extra);
            filtered.remove("actionType");
            if (!filtered.isEmpty()) {
                action.setExtra(objectMapper.writeValueAsString(filtered));
            }
            actionMapper.insert(action);
        } catch (Exception e) {
            log.warn("记录用户行为失败: {}", e.getMessage());
        }
    }

    private List<String> parseMaterialList() {
        try {
            String raw = configService.get("material_list", "[]");
            return objectMapper.readValue(raw, List.class);
        } catch (Exception e) {
            return List.of("AI算法路线图PDF", "Python数据分析模板", "机器学习速查表",
                           "PyTorch模板", "面试题100道", "实战案例集");
        }
    }

    private String extractOpenid(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return jwtUtil.parseToken(bearer.substring(7)).getSubject();
        }
        throw new RuntimeException("未登录");
    }

    private String extractOpenidOptional(HttpServletRequest request) {
        try { return extractOpenid(request); } catch (Exception e) { return null; }
    }
}
