package org.emma.rpc.io;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.StringUtil;
import org.emma.rpc.common.RpcRequest;
import org.emma.rpc.exception.InvalidRpcRequest;
import org.emma.rpc.utils.RpcUtils;
import org.emma.rpc.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

public class RpcChannelInboundHandlerAdapter extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(RpcChannelInboundHandlerAdapter.class);

    // this is the reference of the rpc server instance
    private Object rpcServerInstanceRef;
    public RpcChannelInboundHandlerAdapter(Object rpcServerInstance) {
        this.rpcServerInstanceRef = rpcServerInstance;
    }

    /**
     * RPCNode's channel received message only can be the client side's
     * rpc request which contains the method name, actual server class name, parameter type list and parameter object list.
     * <p>
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
     *
     * @param obj client side's rpc request which wraps method name, method parameter type list and method parameter list.
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

        if (!RpcUtils.isValidRpcRequest(rpcRequest)) {
            throw new InvalidRpcRequest();
        }

        // here we retrieve the class (actually the specified class in the sub-class
        // set of {Master.class, SampleServer.class or the Worker.class})
        // of the server class. Cuz the invoked method depends on the actual class which extends RpcNode.class.
        Class<?> serverClass = rpcServerInstanceRef.getClass();
        String methodName = rpcRequest.getMethodName();
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Method method = serverClass.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        Object[] parameters = rpcRequest.getParameters();

        // invoke the server side's method via reflection
        Object retObj = method.invoke(rpcServerInstanceRef, parameters);

        ret = JSON.toJSONString(retObj);
        LOG.info("#invoke ret non-blank status {}", !StringUtil.isNullOrEmpty(ret));
        return ret;
    }
}
