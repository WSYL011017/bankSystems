package com.bank.service.impl;

import com.bank.mapper.CounterMapper;
import com.bank.model.Counter;
import com.bank.service.CounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CounterServiceImpl implements CounterService {
    @Autowired
    private CounterMapper counterMapper;

    @Override
    public int addCounter(Counter counter) {
        return counterMapper.insert(counter);
    }
}
