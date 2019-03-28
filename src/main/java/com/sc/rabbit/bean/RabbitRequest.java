package com.sc.rabbit.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sun7ay
 * Created on  2019-03-25
 */
@Data
public class RabbitRequest implements Serializable {
    private static final long serialVersionUID = 162517932536389341L;
    private String requestId;
    private String className;
    private String methodName;
    private Class<?>[] paramTypes;
    private Object[] params;
}
