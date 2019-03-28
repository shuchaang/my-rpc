package com.sc.rabbit.codec;

import com.sc.rabbit.utils.SerialUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author sun7ay
 * Created on  2019-03-26
 */
@Slf4j
public  class RpcEncoder extends MessageToByteEncoder {

    private Class<?> aClass;

    public RpcEncoder(Class<?> aClass){
        this.aClass = aClass;
    }


    @Override
    protected void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        log.error("编码");
        if(aClass.isInstance(in)){
            byte[] data = SerialUtils.serialize(in);
            //4个字节的头
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}
