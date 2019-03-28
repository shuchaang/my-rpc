package com.sc.rabbit;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author shuchang
 * Created on  2019-03-27
 */
public class RpcBoostStrap {
    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("application-provider.xml");
    }
}
