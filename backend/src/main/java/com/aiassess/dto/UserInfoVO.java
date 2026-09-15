package com.aiassess.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserInfoVO {
    private String openid;
    private String nickname;
    private String phone;        // 脱敏后
    private String level;        // 最近一次测评等级
    private Integer totalScore;  // 最近一次得分
    private List<String> behaviorTags; // 行为标签
    private LocalDateTime registeredAt; // 首次出现时间
    private long todayExamCount;
    private long totalExamCount;
    private boolean hasLead;     // 是否已留资
}
