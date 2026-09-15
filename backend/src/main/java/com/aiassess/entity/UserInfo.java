package com.aiassess.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserInfo {
    private String openid;
    private String nickname;
    private String phone;
    private String assessLevel;
    private String behaviorTag;
    private LocalDateTime createTime;
    private Integer todayExams;
    private Integer totalExams;
    private Integer hasLead;
}
