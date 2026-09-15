package com.aiassess.service;

import com.aiassess.dto.ReportVO;
import com.aiassess.dto.SubmitExamRequest;
import com.aiassess.entity.*;
import com.aiassess.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamService {

    private final QuestionMapper questionMapper;
    private final ExamSessionMapper sessionMapper;
    private final ExamAnswerMapper answerMapper;
    private final AssessmentReportMapper reportMapper;
    private final LeadMapper leadMapper;
    private final ObjectMapper objectMapper;

    /** 获取所有启用题目（按排序） */
    public List<Question> getQuestions() {
        return questionMapper.selectList(
            new LambdaQueryWrapper<Question>()
                .eq(Question::getStatus, 1)
                .orderByAsc(Question::getSortOrder)
        );
    }

    /** 开始答题，创建session */
    @Transactional
    public ExamSession startExam(String openid) {
        ExamSession session = new ExamSession();
        session.setOpenid(openid);
        session.setStatus(0);
        session.setStartTime(LocalDateTime.now());
        sessionMapper.insert(session);
        return session;
    }

    /** 提交答题，计算得分，生成报告 */
    @Transactional
    public ReportVO submitExam(String openid, SubmitExamRequest req) {
        ExamSession session = sessionMapper.selectById(req.getSessionId());
        if (session == null || !session.getOpenid().equals(openid)) {
            throw new RuntimeException("会话不存在或无权限");
        }
        if (session.getStatus() == 1) {
            // 已提交，直接返回已有报告
            return buildReportVO(session.getId(), openid);
        }

        // 查询所有题目（用于校验答案）
        List<Question> questions = questionMapper.selectList(
            new LambdaQueryWrapper<Question>().eq(Question::getStatus, 1)
        );
        Map<Long, Question> questionMap = questions.stream()
            .collect(Collectors.toMap(Question::getId, q -> q));

        // 保存答题记录，计算得分
        int totalScore = 0;
        int correctCount = 0;
        List<ExamAnswer> answers = new ArrayList<>();

        for (SubmitExamRequest.AnswerItem item : req.getAnswers()) {
            Question q = questionMap.get(item.getQuestionId());
            if (q == null) continue;

            boolean isCorrect = q.getCorrectAnswer().equalsIgnoreCase(item.getSelectedOption());
            int score = isCorrect ? q.getScore() : 0;
            totalScore += score;
            if (isCorrect) correctCount++;

            ExamAnswer answer = new ExamAnswer();
            answer.setSessionId(session.getId());
            answer.setQuestionId(q.getId());
            answer.setSelectedOption(item.getSelectedOption());
            answer.setCorrectAnswer(q.getCorrectAnswer());
            answer.setIsCorrect(isCorrect ? 1 : 0);
            answer.setScore(score);
            answer.setCategory(q.getCategory());
            answers.add(answer);
        }

        // 批量保存答题记录
        for (ExamAnswer answer : answers) {
            answerMapper.insert(answer);
        }

        // 更新session
        session.setTotalScore(totalScore);
        session.setTotalQuestions(answers.size());
        session.setCorrectCount(correctCount);
        session.setStatus(1);
        session.setEndTime(LocalDateTime.now());
        sessionMapper.updateById(session);

        // 生成报告
        generateReport(session, answers, questions);

        return buildReportVO(session.getId(), openid);
    }

    /** 生成测评报告 */
    private void generateReport(ExamSession session, List<ExamAnswer> answers, List<Question> questions) {
        int totalScore = session.getTotalScore();
        int maxScore = questions.stream().mapToInt(Question::getScore).sum();
        int percentage = maxScore > 0 ? (totalScore * 100 / maxScore) : 0;

        // 等级判定
        String level, levelCode;
        if (percentage >= 80) {
            level = "专家级";
            levelCode = "expert";
        } else if (percentage >= 60) {
            level = "进阶级";
            levelCode = "intermediate";
        } else {
            level = "入门级";
            levelCode = "beginner";
        }

        // 按分类统计得分
        Map<String, int[]> categoryStats = new LinkedHashMap<>();
        for (Question q : questions) {
            categoryStats.computeIfAbsent(q.getCategory(), k -> new int[]{0, 0});
            categoryStats.get(q.getCategory())[1] += q.getScore();
        }
        for (ExamAnswer a : answers) {
            if (categoryStats.containsKey(a.getCategory())) {
                categoryStats.get(a.getCategory())[0] += a.getScore();
            }
        }

        List<Map<String, Object>> categoryScores = new ArrayList<>();
        List<String> weakCategories = new ArrayList<>();
        for (Map.Entry<String, int[]> entry : categoryStats.entrySet()) {
            int catScore = entry.getValue()[0];
            int catTotal = entry.getValue()[1];
            int catPct = catTotal > 0 ? (catScore * 100 / catTotal) : 0;
            Map<String, Object> cs = new LinkedHashMap<>();
            cs.put("category", entry.getKey());
            cs.put("score", catScore);
            cs.put("total", catTotal);
            cs.put("percentage", catPct);
            categoryScores.add(cs);
            if (catPct < 60) weakCategories.add(entry.getKey());
        }

        // 生成综合评价
        String summary = buildSummary(level, levelCode, percentage, weakCategories);

        // 生成个性化建议
        List<String> suggestions = buildSuggestions(levelCode, weakCategories);

        // 保存报告
        AssessmentReport report = new AssessmentReport();
        report.setSessionId(session.getId());
        report.setOpenid(session.getOpenid());
        report.setTotalScore(totalScore);
        report.setLevel(level);
        report.setLevelCode(levelCode);
        report.setSummary(summary);
        try {
            report.setCategoryScoresJson(objectMapper.writeValueAsString(categoryScores));
            report.setSuggestionsJson(objectMapper.writeValueAsString(suggestions));
        } catch (Exception e) {
            report.setCategoryScoresJson("[]");
            report.setSuggestionsJson("[]");
        }
        reportMapper.insert(report);
    }

    private String buildSummary(String level, String levelCode, int percentage, List<String> weakCategories) {
        String base = switch (levelCode) {
            case "expert" -> String.format(
                "恭喜！您的AI算法能力达到【%s】，综合得分率 %d%%，展现出扎实的算法功底和系统性知识体系。",
                level, percentage);
            case "intermediate" -> String.format(
                "您的AI算法能力处于【%s】，综合得分率 %d%%，已掌握核心概念，具备一定的实战基础。",
                level, percentage);
            default -> String.format(
                "您的AI算法能力处于【%s】，综合得分率 %d%%，正处于学习成长阶段，潜力无限！",
                level, percentage);
        };
        if (!weakCategories.isEmpty()) {
            base += String.format("在 %s 方向还有较大提升空间。", String.join("、", weakCategories));
        }
        return base;
    }

    private List<String> buildSuggestions(String levelCode, List<String> weakCategories) {
        List<String> suggestions = new ArrayList<>();
        Map<String, String> categoryAdvice = Map.of(
            "机器学习", "建议系统学习监督学习、无监督学习核心算法，动手实践Scikit-learn框架",
            "深度学习", "建议从神经网络基础入手，掌握PyTorch/TensorFlow，完成图像分类实战项目",
            "数据处理", "建议强化Pandas/NumPy数据处理能力，学习特征工程最佳实践",
            "算法基础", "建议刷LeetCode中等难度题目，重点掌握动态规划、图算法等核心数据结构"
        );

        for (String cat : weakCategories) {
            String advice = categoryAdvice.get(cat);
            if (advice != null) suggestions.add(advice);
        }

        suggestions.add(switch (levelCode) {
            case "expert" -> "您已具备参与AI算法竞赛（Kaggle/天池）的能力，建议挑战更复杂的业务场景项目";
            case "intermediate" -> "建议参与实际项目实战，将理论知识转化为工程能力，可考虑AI算法工程师认证";
            default -> "建议从零基础AI算法课程开始，打好数学基础（线性代数、概率统计），循序渐进";
        });

        return suggestions;
    }

    /** 构建报告VO */
    public ReportVO buildReportVO(Long sessionId, String openid) {
        AssessmentReport report = reportMapper.findBySessionId(sessionId);
        if (report == null) throw new RuntimeException("报告不存在");

        ExamSession session = sessionMapper.selectById(sessionId);
        List<Question> questions = questionMapper.selectList(
            new LambdaQueryWrapper<Question>().eq(Question::getStatus, 1)
        );
        int maxScore = questions.stream().mapToInt(Question::getScore).sum();

        ReportVO vo = new ReportVO();
        vo.setSessionId(sessionId);
        vo.setTotalScore(report.getTotalScore());
        vo.setMaxScore(maxScore);
        vo.setLevel(report.getLevel());
        vo.setLevelCode(report.getLevelCode());
        vo.setSummary(report.getSummary());

        try {
            List<ReportVO.CategoryScore> catScores = new ArrayList<>();
            List<Map<String, Object>> rawList = objectMapper.readValue(
                report.getCategoryScoresJson(), new TypeReference<>() {});
            for (Map<String, Object> raw : rawList) {
                ReportVO.CategoryScore cs = new ReportVO.CategoryScore();
                cs.setCategory((String) raw.get("category"));
                cs.setScore(((Number) raw.get("score")).intValue());
                cs.setTotal(((Number) raw.get("total")).intValue());
                cs.setPercentage(((Number) raw.get("percentage")).intValue());
                catScores.add(cs);
            }
            vo.setCategoryScores(catScores);
            vo.setSuggestions(objectMapper.readValue(report.getSuggestionsJson(), new TypeReference<>() {}));
        } catch (Exception e) {
            vo.setCategoryScores(new ArrayList<>());
            vo.setSuggestions(new ArrayList<>());
        }

        // 是否已留资
        vo.setHasLead(leadMapper.countByOpenid(openid) > 0);
        return vo;
    }
}
