package com.company.util;

import org.apache.commons.lang3.RandomStringUtils;

public class FileUtil {
    public static String genFileName(String originalName) {
        String prefix = RandomStringUtils.randomAlphanumeric(4).toUpperCase();
        return prefix + "-" + originalName;
    }

}
