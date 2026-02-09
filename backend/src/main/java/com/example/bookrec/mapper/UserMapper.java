package com.example.bookrec.mapper;

import com.example.bookrec.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author Admin
 * @since 2026-01-26
 */
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT id FROM sys_user " +
            "WHERE username_prefix_hash = CRC32(LEFT(#{username}, 7)) " +
            "  AND username LIKE CONCAT(#{username}, '%') " +
            "LIMIT 1000")
    List<Long> getIdOnly(@Param("username") String username);
}
