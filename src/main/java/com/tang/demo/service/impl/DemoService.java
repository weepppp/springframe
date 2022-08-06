package com.tang.demo.service.impl;

import com.tang.demo.service.IDemoService;
import com.tang.mvcframework.annotation.Service;

/**
 * @author weepppp 2022/8/6 8:43
 * 定义实现类
 * 核心业务逻辑
 **/
@Service
public class DemoService implements IDemoService {
    @Override
    public String get(String name) {
        return "my name is " + "\t" + name;
    }
}
