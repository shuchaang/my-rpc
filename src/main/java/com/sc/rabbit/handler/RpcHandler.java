package com.sc.rabbit.handler;

import com.sc.rabbit.bean.RabbitRequest;
import com.sc.rabbit.bean.RabbitResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**  在这里卡了很久,因为反序列化方法有问题,导致反序列化的结果类型不是RabbitRequest,所以一直没有进入这个handler
 * @author shuchang
 * Created on  2019-03-27
 */
@Slf4j
public class RpcHandler extends SimpleChannelInboundHandler<RabbitRequest> {

    private final Map<String,Object> handler;

    public RpcHandler(Map<String, Object> handler) {
        this.handler = handler;
    }



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RabbitRequest request) throws Exception {
        RabbitResponse response = new RabbitResponse();
        response.setResponseId(request.getRequestId());
        try {
            Object res = handle(request);
            response.setResult(res);
        }catch (Exception e){
            response.setError(e);
        }
        log.info("return response: {}",response);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private Object handle(RabbitRequest request) throws InvocationTargetException {
        String className = request.getClassName();
        Object serviceBean = handler.get(className);
        Class<?> beanClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] paramTypes = request.getParamTypes();
        Object[] params = request.getParams();
        FastClass fastClass = FastClass.create(beanClass);
        FastMethod method = fastClass.getMethod(methodName,paramTypes);
        return method.invoke(serviceBean,params);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server handle error {}",cause);
        ctx.close();
    }
}
