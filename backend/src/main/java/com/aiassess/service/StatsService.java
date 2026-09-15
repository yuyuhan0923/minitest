package com.aiassess.service;

import com.aiassess.entity.*;
import com.aiassess.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final LeadMapper leadMapper;
    private final ExamSessionMapper sessionMapper;
    private final QuestionMapper questionMapper;
    private final UserActionMapper userActionMapper;

    public Map<String, Object> getOverview() {
        Map<String, Object> stats = new LinkedHashMap<>();

        // 注册用户：user_action 表去重 openid（原生 SQL，避免 MyBatis-Plus groupBy 误用）
        long registerUsers = userActionMapper.countDistinctOpenid();
        stats.put("registerUsers", registerUsers);

        // 留资人数：lead 表总记录数
        long totalLeads = leadMapper.selectCount(null);
        stats.put("totalLeads", totalLeads);

        // 访问人数：user_action 表 actionType=visit 的记录数
        long visitUsers = userActionMapper.selectCount(
            new LambdaQueryWrapper<UserAction>()
                .eq(UserAction::getActionType, "visit")
        );
        stats.put("visitUsers", visitUsers);

        // 完成测评：exam_session 表 status=1 的记录数
        long completeExams = sessionMapper.selectCount(
            new LambdaQueryWrapper<ExamSession>()
                .eq(ExamSession::getStatus, 1)
        );
        stats.put("completeExams", completeExams);

        // 点击购买：user_action 表 actionType=buy_click 的记录数
        long buyClickUsers = userActionMapper.selectCount(
            new LambdaQueryWrapper<UserAction>()
                .eq(UserAction::getActionType, "buy_click")
        );
        stats.put("buyClickUsers", buyClickUsers);

        // 转化率 = 留资人数 / 完成测评数 * 100，保留 1 位小数
        double conversion = completeExams > 0 ? (totalLeads * 100.0 / completeExams) : 0;
        stats.put("conversionRate", Math.round(conversion * 10) / 10.0);

        // 今日增量数据
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);

        long todayUsers = userActionMapper.countDistinctOpenidSince(todayStart);
        stats.put("todayUsers", todayUsers);

        long todayLeads = leadMapper.selectCount(
            new LambdaQueryWrapper<Lead>()
                .ge(Lead::getCreatedAt, todayStart)
        );
        stats.put("todayLeads", todayLeads);

        long todayExams = sessionMapper.selectCount(
            new LambdaQueryWrapper<ExamSession>()
                .eq(ExamSession::getStatus, 1)
                .ge(ExamSession::getCreatedAt, todayStart)
        );
        stats.put("todayExams", todayExams);

        return stats;
    }

    public List<Map<String, Object>> getDailyStats(int days) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            LocalDateTime start = LocalDateTime.of(d, LocalTime.MIN);
            LocalDateTime end   = LocalDateTime.of(d, LocalTime.MAX);

            long exams = sessionMapper.selectCount(
                new LambdaQueryWrapper<ExamSession>()
                    .eq(ExamSession::getStatus, 1)
                    .between(ExamSession::getCreatedAt, start, end)
            );
            long leads = leadMapper.selectCount(
                new LambdaQueryWrapper<Lead>()
                    .between(Lead::getCreatedAt, start, end)
            );

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("date", d.toString());
            map.put("exams", exams);
            map.put("leads", leads);
            list.add(map);
        }
        return list;
    }
}
