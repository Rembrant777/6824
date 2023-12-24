package org.emma.rpc.common;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class JSONRpcSerializer implements RpcSerializer {
    private static final Logger LOG = LoggerFactory.getLogger(JSONRpcSerializer.class);

    @Override
    public byte[] serialize(Object obj) throws IOException {
        byte[] ret = null;
        if (Objects.nonNull(obj)) {
            ret = JSON.toJSONBytes(obj);
        }
        LOG.info("#serialize ret non-null status {}", Objects.nonNull(ret));
        return ret;
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException {
        T ret = null;
        if (Objects.nonNull(bytes) && bytes.length > 0) {
            ret = JSON.parseObject(bytes, clazz);
        }
        LOG.info("#deserialize ret non-null status {}", Objects.nonNull(ret));
        return ret;
    }
}
