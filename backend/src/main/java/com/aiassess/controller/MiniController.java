package com.aiassess.controller;

import com.aiassess.dto.*;
import com.aiassess.entity.*;
import com.aiassess.mapper.AssessmentReportMapper;
import com.aiassess.mapper.LeadMapper;
import com.aiassess.service.AssessmentService;
import com.aiassess.service.ExamService;
import com.aiassess.service.WechatService;
import com.aiassess.config.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/mini")
@RequiredArgsConstructor
public class MiniController {

    private final WechatService wechatService;
    private final ExamService examService;
    private final LeadMapper leadMapper;
    private final AssessmentReportMapper reportMapper;
    private final JwtUtil jwtUtil;
    private final AssessmentService assessmentService;
    private final ObjectMapper objectMapper;

    /** 微信登录 */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody WxLoginRequest req) {
        String openid = wechatService.code2Session(req.getCode());
        String token = jwtUtil.generateToken(0L, openid, "user");
        return Result.ok(Map.of("token", token, "openid", openid));
    }

    /** 获取题目列表（不含正确答案） */
    @GetMapping("/questions")
    public Result<List<Map<String, Object>>> getQuestions() {
        List<Question> questions = examService.getQuestions();
        List<Map<String, Object>> result = questions.stream().map(q -> {
            Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("id", q.getId());
            item.put("title", q.getTitle());
            item.put("optionsJson", q.getOptionsJson());
            item.put("category", q.getCategory());
            item.put("score", q.getScore());
            item.put("sortOrder", q.getSortOrder());
            return item;
        }).toList();
        return Result.ok(result);
    }

    /** 开始答题 */
    @PostMapping("/exam/start")
    public Result<ExamSession> startExam(HttpServletRequest request) {
        String openid = extractOpenid(request);
        ExamSession session = examService.startExam(openid);
        return Result.ok(session);
    }

    /** 提交答题 */
    @PostMapping("/exam/submit")
    public Result<ReportVO> submitExam(@RequestBody SubmitExamRequest req, HttpServletRequest request) {
        String openid = extractOpenid(request);
        ReportVO report = examService.submitExam(openid, req);
        return Result.ok(report);
    }

    /** 获取测评报告 */
    @GetMapping("/report/{sessionId}")
    public Result<ReportVO> getReport(@PathVariable Long sessionId, HttpServletRequest request) {
        String openid = extractOpenid(request);
        ReportVO report = examService.buildReportVO(sessionId, openid);
        return Result.ok(report);
    }

    /** 提交留资 */
    @PostMapping("/lead")
    public Result<Void> submitLead(@Valid @RequestBody LeadRequest req, HttpServletRequest request) {
        String openid = extractOpenid(request);
        Lead lead = new Lead();
        lead.setOpenid(openid);
        lead.setSessionId(req.getSessionId());
        lead.setPhone(req.getPhone());
        lead.setNickname(req.getNickname());
        lead.setAvatarUrl(req.getAvatarUrl());
        lead.setSource("miniprogram");
        lead.setFollowStatus(0);

        // 获取报告信息冗余存储
        if (req.getSessionId() != null) {
            try {
                ReportVO report = examService.buildReportVO(req.getSessionId(), openid);
                lead.setLevel(report.getLevel());
                lead.setTotalScore(report.getTotalScore());
            } catch (Exception ignored) {}
        }

        leadMapper.insert(lead);
        return Result.ok();
    }

    private String extractOpenid(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            String token = bearer.substring(7);
            return jwtUtil.parseToken(token).getSubject();
        }
        throw new RuntimeException("未登录");
    }

    private String toLevelCode(String level) {
        if (level == null) return "beginner";
        if (level.contains("入门")) return "beginner";
        if (level.contains("基础")) return "basic";
        if (level.contains("进阶")) return "intermediate";
        if (level.contains("实战")) return "advanced";
        if (level.contains("提升")) return "expert";
        return "beginner";
    }

    // ── AI能力测评接口 ────────────────────────────────────────────────────

    /** 获取测评题目列表 */
    @GetMapping("/assessment/questions")
    public Result<List<AssessmentService.AssessmentQuestion>> getAssessmentQuestions() {
        return Result.ok(assessmentService.getQuestions());
    }

    /** 提交测评答案，返回得分、等级、建议；有 token 时将结果持久化到 assessment_report */
    @PostMapping("/assessment/submit")
    public Result<AssessmentResultVO> submitAssessment(
            @RequestBody AssessmentSubmitRequest req,
            HttpServletRequest request) {
        String openid = null;
        try { openid = extractOpenid(request); } catch (Exception ignored) {}

        AssessmentResultVO result = assessmentService.calculate(req.getAnswers());

        // 有 openid 时持久化报告，供测评记录页查询
        if (StringUtils.hasText(openid)) {
            try {
                AssessmentReport report = new AssessmentReport();
                report.setOpenid(openid);
                report.setSessionId(null);   // 内存测评无 session
                report.setTotalScore(result.getTotalScore());
                report.setLevel(result.getLevel());
                report.setLevelCode(toLevelCode(result.getLevel()));
                report.setSummary(result.getLevelDesc());
                report.setCategoryScoresJson(objectMapper.writeValueAsString(result.getCategoryScores()));
                report.setSuggestionsJson(objectMapper.writeValueAsString(result.getRecommendations()));
                reportMapper.insert(report);
            } catch (Exception e) {
                log.warn("保存测评报告失败: {}", e.getMessage());
            }
        }

        return Result.ok(result);
    }

    /** 获取当前用户的测评记录列表；无 token 时返回空列表 */
    @GetMapping("/records")
    public Result<List<RecordVO>> getRecords(HttpServletRequest request) {
        String openid = null;
        try { openid = extractOpenid(request); } catch (Exception ignored) {}

        if (!StringUtils.hasText(openid)) {
            return Result.ok(List.of());
        }

        List<AssessmentReport> reports = reportMapper.findByOpenid(openid);
        List<RecordVO> list = reports.stream().map(r -> {
            RecordVO vo = new RecordVO();
            vo.setSessionId(r.getId());          // 用 report.id 作为详情跳转标识
            vo.setTotalScore(r.getTotalScore());
            vo.setMaxScore(54);
            vo.setLevel(r.getLevel());
            vo.setLevelCode(r.getLevelCode() != null ? r.getLevelCode() : toLevelCode(r.getLevel()));
            vo.setFinishedAt(r.getCreatedAt());
            vo.setExamType("AI能力测评");
            return vo;
        }).toList();
        return Result.ok(list);
    }
}
