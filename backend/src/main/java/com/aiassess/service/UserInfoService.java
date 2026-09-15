package com.aiassess.service;

import com.aiassess.dto.UserInfoVO;
import com.aiassess.entity.ExamSession;
import com.aiassess.entity.Lead;
import com.aiassess.entity.UserAction;
import com.aiassess.mapper.ExamSessionMapper;
import com.aiassess.mapper.LeadMapper;
import com.aiassess.mapper.UserActionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RequiredArgsConstructor
public class UserInfoService {

    private final UserActionMapper userActionMapper;
    private final ExamSessionMapper sessionMapper;
    private final LeadMapper leadMapper;

    /**
     * 分页查询用户聚合信息。
     * 数据源：user_action 表（去重 openid），关联 exam_session、lead。
     */
    public Map<String, Object> listUsers(int page, int size, String phone) {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        // 1. 从 user_action 获取所有去重 openid（分页）
        List<String> allOpenids = userActionMapper.selectDistinctOpenids();

        // 2. 如果按手机号过滤，先从 lead 表找到匹配的 openid 集合
        if (phone != null && !phone.isBlank()) {
            List<Lead> matchedLeads = leadMapper.selectList(
                new LambdaQueryWrapper<Lead>().like(Lead::getPhone, phone)
            );
            Set<String> matchedOpenids = new HashSet<>();
            matchedLeads.forEach(l -> matchedOpenids.add(l.getOpenid()));
            allOpenids.removeIf(o -> !matchedOpenids.contains(o));
        }

        long total = allOpenids.size();

        // 3. 手动分页
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, allOpenids.size());
        List<String> pageOpenids = fromIndex >= allOpenids.size()
            ? Collections.emptyList()
            : allOpenids.subList(fromIndex, toIndex);

        // 4. 逐 openid 聚合数据
        List<UserInfoVO> list = new ArrayList<>();
        for (String openid : pageOpenids) {
            UserInfoVO vo = buildUserInfo(openid, todayStart);
            list.add(vo);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    private UserInfoVO buildUserInfo(String openid, LocalDateTime todayStart) {
        UserInfoVO vo = new UserInfoVO();
        vo.setOpenid(openid);

        // 注册时间：user_action 中该 openid 最早的记录
        UserAction earliest = userActionMapper.selectOne(
            new LambdaQueryWrapper<UserAction>()
                .eq(UserAction::getOpenid, openid)
                .orderByAsc(UserAction::getCreatedAt)
                .last("LIMIT 1")
        );
        if (earliest != null) {
            vo.setRegisteredAt(earliest.getCreatedAt());
        }

        // 行为标签：该 openid 有哪些 actionType
        List<UserAction> actions = userActionMapper.selectList(
            new LambdaQueryWrapper<UserAction>()
                .eq(UserAction::getOpenid, openid)
                .select(UserAction::getActionType)
        );
        Set<String> actionTypes = new LinkedHashSet<>();
        actions.forEach(a -> actionTypes.add(a.getActionType()));
        vo.setBehaviorTags(new ArrayList<>(actionTypes));

        // 测评数据
        long totalExams = sessionMapper.countCompletedByOpenid(openid);
        long todayExams = sessionMapper.countCompletedTodayByOpenid(openid, todayStart);
        vo.setTotalExamCount(totalExams);
        vo.setTodayExamCount(todayExams);

        // 最近一次测评等级和得分
        ExamSession latest = sessionMapper.findLatestCompleted(openid);
        if (latest != null) {
            vo.setTotalScore(latest.getTotalScore());
            // 根据分数推算等级（与 submitExam 逻辑保持一致）
            vo.setLevel(scoreToLevel(latest.getTotalScore()));
        }

        // 留资信息
        Lead lead = leadMapper.selectOne(
            new LambdaQueryWrapper<Lead>()
                .eq(Lead::getOpenid, openid)
                .orderByDesc(Lead::getCreatedAt)
                .last("LIMIT 1")
        );
        if (lead != null) {
            vo.setHasLead(true);
            vo.setNickname(lead.getNickname());
            vo.setPhone(maskPhone(lead.getPhone()));
            // 如果 lead 有等级信息优先使用
            if (lead.getLevel() != null && !lead.getLevel().isBlank()) {
                vo.setLevel(lead.getLevel());
            }
            if (lead.getTotalScore() != null && vo.getTotalScore() == null) {
                vo.setTotalScore(lead.getTotalScore());
            }
        } else {
            vo.setHasLead(false);
            vo.setNickname("微信用户");
        }

        return vo;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String scoreToLevel(Integer score) {
        if (score == null) return null;
        if (score >= 45) return "专家级";
        if (score >= 27) return "进阶级";
        return "基础级";
    }
}
