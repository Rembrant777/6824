package org.emma.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Objects;

public class RpcDecoder extends ByteToMessageDecoder {
    private static final Logger LOG = LoggerFactory.getLogger(RpcDecoder.class);

    private Class<?> clazz;
    private RpcSerializer rpcSerializer;

    public RpcDecoder(Class<?> clazz, RpcSerializer rpcSerializer) {
        this.clazz = clazz;
        this.rpcSerializer = rpcSerializer;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf,
                          List<Object> list) throws Exception {
        if (Objects.isNull(byteBuf)) {
            LOG.info("#decode recv msg is empty close the channel");
            channelHandlerContext.close();
        }
        // retrieve the length of received message length
        int byteLen = byteBuf.readInt();
        LOG.info("#decode received msg len {}", byteLen);

        // extract the data from the message
        byte [] bytes = new byte[byteLen];
        byteBuf.readBytes(bytes);
        // converted bytes into the specified object
        Object obj = rpcSerializer.deserialize(clazz, bytes);
        list.add(obj);
        LOG.info("#decode list len {}", list.size());
    }
}
