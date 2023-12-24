package org.emma.rpc.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class StringUtilsTest {
    @Test
    public void testIsNotEmpty() {
        String str = UUID.randomUUID().toString();
        Assert.assertTrue(StringUtils.isNotEmpty(str));
    }

    @Test
    public void testIsEmptyOrNull() {
        String str1 = "";
        String str2 = null;
        String str3 = "    ";

        Assert.assertTrue(StringUtils.isEmptyOrNull(str1)
        && StringUtils.isEmptyOrNull(str2)
        && StringUtils.isEmptyOrNull(str3));
    }
}
