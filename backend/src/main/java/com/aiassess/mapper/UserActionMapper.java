package com.aiassess.mapper;

import com.aiassess.entity.UserAction;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserActionMapper extends BaseMapper<UserAction> {

    /** 去重统计 openid 数量（注册用户数） */
    @Select("SELECT COUNT(DISTINCT openid) FROM user_action")
    long countDistinctOpenid();

    /** 去重统计今日 openid 数量 */
    @Select("SELECT COUNT(DISTINCT openid) FROM user_action WHERE created_at >= #{start}")
    long countDistinctOpenidSince(@Param("start") LocalDateTime start);

    /** 获取所有去重 openid 列表，按首次出现时间倒序 */
    @Select("SELECT openid FROM user_action GROUP BY openid ORDER BY MIN(created_at) DESC")
    List<String> selectDistinctOpenids();

    /** 统计某 openid 在指定时间之后的访问次数（用于行为标签计算） */
    @Select("SELECT COUNT(*) FROM user_action WHERE openid = #{openid} AND created_at >= #{since}")
    long countVisitSince(@Param("openid") String openid, @Param("since") LocalDateTime since);
}
