package org.emma.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class RpcEncoderTest {
    private RpcSerializer rpcSerializer;

    private RpcEncoder rpcEncoder;

    @Before
    public void init() {
        this.rpcSerializer = new JSONRpcSerializer();
        Assert.assertTrue(Objects.nonNull(rpcSerializer));

        this.rpcEncoder = new RpcEncoder(RpcEncoderMockObj.class, this.rpcSerializer);
        Assert.assertTrue(Objects.nonNull(rpcEncoder));
    }

    @Test
    public void testEncode() throws Exception {
        RpcEncoderMockObj mockObj = new RpcEncoderMockObj();
        EmbeddedRpcEncoderMockObj embeddedRpcEncoderMockObj = new EmbeddedRpcEncoderMockObj();
        mockObj.setEmbeddedObj(embeddedRpcEncoderMockObj);
        mockObj.setId(UUID.randomUUID().toString());
        mockObj.setName(UUID.randomUUID().toString());

        ByteBuf byteBuf = Unpooled.buffer();
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);

        rpcEncoder.encode(ctx, mockObj, byteBuf);

        // 153
        int encodedObjLen = byteBuf.readInt();

        Assert.assertTrue(encodedObjLen > 0);

        // allocate space for byte array to hold byte data from encoder
        byte [] byteArr = new byte[encodedObjLen];
        byteBuf.readBytes(byteArr);

        RpcEncoderMockObj ret = rpcSerializer.deserialize(RpcEncoderMockObj.class, byteArr);
        Assert.assertTrue(Objects.nonNull(ret) && Objects.nonNull(ret.getEmbeddedObj()));
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    static class RpcEncoderMockObj {
        String name;
        String id;
        EmbeddedRpcEncoderMockObj embeddedObj;
    }

    @AllArgsConstructor
    @Data
    static class EmbeddedRpcEncoderMockObj {
        String uuid;

        public EmbeddedRpcEncoderMockObj() {
            this.uuid = UUID.randomUUID().toString();
        }
    }

}