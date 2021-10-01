package com.example.demo.scheduled.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * Create By aobs
 * Date 2021/10/1 17:08
 * Description
 */

@Data
public class Users {

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("sendKey")
    private String sendKey;

}
