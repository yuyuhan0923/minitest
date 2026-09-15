package com.aiassess.service;

import com.aiassess.entity.UserInfo;
import com.aiassess.mapper.UserActionMapper;
import com.aiassess.mapper.UserInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserManageService {

    private final UserInfoMapper userInfoMapper;
    private final UserActionMapper userActionMapper;

    public List<UserInfo> listAllUsers() {
        List<UserInfo> users = userInfoMapper.selectAllWithAssessData();
        LocalDate today = LocalDate.now();
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        for (UserInfo u : users) {
            u.setAssessLevel(calcAssessLevel(u.getTotalExams()));
            u.setBehaviorTag(calcBehaviorTag(u.getOpenid(), thirtyDaysAgo));
            u.setPhone(maskPhone(u.getPhone()));
        }
        return users;
    }

    public UserInfo getByOpenid(String openid) {
        UserInfo u = userInfoMapper.selectByOpenid(openid);
        if (u == null) return null;
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        u.setAssessLevel(calcAssessLevel(u.getTotalExams()));
        u.setBehaviorTag(calcBehaviorTag(u.getOpenid(), thirtyDaysAgo));
        u.setPhone(maskPhone(u.getPhone()));
        return u;
    }

    private String calcAssessLevel(Integer totalExams) {
        if (totalExams == null || totalExams == 0) return "初级";
        if (totalExams <= 2) return "中级";
        return "高级";
    }

    private String calcBehaviorTag(String openid, LocalDateTime since) {
        long count = userActionMapper.countVisitSince(openid, since);
        if (count >= 5) return "活跃";
        if (count <= 1) return "沉默";
        return "新用户";
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
