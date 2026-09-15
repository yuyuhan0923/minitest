package com.aiassess.mapper;

import com.aiassess.entity.ExamAnswer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ExamAnswerMapper extends BaseMapper<ExamAnswer> {
    @Select("SELECT * FROM exam_answer WHERE session_id = #{sessionId}")
    List<ExamAnswer> findBySessionId(Long sessionId);
}
