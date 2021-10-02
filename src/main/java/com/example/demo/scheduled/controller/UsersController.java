package com.example.demo.scheduled.controller;

import com.example.demo.scheduled.entity.Users;
import com.example.demo.scheduled.mapper.UsersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Create By aobs
 * Date 2021/10/2 9:01
 * Description
 */
@RestController
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private UsersMapper usersMapper;

    /**
     * 添加用户
     * @param users {"username":"211211xxxx" , "password":"xxxxxx"}
     * @return 提示信息
     */
    @PostMapping("/user")
    public String addNewUser(@RequestBody Users users){
        Users selectByUserName = usersMapper.selectByUserName(users.getUsername());
        if (null == selectByUserName){
            usersMapper.insertUser(users);
        } else {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST , "账号已存在");
        }
        return "添加成功";
    }

    /**
     * 查询单个用户
     * @param username 学号
     * @return user
     */
    @GetMapping("/user/{username}")
    public Users selectByUserName(@PathVariable("username") String username){
        return usersMapper.selectByUserName(username);
    }

}
