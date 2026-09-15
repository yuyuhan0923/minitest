package com.aiassess.mapper;

import com.aiassess.entity.Lead;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LeadMapper extends BaseMapper<Lead> {
    @Select("SELECT COUNT(*) FROM `lead` WHERE openid = #{openid}")
    int countByOpenid(String openid);
}
