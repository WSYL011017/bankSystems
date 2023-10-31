package com.bank.config;


import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Configuration;
import sun.util.resources.LocaleData;

import java.util.Date;

/**
 * 配置类：
 * 是MybatisPlus框架自动添加日期时间的功能类
 */
@Configuration
public class MyMetaObjectHandler implements MetaObjectHandler {
    /**
     * 添加日期
     * 当添加数据的时候、会自动调用次方法
     *
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName("creatTime", new Date(), metaObject);
        this.setFieldValByName("updateTime", new Date(), metaObject);
    }

    /**
     * 修改时间
     *
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime", new Date(), metaObject);
    }
}
