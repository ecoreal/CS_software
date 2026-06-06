package com.bengebackend.mapper;

import com.bengebackend.model.User;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Optional;

public interface UserMapper {

    // 查询所有用户
    List<User> findAll();

    // 根据ID查询用户
    Optional<User> findById(int id);

    // 根据用户名查询用户
    Optional<User> findByUsername(String username);

    // 插入新用户
    int insertUser(User user);

    // 更新用户
    int updateUser(User user);

    // 删除用户
    int deleteUser(int id);
}
