package com.aiassess.mapper;

import com.aiassess.entity.AdminUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
    @Select("SELECT * FROM admin_user WHERE username = #{username} LIMIT 1")
    AdminUser findByUsername(String username);
}
