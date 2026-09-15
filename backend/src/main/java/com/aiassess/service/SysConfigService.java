package com.aiassess.service;

import com.aiassess.entity.SysConfig;
import com.aiassess.mapper.SysConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysConfigService {

    private final SysConfigMapper configMapper;

    // yml 兜底配置，数据库未建表时直接使用
    @Value("${app.teacher-wechat:AI_Teacher_001}")
    private String teacherWechat;

    @Value("${app.teacher-qrcode-url:}")
    private String teacherQrcodeUrl;

    @Value("${app.group-qrcode-url:}")
    private String groupQrcodeUrl;

    @Value("${app.group-expire-tip:活码每7天更新，扫码加群即可}")
    private String groupExpireTip;

    @Value("${app.course-name:AI算法工程师系统课}")
    private String courseName;

    @Value("${app.course-price:1299}")
    private String coursePrice;

    @Value("${app.course-discount:限时优惠价 ¥799}")
    private String courseDiscount;

    @Value("${app.course-desc:从Python基础到模型部署，30天掌握AI算法核心技能}")
    private String courseDesc;

    @Value("${app.trial-url:}")
    private String trialUrl;

    @Value("${app.buy-url:}")
    private String buyUrl;

    /** yml 兜底 map，数据库不可用时使用 */
    private Map<String, String> ymlDefaults() {
        Map<String, String> m = new HashMap<>();
        m.put("teacher_wechat",    teacherWechat);
        m.put("teacher_qrcode_url", teacherQrcodeUrl);
        m.put("group_qrcode_url",  groupQrcodeUrl);
        m.put("group_expire_tip",  groupExpireTip);
        m.put("course_name",       courseName);
        m.put("course_price",      coursePrice);
        m.put("course_discount",   courseDiscount);
        m.put("course_desc",       courseDesc);
        m.put("trial_url",         trialUrl);
        m.put("buy_url",           buyUrl);
        m.put("material_list",     "[\"AI算法路线图PDF\",\"Python数据分析模板\",\"机器学习速查表\",\"PyTorch模板\",\"面试题100道\",\"实战案例集\"]");
        return m;
    }

    public String get(String key) {
        return get(key, "");
    }

    public String get(String key, String defaultValue) {
        try {
            SysConfig cfg = configMapper.findByKey(key);
            if (cfg != null && cfg.getConfigValue() != null) {
                return cfg.getConfigValue();
            }
        } catch (Exception e) {
            log.debug("sys_config 表查询失败，使用 yml 兜底: {}", e.getMessage());
        }
        // 先查 yml 兜底，再用 defaultValue
        String ymlVal = ymlDefaults().get(key);
        return (ymlVal != null && !ymlVal.isEmpty()) ? ymlVal : defaultValue;
    }

    public Map<String, String> getAll() {
        try {
            List<SysConfig> list = configMapper.selectList(new LambdaQueryWrapper<>());
            if (!list.isEmpty()) {
                return list.stream().collect(Collectors.toMap(SysConfig::getConfigKey, SysConfig::getConfigValue));
            }
        } catch (Exception e) {
            log.debug("sys_config 表查询失败，使用 yml 兜底");
        }
        return ymlDefaults();
    }

    public void set(String key, String value) {
        try {
            SysConfig existing = configMapper.findByKey(key);
            if (existing != null) {
                existing.setConfigValue(value);
                configMapper.updateById(existing);
            } else {
                SysConfig cfg = new SysConfig();
                cfg.setConfigKey(key);
                cfg.setConfigValue(value);
                configMapper.insert(cfg);
            }
        } catch (Exception e) {
            log.warn("sys_config 写入失败: {}", e.getMessage());
        }
    }
}
