package com.tomcat360.lyqb.core.model.loopr.rsp;


import lombok.Data;

@Data
public class Response<T> {
    private String id;
    private String jsonrpc;
    private T result;

    private String error;

}
