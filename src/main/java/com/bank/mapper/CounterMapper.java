package com.bank.mapper;

import com.bank.model.Counter;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

@Repository
public interface CounterMapper extends BaseMapper<Counter> {
}
