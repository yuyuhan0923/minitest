package com.aiassess.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("`lead`")
public class Lead {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private String openid;
    private String phone;
    private String wechat;
    private String nickname;
    private String avatarUrl;
    private String level;
    private Integer totalScore;
    private String source;
    private Integer materialClaimed;
    private java.time.LocalDateTime materialClaimedAt;
    private String remark;
    private Integer followStatus;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
