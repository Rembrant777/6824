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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.mockito.Mockito.mock;


public class RpcDecoderTest {
    private static final Logger LOG = LoggerFactory.getLogger(RpcDecoderTest.class);
    private RpcSerializer rpcSerializer;
    private RpcDecoder rpcDecoder;

    @Before
    public void init () {
        this.rpcSerializer = new JSONRpcSerializer();
        Assert.assertTrue(Objects.nonNull(rpcSerializer));
        this.rpcDecoder = new RpcDecoder(RpcDecoderMockObj.class, rpcSerializer);
        Assert.assertTrue(Objects.nonNull(this.rpcDecoder));
    }

    @Test
    public void testDecode() throws Exception {
        RpcDecoderMockObj mockObj = new RpcDecoderMockObj();
        EmbeddedRpcDecoderMockObj embeddedMockObj = new EmbeddedRpcDecoderMockObj();
        mockObj.setEmbeddedObj(embeddedMockObj);
        mockObj.setId(UUID.randomUUID().toString());
        mockObj.setName(UUID.randomUUID().toString());

        ByteBuf byteBuf = Unpooled.buffer();
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        List<Object> objectList = new ArrayList<>();

        // here we serialize the object into the byte buffer and also write the length to the byte buffer
        byte [] byteArr = rpcSerializer.serialize(mockObj);
        int byteArrLen = byteArr.length;

        // first write the length of the byte array
        byteBuf.writeInt(byteArrLen);

        // then write the complete byte array to the ByteBuf this buffer of the memory
        byteBuf.writeBytes(byteArr);

        // at last, because we wanna check our rpc decoder works fine, we need to passing our
        // already serializer data of the byte buf to the decoder to handle
        rpcDecoder.decode(ctx, byteBuf, objectList);

        // this decoder do  not have the return value, it holds all the decoded object instance to the object list
        // we just retrieve the decoded object from the object instance list and check is ok
        Assert.assertTrue(Objects.nonNull(objectList) && objectList.size() == 1);
        Object retObj = objectList.get(0);

        Assert.assertTrue(retObj instanceof RpcDecoderMockObj);
        RpcDecoderMockObj decoderMockObj = (RpcDecoderMockObj) retObj;

        // finally, we just check whether the before embedded object's uuid value is
        // match with the encoded && decoded recovered instance's uuid
        Assert.assertTrue(decoderMockObj.getEmbeddedObj().getUuid().equals(embeddedMockObj.getUuid()));
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    static class RpcDecoderMockObj {
        String name;
        String id;
        EmbeddedRpcDecoderMockObj embeddedObj;
    }

    @AllArgsConstructor
    @Data
    static class EmbeddedRpcDecoderMockObj {
        String uuid;

        public EmbeddedRpcDecoderMockObj() {
            this.uuid = UUID.randomUUID().toString();
        }
    }

}