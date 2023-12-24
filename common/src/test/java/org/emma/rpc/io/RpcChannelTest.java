package org.emma.rpc.io;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import org.emma.rpc.common.RpcRequest;
import org.emma.rpc.utils.RpcUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RpcChannelTest {
    private static final Logger LOG = LoggerFactory.getLogger(RpcChannelTest.class);

    // thread pool thread counter
    private static final int THREAD_CNT = 1;
    private static final int THREAD_POOL_SHUTDOWN_WAIT_MS = 15 * 1000;

    private String globalRetValue = null;

    @Mock
    private ChannelHandlerContext mockCtx;

    @Mock
    private ChannelFuture mockChannelFuture;

    private RpcRequest mockRpcRequest;

    @Mock
    private ExecutorService executorService;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        executorService = Executors.newFixedThreadPool(THREAD_CNT);
        mockChannelFuture = mock(ChannelFuture.class);
        mockRpcRequest = genMockRpcRequest();
        Assert.assertTrue(RpcUtils.isValidRpcRequest(mockRpcRequest));
    }

    @After
    public void shutdown() {
        try {
            if (!executorService.awaitTermination(THREAD_POOL_SHUTDOWN_WAIT_MS, TimeUnit.MILLISECONDS)) {
                // if timeout occurs, force shutdown the thread pool
                executorService.shutdown();
            }
        } catch (InterruptedException e) {
            LOG.error("#shutdown shutdown thread pool got exception ", e);
        }
    }

    @Test
    public void testWithCtxMockReturnNull() throws Exception {
        RpcChannel rpcChannel = new RpcChannel(8081);
        rpcChannel.setRequest(mockRpcRequest);

        // here we mock the behavior of the instance of ChannelHandlerContext
        when(mockCtx.writeAndFlush(any(RpcRequest.class))).thenReturn(mockChannelFuture);

        doAnswer(invocation -> {
            LOG.info("#mock invocation set the generate random result to the rpc channel");
            this.globalRetValue = genJsonStrResult();
            rpcChannel.setResult(this.globalRetValue);
            return null;
        }).when(mockChannelFuture).getNow();

        rpcChannel.setContext(mockCtx);

        // here we invoke the thread pool to submit this task
        executorService.submit(() -> {
            try {
                LOG.info("#testWithCtxMockReturnNull invoke rpc channel");
                rpcChannel.call();
            } catch (Exception e) {
                LOG.error("#testWithCtxMockReturnNull submit got exp ", e);
            }
        });
        String result = rpcChannel.getResult();
        Assert.assertEquals(result, this.globalRetValue);
        LOG.info("#testWithCtxMockReturnNull result non-null status {}",
                Objects.nonNull(result));
    }

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

    /**
     * This method will generate a json string formatted
     * string to mock as the response from the server side (ChannelContext)
     */
    private final String genJsonStrResult() {
        String ret = UUID.randomUUID().toString();
        LOG.info("#genJsonStrResult ret {}", ret);
        return ret;
    }
}