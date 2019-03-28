package com.sc.rabbit.service.impl;

import com.sc.rabbit.server.RabbitService;
import com.sc.rabbit.service.HelloService;

/**
 * @author shuchang
 * Created on  2019-03-27
 */
@RabbitService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    public String hello(String name) {
        return name+ " hello ";
    }
}
