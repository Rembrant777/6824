package org.emma.rpc.io;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import org.emma.rpc.common.JSONRpcSerializer;
import org.emma.rpc.common.RpcEncoder;
import org.emma.rpc.common.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class RpcChannelWorkerInitializer extends ChannelInitializer<SocketChannel> {
    private static final Logger LOG = LoggerFactory.getLogger(RpcChannelWorkerInitializer.class);

    private RpcChannel rpcChannel;

    public RpcChannelWorkerInitializer(RpcChannel rpcChannel) {
        this.rpcChannel = rpcChannel;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        LOG.info("#initChannel ch non-null status {}", Objects.nonNull(ch));

        ChannelPipeline pipeline = ch.pipeline();
        // first we need to add an encoder that coverts the client's rpc request instance object into byte array
        pipeline.addFirst(new RpcEncoder(RpcRequest.class, new JSONRpcSerializer()));
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(rpcChannel);
    }
}
