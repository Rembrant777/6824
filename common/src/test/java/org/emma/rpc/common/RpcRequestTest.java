package org.emma.rpc.common;

import org.emma.rpc.utils.RpcUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

public class RpcRequestTest {
    private RpcRequest rpcRequest = null;

    @Before
    public void init() {
        rpcRequest = genMockRpcRequest();
    }

    @Test
    public void testValidRpcRequest() {
        Assert.assertTrue(RpcUtils.isValidRpcRequest(this.rpcRequest));
    }

    @Test
    public void testInvalidRpcRequest() {
        rpcRequest.setClazzName(null);
        Assert.assertFalse(RpcUtils.isValidRpcRequest(rpcRequest));
        rpcRequest.setClazzName(this.getClass().getName());
    }

    // -- private method --
    private RpcRequest genMockRpcRequest() {
        RpcRequest ans = new RpcRequest();
        ans.setRequestId(UUID.randomUUID().toString());
        ans.setClazzName(this.getClass().getName());
        Object[] argArr = new Object[]{new Integer(1234), new String("test")};
        ans.setParameters(argArr);
        ans.setMethodName(UUID.randomUUID().toString());
        ans.setParameterTypes(RpcUtils.genParameterTypeArr(argArr));
        Assert.assertTrue(RpcUtils.isValidRpcRequest(ans));
        return ans;
    }
}