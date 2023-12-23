package org.emma.rpc.io;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.internal.StringUtil;
import org.emma.rpc.common.RpcRequest;
import org.emma.rpc.exception.InvalidRpcRequest;
import org.emma.rpc.utils.RpcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Objects;

import static org.emma.rpc.utils.RpcUtils.isValidRpcRequest;

public class RpcChannelInboundHandlerAdapter extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(RpcChannelInboundHandlerAdapter.class);

    private Object rpcServerInstance;

    public RpcChannelInboundHandlerAdapter(Object rpcServerInstance) {
        this.rpcServerInstance = rpcServerInstance;
    }

    /**
     * RPCNode's channel received message only can be the client side's
     * rpc request which contains the method name, actual server class name, parameter type list and parameter object list.
     *
     * RPCNode channel read method handles the client side's request and invoke server's method to execute it.
     * and then wrap the method execute result to the channel back to the client side as response.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object obj) throws Exception {
        LOG.info("#channelRead recv obj non-null status {}", Objects.nonNull(obj));
        ctx.writeAndFlush(invoke(obj));
    }

    /**
     * server side's request executor
     * @param obj  client side's rpc request which wraps method name, method parameter type list and method parameter list.
     * @return server side's method invoke result
     */
    private Object invoke(Object obj) throws Exception {
        String ret = "";

        if (Objects.isNull(obj) || !(obj instanceof RpcRequest)) {
            throw new Exception("#invoke recv obj is null or not an instance of the required RpcRequest");
        }

        /**
         * RpcRequest contains 3 important parameters.
         * first, is the method name that the client side want to invoke on the server side.
         * second, is the parameter type list which defines the actual method that defined on the server side.
         * the last, is the parameters that gonna passing to the server side's method
         */
        RpcRequest rpcRequest = (RpcRequest) obj;

        if (RpcUtils.isValidRpcRequest(rpcRequest)) {
            throw new InvalidRpcRequest();
        }

        // here we retrieve the class (actually the specified class in the sub-class
        // set of {Master.class, SampleServer.class or the Worker.class})
        // of the server class. Cuz the invoked method depends on the actual class which extends RpcNode.class.
        Class<?> serverClass = rpcServerInstance.getClass();
        String methodName = rpcRequest.getMethodName();
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Method method = serverClass.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        Object[] parameters = rpcRequest.getParameters();

        // invoke the server side's method via reflection
        Object retObj = method.invoke(rpcServerInstance, parameters);

        ret = JSON.toJSONString(retObj);
        LOG.info("#invoke ret non-blank status {}", !StringUtil.isNullOrEmpty(ret));
        return ret;
    }

    /**
     * As we just talked about the class RPCNode will be implemented by several classes like
     * the Master -- the server, when Master extends the RpcNode it will invoke the invoke method,
     *               that handles the RpcRequest from the client (Worker) side, invoke the method on the server side, and
     *               then wrap the execution result to the response body back to the client side.
     *
     * the Worker -- the client, when Worker extends the RpcNode it will invoke this call method,
     *              that wrap the local context variables and info like parameter type list, parameter object list and the method name
     *              into the RpcRequest.
     *              And then send the request to the server (Master) side to handle and waiting for the response body and then extract
     *              via some standard to extract the result value from the response body.
     *
     * Master and Worker both extends this class as parent class, but execute different methods.
     * Master -- invoke
     * Worker -- call
     */
    public Object call(int port, String methodName, Object [] args) {
        Object ret = null;
        RpcRequest request = new RpcRequest();
        // here we build a request that encapsulate context variables and info into it.


        LOG.info("#call ret non-null status {}", Objects.nonNull(ret));
        return ret;
    }
}
