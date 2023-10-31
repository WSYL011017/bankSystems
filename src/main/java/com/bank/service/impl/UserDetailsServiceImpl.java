package com.bank.service.impl;

import com.bank.mapper.CounterMapper;
import com.bank.model.Counter;
import com.bank.model.SysUser;
import com.bank.service.CounterService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private CounterMapper counterMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户的认证信息：用户名、密码
        Counter counter = counterMapper.selectOne(
                new LambdaQueryWrapper<Counter>().eq(Counter::getUsername,username));
        // 使用实现了UserDetail接口的实现类去封装认证信息
        SysUser sysUser = new SysUser();
        sysUser.setUsername(counter.getUsername());
        sysUser.setPassword(counter.getPassword());
        return sysUser;
    }
}
