package org.emma.rpc.io;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@Data
public class RpcChannelInboundHandlerAdapter extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(RpcChannelInboundHandlerAdapter.class);

    // this is the reference of the rpc server instance
    private RpcNode rpcServerInstanceRef;
    private Object handleResult;
    public RpcChannelInboundHandlerAdapter(RpcNode rpcServerInstance) {
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
        Object ret = rpcServerInstanceRef.invoke(obj);
        this.handleResult = ret;
        LOG.info("#channelRed got ret non-null status {}", Objects.nonNull(ret));
        ctx.writeAndFlush(ret);
    }
}
