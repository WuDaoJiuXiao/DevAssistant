package com.jiuxiao.assistant.generator;

import com.alibaba.fastjson2.JSON;
import com.jiuxiao.assistant.dto.FieldInfo;
import com.jiuxiao.assistant.dto.MethodInfo;
import com.jiuxiao.assistant.dto.MethodParamInfo;
import com.jiuxiao.assistant.dto.TypeInfo;
import com.jiuxiao.assistant.enums.BasicTypeEnum;
import com.jiuxiao.assistant.enums.FieldTypeEnum;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Json对象转换解析器
 */
public class JsonGenerator {

    private static final int MIN_MAP_NUM = 2;

    private static final double DOUBLE_DEFAULT_VALUE = 0.0;

    private static final String KEY_STRING = "key";

    private static final String DEFAULT_KEY_STRING = "defaultKey";

    private static final String BIG_DECIMAL_STRING = "java.math.BigDecimal";

    private static final String EMPTY_STRING = "";

    private static final String DATE_FORMATTER = "yyyy-MM-dd HH:mm:ss";

    private static final Set<String> dateSet = new HashSet<>(List.of(
            "java.util.Date",
            "java.sql.Date",
            "java.sql.Timestamp",
            "java.time.LocalDate",
            "java.time.LocalDateTime",
            "java.time.LocalTime",
            "java.time.ZonedDateTime",
            "java.time.Instant"
    ));

    private static final HashMap<String, BasicTypeEnum> boxedMap = new HashMap<>() {{
        put("java.lang.Byte", BasicTypeEnum.BYTE);
        put("java.lang.Short", BasicTypeEnum.SHORT);
        put("java.lang.Integer", BasicTypeEnum.INT);
        put("java.lang.Long", BasicTypeEnum.LONG);
        put("java.lang.Double", BasicTypeEnum.DOUBLE);
        put("java.lang.Float", BasicTypeEnum.FLOAT);
        put("java.lang.Boolean", BasicTypeEnum.BOOLEAN);
        put("java.lang.Character", BasicTypeEnum.CHAR);
    }};

    /**
     * 将方法信息对象转为JSON请求体
     *
     * @param methodInfo 方法信息对象
     * @return JSON字符对象
     */
    public static String generateRequestBody(MethodInfo methodInfo) {
        Map<String, Object> rootMap = new LinkedHashMap<>();
        for (MethodParamInfo parameter : methodInfo.getParameters()) {
            rootMap.put(parameter.getParamName(), buildValue(parameter.getParamType()));
        }
        return JSON.toJSONString(rootMap);
    }

    /**
     * 根据不同类型映射对应的转换器
     *
     * @param typeInfo 类型信息
     * @return 转换后的对象
     */
    private static Object buildValue(TypeInfo typeInfo) {
        String category = typeInfo.getCategory();
        FieldTypeEnum typeEnum = FieldTypeEnum.findByName(category);
        if (Objects.isNull(typeEnum)) {
            return null;
        }

        String fullName = typeInfo.getFullName();
        if (StringUtils.isEmpty(fullName)) {
            return null;
        }

        // 基本类型包装类、日期类、BigDecimal 等特殊常用类型，需要另处理
        boolean clzFace = Objects.equals(FieldTypeEnum.CLASS, typeEnum) || Objects.equals(FieldTypeEnum.INTERFACE, typeEnum);
        if (clzFace) {
            if (isBoxedWrapper(fullName)) {
                return getBasicDefault(boxedMap.getOrDefault(fullName, BasicTypeEnum.DEFAULT).getName());
            } else if (isDateTimeWrapper(fullName)) {
                return getDatetimeDefault();
            } else if (Objects.equals(fullName, BIG_DECIMAL_STRING)) {
                return DOUBLE_DEFAULT_VALUE;
            }
        }

        switch (typeEnum) {
            case BASIC:
                return getBasicDefault(typeInfo.getSimpleName());
            case STRING:
            case ENUM:
                return EMPTY_STRING;
            case CLASS:
            case INTERFACE:
                return buildObject(typeInfo);
            case LIST:
            case ARRAY:
                return buildList(typeInfo);
            case MAP:
                return buildMap(typeInfo);
            case FUNCTIONAL:
            case TYPE_PARAM:
            case WILDCARD:
                return new HashMap<>();
            default:
                return null;
        }
    }

    /**
     * 生成当前时间格式化结果
     *
     * @return 时间格式化对象
     */
    private static Object getDatetimeDefault() {
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMATTER);
        return localDateTime.format(formatter);
    }

    /**
     * 构建类属性对象
     *
     * @param typeInfo 类型信息
     * @return 转换后的对象
     */
    private static Object buildObject(TypeInfo typeInfo) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (FieldInfo field : typeInfo.getFields()) {
            map.put(field.getFiledName(), buildValue(field.getFieldType()));
        }
        return map;
    }

    /**
     * 构建集合对象
     *
     * @param typeInfo 类型信息
     * @return 转换后的对象
     */
    private static Object buildList(TypeInfo typeInfo) {
        List<Object> list = new ArrayList<>();
        List<TypeInfo> genericTypes = typeInfo.getGenericTypes();
        if (!genericTypes.isEmpty()) {
            list.add(buildValue(genericTypes.get(0)));
        }
        return list;
    }

    /**
     * 构建哈希对象
     *
     * @param typeInfo 类型信息
     * @return 转换后的对象
     */
    private static Object buildMap(TypeInfo typeInfo) {
        Map<String, Object> map = new LinkedHashMap<>();
        List<TypeInfo> genericTypes = typeInfo.getGenericTypes();
        if (genericTypes.size() > MIN_MAP_NUM) {
            String key = getDefaultKey(genericTypes.get(0));
            Object value = buildValue(genericTypes.get(1));
            map.put(key, value);
        }
        return map;
    }

    /**
     * 获取默认KEY
     *
     * @param typeInfo 类型信息
     * @return 默认的KEY值
     */
    private static String getDefaultKey(TypeInfo typeInfo) {
        String category = typeInfo.getCategory();
        boolean equalBasic = Objects.equals(category, FieldTypeEnum.BASIC.getName());
        boolean equalString = Objects.equals(category, FieldTypeEnum.STRING.getName());
        if (equalBasic || equalString) {
            return KEY_STRING;
        }
        return DEFAULT_KEY_STRING;
    }

    /**
     * 获取基本数据类型的默认值
     *
     * @param simpleName 类名
     * @return 默认值
     */
    private static Object getBasicDefault(String simpleName) {
        if (StringUtils.isEmpty(simpleName)) {
            return null;
        }

        BasicTypeEnum typeEnum = BasicTypeEnum.findByName(simpleName);
        if (Objects.isNull(typeEnum) || Objects.equals(typeEnum, BasicTypeEnum.DEFAULT)) {
            return null;
        }

        switch (typeEnum) {
            case BYTE:
            case SHORT:
            case INT:
                return 0;
            case LONG:
                return 0L;
            case FLOAT:
                return 0.0F;
            case DOUBLE:
                return 0.0;
            case BOOLEAN:
                return false;
            case CHAR:
                return '0';
            default:
                return null;
        }
    }


    /**
     * 是否为常用的日期类型
     *
     * @param fullName 全限定名
     * @return 是否为日期类型
     */
    private static boolean isDateTimeWrapper(String fullName) {
        if (StringUtils.isEmpty(fullName)) {
            return false;
        }
        return dateSet.contains(fullName);
    }

    /**
     * 是否是基本类型的包装类
     *
     * @param simpleName 全限定名
     * @return 是否为包装类
     */
    private static boolean isBoxedWrapper(String simpleName) {
        if (StringUtils.isEmpty(simpleName)) {
            return false;
        }
        return boxedMap.containsKey(simpleName);
    }
}
