package org.emma.rpc.io;

import lombok.Data;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.emma.rpc.common.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.Callable;

@Data
public class RpcChannel extends ChannelInboundHandlerAdapter implements Callable {
    private static final Logger LOG = LoggerFactory.getLogger(RpcChannel.class);

    private ChannelHandlerContext context;
    private String result;
    private RpcRequest request;
    private int serverPort;

    public RpcChannel(int port) {
        this.serverPort = port;
    }


    /**
     * Method will be invoked once the channel is established to the server side.
     *
     * @param ctx the reference of the channel context
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("#channelActive invoked recv ctx non-null status {}", Objects.nonNull(ctx));
        this.context = ctx;
    }

    /**
     * Method will be invoked as the channel read operation is triggered
     */
    @Override
    public synchronized void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // todo: object converted to message object to be added here (may be later ...)
        this.result = msg.toString();
        notify();
    }

    @Override
    public synchronized Object call() throws Exception {
        LOG.info("#call recv request non-null status {}", Objects.nonNull(request));
        context.writeAndFlush(request);
        wait();
        return result;
    }

    public void setRequest(RpcRequest request) {
        this.request = request;
    }
}
