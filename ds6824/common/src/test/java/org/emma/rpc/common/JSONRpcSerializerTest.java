package org.emma.rpc.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;

public class JSONRpcSerializerTest {
    private static final Logger LOG = LoggerFactory.getLogger(JSONRpcSerializerTest.class);

    private static JSONRpcSerializer serializer;

    @BeforeAll
    public static void init() {
        serializer = new JSONRpcSerializer();
        Assert.assertTrue(Objects.nonNull(serializer));
    }

    @Test
    public void testJsonSerializer_SerializerOperation() throws Exception {
        MockJsonObj obj = genMockJsonObj();
        Assert.assertTrue(Objects.nonNull(obj));

        // try to serialize without json serializer
        byte[] byteArr = obj.toString().getBytes();
        byte[] jsonByteArr = serializer.serialize(obj);

        Assert.assertTrue(Objects.nonNull(byteArr)
                && Objects.nonNull(jsonByteArr)
                && byteArr.length > jsonByteArr.length);
    }


    @Test
    public void testJsonSerializer_DeSerializerOperation() throws Exception {
        MockJsonObj tobeSerializeObj = genMockJsonObj();
        Assert.assertTrue(Objects.nonNull(tobeSerializeObj));

        byte[] serializerByteArr = serializer.serialize(tobeSerializeObj);
        Assert.assertTrue(Objects.nonNull(serializerByteArr) && serializerByteArr.length > 0);

        MockJsonObj deserializeObj = serializer.deserialize(MockJsonObj.class, serializerByteArr);

        Assert.assertTrue(Objects.nonNull(deserializeObj) && Objects.nonNull(deserializeObj.getEmbeddedObj()));
    }


    private MockJsonObj genMockJsonObj() {
        MockJsonObj obj = new MockJsonObj();
        EmbeddedObj embObj = new EmbeddedObj();
        embObj.setId(UUID.randomUUID().toString());
        embObj.setName(UUID.randomUUID().toString());
        obj.setId(UUID.randomUUID().toString());
        obj.setTimestamp(234567890L);
        obj.setEmbeddedObj(embObj);
        obj.setValue(234567890L);
        LOG.info("#genMockJsonObj ret obj non-null status {}", Objects.nonNull(obj));
        return obj;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    static class MockJsonObj {
        public String id;
        public Long value;
        public String name;
        public EmbeddedObj embeddedObj;
        public Long timestamp;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    static class EmbeddedObj {
        public String name;
        public String id;
    }
}