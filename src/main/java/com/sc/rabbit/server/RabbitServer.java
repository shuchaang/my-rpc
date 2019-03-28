package com.sc.rabbit.server;

import com.sc.rabbit.bean.RabbitRequest;
import com.sc.rabbit.bean.RabbitResponse;
import com.sc.rabbit.codec.RpcDecoder;
import com.sc.rabbit.codec.RpcEncoder;
import com.sc.rabbit.handler.RpcHandler;
import com.sc.rabbit.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shuchang
 * Created on  2019-03-27
 */
@Slf4j
public class RabbitServer implements ApplicationContextAware, InitializingBean {
    private String address;
    private ServiceRegistry serviceRegistry;
    /**
     * 接口名与服务
     */
    private Map<String,Object> handlerMap = new HashMap<String, Object>();

    public RabbitServer(String address,ServiceRegistry serviceRegistry){
        this.address=address;
        this.serviceRegistry=serviceRegistry;
    }



    @Override
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new RpcDecoder(RabbitRequest.class));
                            pipeline.addLast(new RpcEncoder(RabbitResponse.class));
                            pipeline.addLast(new RpcHandler(handlerMap));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            String[] split = address.split(":");
            String ip = split[0];
            int port = Integer.parseInt(split[1]);
            ChannelFuture future = serverBootstrap.bind(ip, port).sync();
            log.info("server start on {} : {}", ip, port);
            if (serviceRegistry != null) {
                serviceRegistry.register(address);
                log.info("server register address {}", address);
            }
            future.channel().closeFuture().sync();
        }catch (Exception e){
            log.error("server start error {}",e);
        }finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(RabbitService.class);
        if(beansWithAnnotation!=null&&beansWithAnnotation.size()>0){
            for (Object value : beansWithAnnotation.values()) {
                String name = value.getClass().getAnnotation(RabbitService.class).value().getName();
                handlerMap.put(name,value);
            }
        }
    }
}
