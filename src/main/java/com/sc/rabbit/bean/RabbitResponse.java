package com.sc.rabbit.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @author sun7ay
 * Created on  2019-03-25
 */
@Data
public class RabbitResponse implements Serializable {
    private static final long serialVersionUID = 6911306249577441029L;
    private String responseId;
    private Object result;
    private Throwable error;
}
