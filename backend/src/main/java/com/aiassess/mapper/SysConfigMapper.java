package com.aiassess.mapper;

import com.aiassess.entity.SysConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {
    @Select("SELECT * FROM sys_config WHERE config_key = #{key}")
    SysConfig findByKey(String key);
}
