package com.example.demo.scheduled.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Create By aobs
 * Date 2021/10/1 17:08
 * Description
 */

@Data
public class Users {

    @NotNull
    @TableField("username")
    private String username;

    @NotNull
    @TableField("password")
    private String password;

    @TableField("sendKey")
    private String sendKey;

}
