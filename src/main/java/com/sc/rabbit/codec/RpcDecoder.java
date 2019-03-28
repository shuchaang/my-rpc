package com.sc.rabbit.codec;

import com.sc.rabbit.bean.RabbitRequest;
import com.sc.rabbit.utils.SerialUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author sun7ay
 * Created on  2019-03-26
 */
@Slf4j
public class RpcDecoder extends ByteToMessageDecoder {


    private Class<?> clazz;

    public RpcDecoder(Class<?> clz){
        this.clazz=clz;
    }


    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) throws Exception {
        //这里要根据编码来做解码,前4个字节写了报文长度
        log.error("解码");
        if(in.readableBytes()<4){
            return;
        }
        in.markReaderIndex();
        int dataLength =  in.readInt();
        if(dataLength<0){
            channelHandlerContext.close();
        }
        if(in.readableBytes()<dataLength){
            in.resetReaderIndex();
            return;
        }
        byte[] data =new  byte[dataLength];
        in.readBytes(data);

        Object obj = SerialUtils.deSerialize(data, clazz);
        list.add(obj);
    }
}
