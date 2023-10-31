package com.bank.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("counter")
public class Counter {
    private Integer id;
    private String username;
    private String password;
    @TableField("user_real_name")
    private String userRealName;
    @TableField(value = "creat_time", fill = FieldFill.INSERT)
    private Date creatTime;
    @TableField(value = "update_time",fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    /**
     * 默认值为0
     */
    @TableField(value = "account_non_locked", insertStrategy = FieldStrategy.DEFAULT)
    private Integer accountNonLocked = 0;
}
