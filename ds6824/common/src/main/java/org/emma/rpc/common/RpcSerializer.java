package org.emma.rpc.common;

import java.io.IOException;

public interface RpcSerializer {

    /**
     * serializer convert java object into the byte array
     * @param obj to be serialized object instance.
     * @return
     */
    byte [] serialize(Object obj) throws IOException;

    /**
     * deserializer that converts the byte array in to the specialized object instance.
     * @param clazz specialized object's class type
     * @param bytes byte array
     * @return object instance of the clazz specified
     */
    <T> T deserialize(Class<T> clazz, byte [] bytes) throws IOException;
}
