package com.aiassess.service;

import com.aiassess.dto.AssessmentResultVO;
import com.aiassess.dto.AssessmentResultVO.CategoryScore;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AssessmentService {

    // ── 题目数据（内存，无需建表）──────────────────────────────────────────
    public record Option(String key, String text) {}

    public record AssessmentQuestion(
        int id,
        String category,
        String title,
        List<Option> options
    ) {}

    private static final List<AssessmentQuestion> QUESTIONS = List.of(
        // 编程基础
        q(1,  "编程基础", "你目前 Python 水平如何？",
            "没学过", "会基础语法", "会使用 NumPy / Pandas", "能独立写数据处理脚本"),
        q(2,  "编程基础", "你是否使用过 Jupyter Notebook？",
            "没用过", "用过但不熟", "经常使用", "能熟练做数据分析和模型实验"),
        q(3,  "编程基础", "你是否会使用 Git？",
            "不会", "会 clone 项目", "会 commit / push", "会分支管理和协作开发"),
        // 数学基础
        q(4,  "数学基础", "你对线性代数掌握如何？",
            "不熟悉矩阵", "知道矩阵乘法", "理解向量、矩阵、特征值", "能理解神经网络中的矩阵运算"),
        q(5,  "数学基础", "你对概率统计掌握如何？",
            "几乎不了解", "知道均值、方差、概率", "理解分布、条件概率", "能理解损失函数和模型评估指标"),
        q(6,  "数学基础", "你对微积分掌握如何？",
            "不熟悉", "知道导数", "理解梯度", "能理解反向传播中的梯度计算"),
        // 机器学习基础
        q(7,  "机器学习基础", "你是否了解监督学习和无监督学习？",
            "完全不了解", "听说过", "理解基本区别", "能用于实际项目"),
        q(8,  "机器学习基础", "你是否使用过 sklearn？",
            "没用过", "跑过示例代码", "用过分类、回归模型", "能完成完整机器学习项目"),
        q(9,  "机器学习基础", "你是否理解过拟合和欠拟合？",
            "不理解", "听说过", "基本理解", "能通过正则化、交叉验证等方式处理"),
        // 深度学习基础
        q(10, "深度学习基础", "你是否理解神经网络的基本结构？",
            "不理解", "听说过", "理解输入层、隐藏层、输出层", "能手写简单神经网络代码"),
        q(11, "深度学习基础", "你是否了解 CNN？",
            "不了解", "听说过", "理解卷积和池化", "做过图像分类项目"),
        q(12, "深度学习基础", "你是否了解 Transformer？",
            "不了解", "听说过", "理解 Attention 基本思想", "阅读过相关代码或论文"),
        // 框架工具能力
        q(13, "框架工具能力", "你是否使用过 PyTorch？",
            "没用过", "跑过 demo", "会写 Dataset、Model、Training Loop", "能独立训练和调参"),
        q(14, "框架工具能力", "你是否使用过 GPU 训练模型？",
            "没用过", "用过 Colab", "会配置 CUDA 环境", "能处理显存不足、训练加速等问题"),
        q(15, "框架工具能力", "你是否用过 Hugging Face？",
            "没用过", "听说过", "使用过 tokenizer / model", "能微调模型或部署模型"),
        // 项目实战能力
        q(16, "项目实战能力", "你是否完整做过 AI 项目？",
            "没有", "跑过别人代码", "改过开源项目", "独立完成过项目"),
        q(17, "项目实战能力", "你是否能处理真实数据集？",
            "不会", "会下载数据", "会清洗和划分数据集", "会处理缺失值、类别不平衡、数据增强"),
        q(18, "项目实战能力", "你是否有可以展示的 AI 项目作品？",
            "没有", "有简单 demo", "有 GitHub 项目", "有可部署或可演示项目")
    );

    private static AssessmentQuestion q(int id, String cat, String title,
                                         String a, String b, String c, String d) {
        return new AssessmentQuestion(id, cat, title, List.of(
            new Option("A", a), new Option("B", b),
            new Option("C", c), new Option("D", d)
        ));
    }

    // ── 评分规则 ──────────────────────────────────────────────────────────
    private static final Map<String, Integer> SCORE_MAP = Map.of(
        "A", 0, "B", 1, "C", 2, "D", 3
    );

    // ── 等级定义 ──────────────────────────────────────────────────────────
    private record Level(int min, int max, String name, String desc, List<String> recs) {}

    private static final List<Level> LEVELS = List.of(
        new Level(0,  20, "AI入门准备阶段",
            "你目前处于 AI 学习的起点，打好基础是关键。",
            List.of("Python 编程基础", "NumPy / Pandas 数据处理", "线性代数与概率统计", "数学基础补强")),
        new Level(21, 40, "AI基础学习阶段",
            "你已有一定基础，可以系统学习机器学习核心概念。",
            List.of("机器学习基础理论", "数据预处理与特征工程", "模型评估与调优", "sklearn 实战练习")),
        new Level(41, 60, "机器学习进阶阶段",
            "你掌握了基础，可以深入经典算法并动手做项目。",
            List.of("sklearn 经典算法深入", "完整机器学习项目实战", "Kaggle 竞赛入门", "数据可视化与分析")),
        new Level(61, 80, "深度学习实战阶段",
            "你具备较强基础，可以进入深度学习核心领域。",
            List.of("PyTorch 框架深入", "CNN 图像识别项目", "Transformer 与 NLP", "GPU 训练与模型调参")),
        new Level(81, 54, "项目/求职/科研提升阶段",
            "你已达到较高水平，可以冲击项目部署、求职或科研方向。",
            List.of("模型部署与工程化", "Hugging Face 模型微调", "简历项目打磨", "论文阅读与复现"))
    );

    // ── 公开接口 ──────────────────────────────────────────────────────────

    public List<AssessmentQuestion> getQuestions() {
        return QUESTIONS;
    }

    public AssessmentResultVO calculate(Map<Integer, String> answers) {
        int total = 0;
        Map<String, int[]> catScores = new LinkedHashMap<>(); // [得分, 满分]

        for (AssessmentQuestion q : QUESTIONS) {
            catScores.putIfAbsent(q.category(), new int[]{0, 0});
            catScores.get(q.category())[1] += 3; // 每题满分3分

            String ans = answers.getOrDefault(q.id(), "A").toUpperCase();
            int score = SCORE_MAP.getOrDefault(ans, 0);
            total += score;
            catScores.get(q.category())[0] += score;
        }

        Level matched = LEVELS.get(0);
        for (Level lv : LEVELS) {
            if (total >= lv.min()) {
                matched = lv;
            }
        }

        List<CategoryScore> categoryScores = catScores.entrySet().stream()
            .map(e -> CategoryScore.builder()
                .category(e.getKey())
                .score(e.getValue()[0])
                .maxScore(e.getValue()[1])
                .build())
            .toList();

        return AssessmentResultVO.builder()
            .totalScore(total)
            .maxScore(QUESTIONS.size() * 3)
            .level(matched.name())
            .levelDesc(matched.desc())
            .recommendations(matched.recs())
            .categoryScores(categoryScores)
            .build();
    }
}
