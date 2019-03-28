package com.sc.rabbit.client;

import com.sc.rabbit.bean.RabbitRequest;
import com.sc.rabbit.bean.RabbitResponse;
import com.sc.rabbit.codec.RpcDecoder;
import com.sc.rabbit.codec.RpcEncoder;
import io.netty.bootstrap.Bootstrap;

import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author shuchang
 * Created on  2019-03-27
 */
@Slf4j
public class RpcClient extends SimpleChannelInboundHandler<RabbitResponse> {

    private String host;
    private int port;
    private RabbitResponse response;

    private final Object lock = new Object();


    public RpcClient(String host,int port){
        this.host=host;
        this.port=port;
    }



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RabbitResponse response) throws Exception {
        this.response=response;
        synchronized (lock){
            lock.notifyAll();
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("rpcClient-{}",cause);
        ctx.close();
    }


    public RabbitResponse send(RabbitRequest request) throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            //创建并初始化Netty客户端bootstrap对象
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    /*将RPC请求进行编码（发送请求）*/
                                    .addLast(new RpcEncoder(RabbitRequest.class))
                                    /*将RPC响应进行解码（返回响应）*/
                                    .addLast(new RpcDecoder(RabbitResponse.class))
                                    /*使用RpcClient发送RPC请求*/
                                    .addLast(RpcClient.this);
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);

            //连接RPC服务器
            ChannelFuture future = bootstrap.connect(host, port).sync();
            //写入RPC请求数据
            future.channel().writeAndFlush(request).sync();
            synchronized (lock){
                lock.wait();
            }
            if(null!=response){
                future.channel().closeFuture().sync();
            }
            return response;

        }finally {
            group.shutdownGracefully();
        }

    }
}
