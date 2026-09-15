package com.aiassess.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_action")
public class UserAction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String openid;
    private String actionType;
    private String extra;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
