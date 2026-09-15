package com.aiassess.mapper;

import com.aiassess.entity.ExamSession;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;

@Mapper
public interface ExamSessionMapper extends BaseMapper<ExamSession> {

    @Select("SELECT COUNT(*) FROM exam_session WHERE openid = #{openid} AND status = 1")
    long countCompletedByOpenid(@Param("openid") String openid);

    @Select("SELECT COUNT(*) FROM exam_session WHERE openid = #{openid} AND status = 1 AND created_at >= #{start}")
    long countCompletedTodayByOpenid(@Param("openid") String openid, @Param("start") LocalDateTime start);

    /** 查询某 openid 最近一次完成的测评（取 total_score 和 created_at） */
    @Select("SELECT * FROM exam_session WHERE openid = #{openid} AND status = 1 ORDER BY created_at DESC LIMIT 1")
    ExamSession findLatestCompleted(@Param("openid") String openid);
}
