package org.emma.rpc.utils;

import org.emma.rpc.common.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class RpcUtils {
    private static final Logger LOG = LoggerFactory.getLogger(RpcRequest.class);

    public static boolean isValidRpcRequest(RpcRequest rpcRequest) {
        boolean flag = false;

        String rid = rpcRequest.getRequestId();
        String methodName = rpcRequest.getMethodName();
        String clazzName = rpcRequest.getClazzName();

        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object [] parameterObjs = rpcRequest.getParameters();

        if (StringUtils.isEmptyOrNull(rid)) {
            LOG.info("#isValidRpcRequest flag {}", flag);
            return flag;
        }

        if (StringUtils.isEmptyOrNull(methodName)) {
            LOG.info("#isValidRpcRequest flag {}", flag);
            return flag;
        }

        if (StringUtils.isEmptyOrNull(clazzName)) {
            LOG.info("#isValidRpcRequest flag {}", flag);
            return flag;
        }

        int paramTypeLen = Objects.isNull(parameterTypes) ? 0 : parameterTypes.length;
        int paramObjLen = Objects.isNull(parameterObjs) ? 0 : parameterObjs.length;


        if (paramObjLen == paramTypeLen && paramObjLen == 0) {
            // remote invoke method has no parameters is allowed, not need to verify class match return ok direct!
            LOG.info("#isValidRpcRequest flag {}", true);
            return true;
        }

        if (paramObjLen != paramTypeLen) {
            // remove call with parameter instance len not match to the parameter type length , we do not need to
            // verify the type match or not return invalid rpc request directly!
            LOG.info("#isValidRpcRequest flag {}", flag);
            return flag;
        }

        // here we need to verify the order of the parameter types and the parameter object instance
        // and also the class types should be match
        for (int idx = 0; idx < paramTypeLen; idx++) {
            Object obj = parameterObjs[idx];
            Class<?> clazz = parameterTypes[idx];
            flag = clazz.isInstance(obj);

            if (!flag) {
                LOG.info("#isValidRpcRequest flag {}", flag);
                return flag;
            }
        }

        // if we got here ,we can say that this rpc request is valid
        // 1. all class member variables should not be null or empty
        // 2. parameter types length and type should match to the parameter object instance
        LOG.info("#isValidRpcRequest flag {}", flag);
        return flag;
    }

    public static Class<?>[] genParameterTypeArr(Object[] args) {
        Class<?> [] ret = new Class[args.length];

        for (int idx = 0; idx < args.length; idx++) {
            ret[idx] = args[idx].getClass();
        }

        int len = Objects.nonNull(ret) ? ret.length : 0;
        LOG.info("#genParameterTypeArr ret non-null status {}, len {}", Objects.nonNull(ret), len);
        return ret;
    }

    public static int randPort() {
        int ret = -1;
        int start = 11000, end = 13000;
        ret = (int) ((Math.random() * (end - start)) + start);
        LOG.info("#randPort ret {}", ret);
        return ret;
    }
}
