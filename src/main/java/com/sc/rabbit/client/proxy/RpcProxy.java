package com.sc.rabbit.client.proxy;

import com.sc.rabbit.bean.RabbitRequest;
import com.sc.rabbit.bean.RabbitResponse;
import com.sc.rabbit.client.RpcClient;
import com.sc.rabbit.registry.ServiceDiscover;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * @author shuchang
 * Created on  2019-03-27
 */
@Slf4j
public class RpcProxy {


    private String address;

    private ServiceDiscover discover;


    public RpcProxy(ServiceDiscover discover){
        this.discover=discover;
    }

    public <T> T createProxy(final Class<?> clz){
        return (T) Proxy.newProxyInstance(clz.getClassLoader(), new Class<?>[]{clz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                RabbitRequest request = new RabbitRequest();
                request.setRequestId(UUID.randomUUID().toString());
                request.setClassName(method.getDeclaringClass().getName());
                request.setMethodName(method.getName());
                request.setParamTypes(method.getParameterTypes());
                request.setParams(args);


                if(null!=discover){
                    address = discover.discover();
                }
                if(address==null){
                    throw new IllegalArgumentException("无法获取远程服务");
                }
                String[] split = address.split(":");
                String ip = split[0];
                int port = Integer.parseInt(split[1]);

                RpcClient client = new RpcClient(ip,port);

                RabbitResponse response = client.send(request);
                log.info("获取响应-{}",response);
                if(response!=null){
                    if(response.getError()!=null){
                        throw response.getError();
                    }else{
                        return response.getResult();
                    }
                }else{
                    throw new IllegalArgumentException("无响应");
                }
            }
        });
    }

}