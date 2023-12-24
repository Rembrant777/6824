package org.emma.rpc.io;

import org.emma.rpc.common.RpcRequest;
import org.emma.rpc.utils.RpcUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;
public class RpcNodeTest {
    private static final Logger LOG = LoggerFactory.getLogger(RpcNodeTest.class);

    private final int THREAD_CNT = 1;

    private RpcNode rpcNode;

    private int serverPort;

    @Before
    public void setUp() {
        LOG.info("#setUp ...");
        rpcNode = new RpcNode();

        try {
            LOG.info("#setUp RpcNode serve start");
            rpcNode.serve();
            this.serverPort = rpcNode.getPort();
            if (this.serverPort > 0) {
                LOG.info("#setUp RpcNode is setup successfully and works on port {}", this.serverPort);
            }
        } catch (Exception e) {
            LOG.error("#setUp failed !", e);
        }

        Assert.assertTrue(Objects.nonNull(rpcNode)
                && Objects.nonNull(rpcNode.getExecutorService())
                && Objects.nonNull(rpcNode.getBossGroup())
                && Objects.nonNull(rpcNode.getWorkGroup()));
    }

    @After
    public void shutDown() {
        LOG.info("#shutDown ... ");

        if (Objects.nonNull(rpcNode)) {
            rpcNode.shutdown();
        }
        rpcNode = null;
    }

    /**
     * Here we execute the testInvoke is trying to mock the process
     * of how server process rpc request(s) from the client.
     */
    @Test
    public void testInvoke() throws Exception {
        RpcRequest rpcRequest = genValidRpcRequest();
        Assert.assertTrue(RpcUtils.isValidRpcRequest(rpcRequest));

        // Call the method under test
        Object retObj = rpcNode.invoke(rpcRequest);

        // here we continue to parser the result
        Assert.assertTrue(Objects.nonNull(retObj));
    }


    /**
     * Here we execute the call method is trying to mimic the process of client side
     * how to create {@link RpcRequest} and send it to the server side and blocked waiting for the server side's response.
     *
     * As a client, suppose it wants to send request(the RpcRequest) to the server side.
     * it should
     * 1. create the {@link RpcRequest} and make sure the request is valid (which can be retrieved and parsed by the server side successsfully)
     * 2. know which server it needs to send request to (know the port).
     */
    @Test
    public void testCall() throws Exception {
        LOG.info("#testCall begin ... ");
        int port = this.serverPort;

        String methodName = getMethodName();
        Object [] paramObjArr = getParamObjArr();
        Object serverResponse = rpcNode.call(port, methodName, paramObjArr);

        Assert.assertNotNull(serverResponse);

        LOG.info("#testCall end ... ");
    }

    // -- private methods --

    /**
     * Method to generate valid {@link RpcRequest}
     */
    private RpcRequest genValidRpcRequest() {
        RpcRequest ret = new RpcRequest();

        ret.setRequestId(UUID.randomUUID().toString());
        ret.setMethodName(getMethodName());
        ret.setClazzName(getClazzName());
        Object[] paramObjArr = getParamObjArr();
        ret.setParameters(paramObjArr);
        ret.setParameterTypes(getParamTypeArr(paramObjArr));

        LOG.info("#genValidRpcRequest ret non-null status {}, valid status {}",
                Objects.nonNull(ret), RpcUtils.isValidRpcRequest(ret));
        return ret;
    }

    private String getMethodName() {
        return "mockMethod";
    }

    private String getClazzName() {
        return RpcNode.class.getName();
    }

    private Object[] getParamObjArr() {
        Object[] retArr = new Object[]{new String("param1-str-mock"), new String("param2-str-mock"),
                new Integer(678)};

        LOG.info("#getParamObjArr ret non-null status {}", Objects.nonNull(retArr));
        return retArr;
    }

    private Class<?>[] getParamTypeArr(Object[] paramObjArr) {
        Assert.assertTrue(Objects.nonNull(paramObjArr) && paramObjArr.length >= 0);
        Class<?>[] ret = RpcUtils.genParameterTypeArr(paramObjArr);
        LOG.info("#getParamTypeArr ret non-null status {}", Objects.nonNull(ret));
        return ret;
    }
}