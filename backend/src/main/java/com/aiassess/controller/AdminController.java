package com.aiassess.controller;

import com.aiassess.dto.*;
import com.aiassess.entity.*;
import com.aiassess.mapper.*;
import com.aiassess.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final QuestionMapper questionMapper;
    private final LeadMapper leadMapper;
    private final AssessmentReportMapper reportMapper;
    private final StatsService statsService;
    private final SysConfigService sysConfigService;
    private final UserActionMapper userActionMapper;
    private final UserManageService userManageService;

    @Value("${app.course.course-name:AI算法工程师系统课}")
    private String courseName;
    @Value("${app.course.course-price:限时优惠价 ¥799}")
    private String coursePrice;
    @Value("${app.course.course-discount:从Python基础到模型部署，30天掌握AI算法核心技能}")
    private String courseDiscount;
    @Value("${app.course.course-url:}")
    private String courseUrl;
    @Value("${app.course.buy-url:}")
    private String buyUrl;
    @Value("${app.wechat.teacher-wechat-id:sxy15205811430}")
    private String teacherWechatId;

    /** 管理员登录 */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody AdminLoginRequest req) {
        return Result.ok(adminService.login(req));
    }

    // ==================== 题库管理 ====================

    @GetMapping("/questions")
    public Result<Map<String, Object>> listQuestions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<Question>()
            .orderByAsc(Question::getSortOrder);
        if (StringUtils.hasText(category)) wrapper.eq(Question::getCategory, category);
        if (StringUtils.hasText(keyword)) wrapper.like(Question::getTitle, keyword);

        Page<Question> pageResult = questionMapper.selectPage(new Page<>(page, size), wrapper);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("size", size);
        return Result.ok(result);
    }

    @GetMapping("/questions/{id}")
    public Result<Question> getQuestion(@PathVariable Long id) {
        return Result.ok(questionMapper.selectById(id));
    }

    @PostMapping("/questions")
    public Result<Question> createQuestion(@Valid @RequestBody QuestionRequest req) {
        Question q = new Question();
        copyQuestionFields(req, q);
        questionMapper.insert(q);
        return Result.ok(q);
    }

    @PutMapping("/questions/{id}")
    public Result<Question> updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionRequest req) {
        Question q = questionMapper.selectById(id);
        if (q == null) return Result.fail("题目不存在");
        copyQuestionFields(req, q);
        questionMapper.updateById(q);
        return Result.ok(q);
    }

    @DeleteMapping("/questions/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        questionMapper.deleteById(id);
        return Result.ok();
    }

    private void copyQuestionFields(QuestionRequest req, Question q) {
        q.setTitle(req.getTitle());
        q.setOptionsJson(req.getOptionsJson());
        q.setCorrectAnswer(req.getCorrectAnswer());
        q.setCategory(req.getCategory());
        q.setScore(req.getScore());
        q.setSortOrder(req.getSortOrder());
        q.setStatus(req.getStatus());
    }

    // ==================== 线索管理 ====================

    @GetMapping("/leads")
    public Result<Map<String, Object>> listLeads(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer followStatus) {
        LambdaQueryWrapper<Lead> wrapper = new LambdaQueryWrapper<Lead>()
            .orderByDesc(Lead::getCreatedAt);
        if (StringUtils.hasText(phone)) wrapper.like(Lead::getPhone, phone);
        if (followStatus != null) wrapper.eq(Lead::getFollowStatus, followStatus);

        Page<Lead> pageResult = leadMapper.selectPage(new Page<>(page, size), wrapper);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        return Result.ok(result);
    }

    @PutMapping("/leads/{id}/follow")
    public Result<Void> updateFollowStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Lead lead = leadMapper.selectById(id);
        if (lead == null) return Result.fail("线索不存在");
        Object followStatusVal = body.get("followStatus");
        if (followStatusVal != null) {
            lead.setFollowStatus(((Number) followStatusVal).intValue());
        }
        Object remarkVal = body.get("remark");
        if (remarkVal instanceof String remark && StringUtils.hasText(remark)) {
            lead.setRemark(remark);
        }
        leadMapper.updateById(lead);
        return Result.ok();
    }

    // ==================== 报告管理 ====================

    @GetMapping("/reports")
    public Result<Map<String, Object>> listReports(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String levelCode) {
        LambdaQueryWrapper<AssessmentReport> wrapper = new LambdaQueryWrapper<AssessmentReport>()
            .orderByDesc(AssessmentReport::getCreatedAt);
        if (StringUtils.hasText(levelCode)) wrapper.eq(AssessmentReport::getLevelCode, levelCode);

        Page<AssessmentReport> pageResult = reportMapper.selectPage(new Page<>(page, size), wrapper);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        return Result.ok(result);
    }

    // ==================== 统计看板 ====================

    @GetMapping("/stats/overview")
    public Result<Map<String, Object>> getOverview() {
        return Result.ok(statsService.getOverview());
    }

    @GetMapping("/stats/daily")
    public Result<List<Map<String, Object>>> getDailyStats(
            @RequestParam(defaultValue = "7") int days) {
        return Result.ok(statsService.getDailyStats(days));
    }

    // ==================== 系统配置管理 ====================

    /** 获取所有配置 */
    @GetMapping("/config")
    public Result<Map<String, String>> getAllConfig() {
        return Result.ok(sysConfigService.getAll());
    }

    /** 批量更新配置（传入 key-value map） */
    @PutMapping("/config")
    public Result<Void> updateConfig(@RequestBody Map<String, String> configs) {
        configs.forEach(sysConfigService::set);
        return Result.ok();
    }

    /** 更新单个配置 */
    @PutMapping("/config/{key}")
    public Result<Void> updateConfigItem(@PathVariable String key, @RequestBody Map<String, String> body) {
        String value = body.get("value");
        if (value == null) return Result.fail("value 不能为空");
        sysConfigService.set(key, value);
        return Result.ok();
    }

    // ==================== 用户行为 / 高意向线索 ====================

    /** 用户行为列表（筛选高意向：course_trial / course_buy / add_wechat） */
    @GetMapping("/actions")
    public Result<Map<String, Object>> listActions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String openid) {
        LambdaQueryWrapper<UserAction> wrapper = new LambdaQueryWrapper<UserAction>()
            .orderByDesc(UserAction::getCreatedAt);
        if (StringUtils.hasText(actionType)) wrapper.eq(UserAction::getActionType, actionType);
        if (StringUtils.hasText(openid))     wrapper.eq(UserAction::getOpenid, openid);

        Page<UserAction> pageResult = userActionMapper.selectPage(new Page<>(page, size), wrapper);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        return Result.ok(result);
    }

    // ==================== 用户列表 ====================

    @GetMapping("/users")
    public Result<List<UserInfo>> listUsers() {
        return Result.ok(userManageService.listAllUsers());
    }

    @GetMapping("/users/{openid}")
    public Result<UserInfo> getUserByOpenid(@PathVariable String openid) {
        UserInfo u = userManageService.getByOpenid(openid);
        if (u == null) return Result.fail("用户不存在");
        return Result.ok(u);
    }

    // ==================== 测评记录列表 ====================

    /** 所有测评记录（assessment_report 表） */
    @GetMapping("/records")
    public Result<Map<String, Object>> listRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String openid,
            @RequestParam(required = false) String level) {
        LambdaQueryWrapper<AssessmentReport> wrapper = new LambdaQueryWrapper<AssessmentReport>()
            .orderByDesc(AssessmentReport::getCreatedAt);
        if (StringUtils.hasText(openid)) wrapper.eq(AssessmentReport::getOpenid, openid);
        if (StringUtils.hasText(level))  wrapper.eq(AssessmentReport::getLevel, level);
        Page<AssessmentReport> pageResult = reportMapper.selectPage(new Page<>(page, size), wrapper);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        return Result.ok(result);
    }

    // ==================== 课程配置查看 ====================

    /** 查看当前课程配置（来自 yml） */
    @GetMapping("/course")
    public Result<Map<String, Object>> getCourseConfig() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("courseName",     courseName);
        data.put("coursePrice",    coursePrice);
        data.put("courseDiscount", courseDiscount);
        data.put("courseUrl",      StringUtils.hasText(courseUrl) ? courseUrl : null);
        data.put("buyUrl",         StringUtils.hasText(buyUrl)    ? buyUrl    : null);
        data.put("teacherWechatId", teacherWechatId);
        return Result.ok(data);
    }
}
