package org.emma.rpc.utils;


import org.emma.rpc.common.JSONRpcSerializer;
import org.emma.rpc.common.RpcDecoder;
import org.emma.rpc.common.RpcRequest;
import org.emma.rpc.io.RpcNode;
import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;
import java.util.UUID;

public class RpcUtilsTest {

    @Test
    public void testIsValidRpcRequest() {
        RpcRequest rpcRequest = genDefaultRpcRequest();
        Assert.assertNotNull(rpcRequest);
        rpcRequest.setRequestId(UUID.randomUUID().toString());
        Object[] argArr = genArgArr();
        rpcRequest.setParameters(argArr);
        rpcRequest.setParameterTypes(RpcUtils.genParameterTypeArr(argArr));

        rpcRequest.setClazzName(this.getClass().getName());
        rpcRequest.setMethodName("genArgArr");

        Assert.assertTrue(RpcUtils.isValidRpcRequest(rpcRequest));
    }

    @Test
    public void testGenParameterTypeArr() {
        Object[] objArr = new Object[]{new RpcUtilsTest(), new RpcUtils(),
                new RpcNode(), new RpcDecoder(null, null),
                new JSONRpcSerializer()};

        Assert.assertTrue(Objects.nonNull(objArr) && objArr.length > 0);
        Class<?>[] clazzArr = RpcUtils.genParameterTypeArr(objArr);
        Assert.assertTrue(Objects.nonNull(clazzArr) && clazzArr.length > 0
                && clazzArr.length == objArr.length);
    }

    private Object[] genArgArr() {
        return new Object[]{new Integer(789), new String("Test"),
                new Double(233.2)};
    }

    private RpcRequest genDefaultRpcRequest() {
        RpcRequest request = new RpcRequest();
        return request;
    }
}