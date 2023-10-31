package com.bank.controller;

import com.bank.common.Result;
import com.bank.model.Counter;
import com.bank.service.CounterService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CounterController {

    @Autowired
    private CounterService counterService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 添加管理员接口
     *
     * @param counter
     * @return
     */
    @ApiOperation("添加管理员")
    @PostMapping("addCounter")
    public Result addCounter(@RequestBody Counter counter) {
        String password = bCryptPasswordEncoder.encode(counter.getPassword());
        counter.setPassword(password);
        int i = counterService.addCounter(counter);
        return new Result("200", "新增成功！", i);
    }
}
