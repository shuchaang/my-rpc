package com.sc.rabbit.utils;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sun7ay
 * Created on  2019-03-26
 */
public final class SerialUtils {
    private SerialUtils(){}

    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<Class<?>, Schema<?>>();
    private static Objenesis objenesis = new ObjenesisStd(true);


    private static <T> Schema<T> getScema(Class<T> clz){
        Schema<T> schema = (Schema<T>) cachedSchema.get(clz);
        if(schema==null){
            schema = RuntimeSchema.createFrom(clz);
            if(null!=schema){
                cachedSchema.put(clz,schema);
            }
        }
        return schema;
    }


    /**
     * 序列化
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> byte[] serialize(T obj) {
        Class<T> clz = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getScema(clz);
            return ProtobufIOUtil.toByteArray(obj, schema, buffer);
        }catch (Exception e){
            throw new IllegalArgumentException();
        }finally {
            buffer.clear();
        }
    }



    public static <T> T deSerialize(byte[] data,Class<T> clz){
        T msg = (T) objenesis.newInstance(clz);
        Schema<T> schema = getScema(clz);
        ProtobufIOUtil.mergeFrom(data,msg,schema);
        return msg;
    }
}
