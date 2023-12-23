package org.emma.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class RpcEncoder extends MessageToByteEncoder {
    private static final Logger LOG = LoggerFactory.getLogger(RpcEncoder.class);

    private Class<?> clazz;

    private RpcSerializer rpcSerializer;

    public RpcEncoder(Class<?> clazz, RpcSerializer rpcSerializer) {
        this.clazz = clazz;
        this.rpcSerializer = rpcSerializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object obj, ByteBuf byteBuf) throws Exception {
        LOG.info("#encode recv obj non-null status {}, byte buf non-null status {}",
                Objects.nonNull(obj), Objects.nonNull(byteBuf));

        // here we try to encode the object instance into the byte buffer array via rpc serializer
        if (Objects.nonNull(clazz) && clazz.isInstance(obj)) {
            byte[] byteArr = rpcSerializer.serialize(obj);

            // first, write the length of the byte array to the byte buf first
            byteBuf.writeInt(byteArr.length);

            // then, write the serialized byte array to the byte buffer
            byteBuf.writeBytes(byteArr);
        }
    }
}
