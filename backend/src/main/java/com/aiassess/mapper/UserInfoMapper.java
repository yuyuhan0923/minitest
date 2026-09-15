package com.aiassess.mapper;

import com.aiassess.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface UserInfoMapper {

    @Select("""
        SELECT
            ua.openid,
            COALESCE(l.nickname, '微信用户')                                    AS nickname,
            l.phone,
            MIN(ua.created_at)                                                  AS createTime,
            COUNT(DISTINCT CASE WHEN es.status = 1 THEN es.id END)             AS totalExams,
            COUNT(DISTINCT CASE WHEN es.status = 1
                AND es.created_at >= CURDATE() THEN es.id END)                 AS todayExams,
            MAX(CASE WHEN l.openid IS NOT NULL THEN 1 ELSE 0 END)              AS hasLead
        FROM user_action ua
        LEFT JOIN exam_session es ON es.openid = ua.openid
        LEFT JOIN `lead` l       ON l.openid  = ua.openid
        GROUP BY ua.openid
        ORDER BY MIN(ua.created_at) DESC
    """)
    List<UserInfo> selectAllWithAssessData();

    @Select("""
        SELECT
            ua.openid,
            COALESCE(l.nickname, '微信用户')                                    AS nickname,
            l.phone,
            MIN(ua.created_at)                                                  AS createTime,
            COUNT(DISTINCT CASE WHEN es.status = 1 THEN es.id END)             AS totalExams,
            COUNT(DISTINCT CASE WHEN es.status = 1
                AND es.created_at >= CURDATE() THEN es.id END)                 AS todayExams,
            MAX(CASE WHEN l.openid IS NOT NULL THEN 1 ELSE 0 END)              AS hasLead
        FROM user_action ua
        LEFT JOIN exam_session es ON es.openid = ua.openid
        LEFT JOIN `lead` l       ON l.openid  = ua.openid
        WHERE ua.openid = #{openid}
        GROUP BY ua.openid
    """)
    UserInfo selectByOpenid(@Param("openid") String openid);
}
