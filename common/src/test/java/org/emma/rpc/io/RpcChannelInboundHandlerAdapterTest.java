package org.emma.rpc.io;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.emma.rpc.common.RpcRequest;
import org.emma.rpc.utils.RpcUtils;
import org.emma.rpc.utils.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class RpcChannelInboundHandlerAdapterTest {
    private static final Logger LOG = LoggerFactory.getLogger(RpcChannelInboundHandlerAdapterTest.class);
    @Mock
    private RpcNode mockRpcNode;

    @Mock
    private ChannelHandlerContext mockCtx;

    private RpcChannelInboundHandlerAdapter handler;

    @Before
    public void init() {
        LOG.info("#init ... ");
        MockitoAnnotations.initMocks(this);
        this.handler = new RpcChannelInboundHandlerAdapter(mockRpcNode);
        try {
            when(mockRpcNode.invoke(any(RpcRequest.class))).thenReturn(genMockInvokeRet());
        } catch (Exception e) {
            LOG.error("#init got exp ", e);
        }
    }


    @Test
    public void testHandlerChannelReadWithNonRpcRequestTypeMessage() {
        try {
            this.handler.channelRead(mockCtx, genNonRpcRequestInstance());
        } catch (Exception e) {
            LOG.error("#testHandlerChannelReadWithNonRpcRequestTypeMessage got exp ", e);
        }
    }

    @Test
    public void testHandleChannelReadWithInvalidRpcRequestTypeMessage() {
        try {
            this.handler.channelRead(mockCtx, genInvalidRpcRequestInstance());
        } catch (Exception e) {
            LOG.error("#testHandleChannelReadWithInvalidRpcRequestTypeMessage got exp ", e);
        }
    }

    @Test
    public void testHandleChannelReadWithValidRpcRequestTypeMessage() {
        try {
            this.handler.channelRead(mockCtx, genValidRpcRequestInstance());
        } catch (Exception e) {
            LOG.error("#testHandleChannelReadWithValidRpcRequestTypeMessage got exp ", e);
        }

        Object handleRet = this.handler.getHandleResult();
        // try to convert tht ret back into the specified class
        JSONObject jsonObj = JSONObject.parseObject(handleRet.toString());
        MockJsonObj mockJsonObj = new MockJsonObj();
        mockJsonObj.setCnt(jsonObj.getInteger("cnt"));
        mockJsonObj.setUid(jsonObj.getString("uid"));
        Assert.assertTrue(Objects.nonNull(mockJsonObj));
    }

    @After
    public void shutdown() {
        LOG.info("#shutdown ... ");
    }


    /**
     * method try to return a non {@link RpcRequest} class instance as return value.
     */
    private Object genNonRpcRequestInstance() {
        Object ret = null;
        ret = new String("ERTYUIOFRTYUIOTYUIO");
        LOG.info("#genInvalidRpcRequestInstance non-null status {}", Objects.nonNull(ret));
        return ret;
    }

    /**
     * Method try to create a {@link RpcRequest} instance, but not a valid one.
     */
    private Object genInvalidRpcRequestInstance() {
        Object ret = null;
        RpcRequest request = new RpcRequest();
        request.setRequestId(null);
        request.setParameterTypes(null);
        request.setParameterTypes(null);
        request.setMethodName(null);
        request.setClazzName(null);

        Assert.assertTrue(!RpcUtils.isValidRpcRequest(request) && Objects.nonNull(request));
        ret = request;
        LOG.info("#genInvalidRpcRequestInstance non-null status {}", Objects.nonNull(ret));
        return ret;
    }

    /**
     * Method try to create a {@link RpcRequest} instance and also make sure the {@link RpcRequest}
     * should be valid one which can be recognized by the Server side.
     */
    private Object genValidRpcRequestInstance() {
        Object ret = null;
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        Object[] argsArr = new Object[]{new Integer(67890), new String("%^&*(")};
        request.setParameters(argsArr);
        request.setParameterTypes(RpcUtils.genParameterTypeArr(argsArr));
        request.setMethodName(UUID.randomUUID().toString());
        request.setClazzName(this.getClass().getName());

        Assert.assertTrue(RpcUtils.isValidRpcRequest(request));
        ret = request;
        LOG.info("#genValidRpcRequestInstance ret non-null status {}", Objects.nonNull(ret));
        return ret;
    }


    /**
     * method that try to mock generate response json formatted string value from the {@link RpcNode#invoke(Object)}
     *
     */
    private Object genMockInvokeRet() {
        String ret = null;
        MockJsonObj obj = new MockJsonObj(UUID.randomUUID().toString(), new Random().nextInt());
        ret = obj.toString();
        LOG.info("#genMockInvokeRet ret non-blank status {}", StringUtils.isNotEmpty(ret));
        return ret;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class MockJsonObj {
        public String uid;
        public int cnt;

        public String toString() {
            return JSON.toJSONString(this);
        }
    }
}