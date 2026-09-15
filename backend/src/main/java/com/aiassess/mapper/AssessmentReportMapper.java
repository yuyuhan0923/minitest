package com.aiassess.mapper;

import com.aiassess.entity.AssessmentReport;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AssessmentReportMapper extends BaseMapper<AssessmentReport> {
    @Select("SELECT * FROM assessment_report WHERE session_id = #{sessionId} LIMIT 1")
    AssessmentReport findBySessionId(Long sessionId);

    @Select("SELECT * FROM assessment_report WHERE openid = #{openid} ORDER BY created_at DESC")
    java.util.List<AssessmentReport> findByOpenid(String openid);
}
