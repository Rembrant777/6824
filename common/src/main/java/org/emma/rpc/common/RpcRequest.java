package org.emma.rpc.common;

import lombok.Data;

@Data
public class RpcRequest {
    /**
     * ID of the requested object instance
     */
    private String requestId;

    /**
     * Name of the class name
     */
    private String clazzName;

    /**
     * method name
     */
    private String methodName;

    /**
     * parameter type list
     */
    private Class<?> [] parameterTypes;

    /**
     * parameter object array
     */
    private Object[] parameters;
}
