package com.jiuxiao.assistant.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 类对象工具类
 */
public class ClassUtil {

    private static final String CHAR_POOL = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * 生成某个类型的随机数据
     *
     * @param clazz     类对象
     * @param isDefault 是否随机(true:所有对象的值为类型的默认值 / false:所有对象的值为随机有效值)
     * @return 生成的随机值
     */
    public static Object toValue(Class<?> clazz, boolean isDefault) {
        if (clazz == null) {
            return null;
        }

        if (byte.class == clazz || Byte.class == clazz) {
            return isDefault ? (byte) 0 : (byte) randomInt(0, Byte.MAX_VALUE);
        }

        if (short.class == clazz || Short.class == clazz) {
            return isDefault ? (short) 0 : (short) randomInt(0, Short.MAX_VALUE);
        }

        if (int.class == clazz || Integer.class == clazz) {
            return isDefault ? 0 : randomInt(0, Integer.MAX_VALUE);
        }

        if (long.class == clazz || Long.class == clazz) {
            return isDefault ? 0L : randomLong();
        }

        if (float.class == clazz || Float.class == clazz) {
            return isDefault ? 0.0F : randomDouble();
        }

        if (double.class == clazz || Double.class == clazz) {
            return isDefault ? 0.0D : randomDouble();
        }

        if (boolean.class == clazz || Boolean.class == clazz) {
            return isDefault ? Boolean.FALSE : randomBoolean();
        }

        if (char.class == clazz || Character.class == clazz) {
            return isDefault ? '0' : randomChar();
        }

        if (String.class == clazz) {
            return isDefault ? "" : randomString(5, 15);
        }

        if (BigDecimal.class == clazz) {
            return isDefault ? BigDecimal.ZERO : randomBigDecimal();
        }

        if (List.class == clazz) {
            List<Object> list = new ArrayList<>();
            if (!isDefault) {
                list.add(randomString(3, 8));
            }
            return list;
        }

        if (Set.class == clazz) {
            Set<Object> set = new HashSet<>();
            if (!isDefault) {
                set.add(randomInt(1, 100));
            }
            return set;
        }

        if (Map.class == clazz) {
            Map<String, Object> map = new HashMap<>();
            if (!isDefault) {
                map.put(randomString(2, 5), randomInt(1, 100));
            }
            return map;
        }

        return null;
    }

    private static int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max);
    }

    private static long randomLong() {
        return ThreadLocalRandom.current().nextLong(0, Long.MAX_VALUE);
    }

    private static double randomDouble() {
        double num = ThreadLocalRandom.current().nextDouble(0.01, 100_000);
        return Math.round(num * 100_000) / 100_000.0;
    }

    private static boolean randomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    private static char randomChar() {
        return CHAR_POOL.charAt(randomInt(0, CHAR_POOL.length() - 1));
    }

    private static String randomString(int minLen, int maxLen) {
        int len = randomInt(minLen, maxLen);
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(randomChar());
        }
        return sb.toString();
    }

    private static BigDecimal randomBigDecimal() {
        return BigDecimal.valueOf(randomLong()).setScale(6, RoundingMode.HALF_UP);
    }
}
