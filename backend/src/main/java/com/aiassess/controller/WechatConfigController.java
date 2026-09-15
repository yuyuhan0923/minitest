package com.aiassess.controller;

import com.aiassess.dto.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 微信联系 + 课程配置接口 —— 纯 yml 驱动，不依赖数据库
 * 修改 application.yml 中 app.wechat.* / app.course.* 即可生效
 */
@RestController
@RequestMapping("/api/mini")
public class WechatConfigController {

    // ── 微信配置 ──────────────────────────────────────────────────────────
    @Value("${app.wechat.teacher-wechat-id:sxy15205811430}")
    private String teacherWechatId;

    @Value("${app.wechat.teacher-qrcode-url:/images/teacher_wechat.png}")
    private String teacherQrcodeUrl;

    @Value("${app.wechat.group-qrcode-url:/images/study_group.png}")
    private String groupQrcodeUrl;

    // ── 课程配置 ──────────────────────────────────────────────────────────
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

    /**
     * GET /api/mini/wechat/config
     * 返回老师微信号、二维码路径、群活码路径
     */
    @GetMapping("/wechat/config")
    public Result<Map<String, Object>> getWechatConfig() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("teacherWechatId",  teacherWechatId);
        data.put("teacherQrcodeUrl", StringUtils.hasText(teacherQrcodeUrl) ? teacherQrcodeUrl : null);
        data.put("groupQrcodeUrl",   StringUtils.hasText(groupQrcodeUrl)   ? groupQrcodeUrl   : null);
        return Result.ok(data);
    }

    /**
     * GET /api/mini/course/config
     * 返回课程名称、价格文案、优惠描述、购买链接、试听链接
     */
    @GetMapping("/course/config")
    public Result<Map<String, Object>> getCourseConfig() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("courseName",     courseName);
        data.put("coursePrice",    coursePrice);
        data.put("courseDiscount", courseDiscount);
        data.put("courseUrl",      StringUtils.hasText(courseUrl) ? courseUrl : null);
        data.put("buyUrl",         StringUtils.hasText(buyUrl)    ? buyUrl    : null);
        return Result.ok(data);
    }
}
