package org.emma.rpc.io;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import org.emma.rpc.common.JSONRpcSerializer;
import org.emma.rpc.common.RpcDecoder;
import org.emma.rpc.common.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcChannelInitializer extends ChannelInitializer<NioSocketChannel> {
    private static final Logger LOG = LoggerFactory.getLogger(RpcChannelInitializer.class);
    private Object rpcServerInstance;

    public RpcChannelInitializer(Object rpcServerInstance) {
        this.rpcServerInstance = rpcServerInstance;
    }

    @Override
    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
        LOG.info("#initChannel begin...");
        ChannelPipeline pipeline = nioSocketChannel.pipeline();
        pipeline.addFirst(new StringEncoder());
        pipeline.addLast(new RpcDecoder(RpcRequest.class, new JSONRpcSerializer()));

        // here we pass the rpc node (the server object instance to the channel handle adapter)
        // todo: this better replaced by the protocol
        pipeline.addLast(new RpcChannelInboundHandlerAdapter(rpcServerInstance));

        LOG.info("#initChannel end...");
    }
}
