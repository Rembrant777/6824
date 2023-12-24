package org.emma.rpc.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class StringUtils {
    private static final Logger LOG = LoggerFactory.getLogger(StringUtils.class);
    public static boolean isEmptyOrNull(String str) {
        boolean flag = true;
        if (Objects.isNull(str)) {
            LOG.info("#isEmptyOrNull flag {}", flag);
            return flag;
        }

        if (str.length() == 0 || str.trim().length() == 0) {
            LOG.info("#isEmptyOrNull flag {}", flag);
            return flag;
        }

        LOG.info("#isEmptyOrNull flag {}", flag);
        return false;
    }

    public static boolean isNotEmpty(String str) {
        boolean flag =  !isEmptyOrNull(str);
        LOG.info("#isNotEmpty flag {}", flag);
        return flag;
    }
}
