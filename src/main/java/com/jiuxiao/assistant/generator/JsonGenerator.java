package com.jiuxiao.assistant.generator;

import com.alibaba.fastjson2.JSON;
import com.jiuxiao.assistant.dto.extract.FieldInfo;
import com.jiuxiao.assistant.dto.extract.MethodInfo;
import com.jiuxiao.assistant.dto.extract.MethodParamInfo;
import com.jiuxiao.assistant.dto.extract.TypeInfo;
import com.jiuxiao.assistant.enums.BasicTypeEnum;
import com.jiuxiao.assistant.enums.FieldTypeEnum;
import com.jiuxiao.assistant.util.ClassUtil;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Json对象转换解析器
 */
public class JsonGenerator {

    private static final int MIN_MAP_NUM = 2;

    private static final String KEY_STRING = "key";

    private static final String DEFAULT_KEY_STRING = "defaultKey";

    private static final String BIG_DECIMAL_STRING = "java.math.BigDecimal";

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
     * @param isDefault  是否设置默认值(true:所有对象的值为类型的默认值 / false:所有对象的值为随机有效值)
     * @return JSON字符对象
     */
    public static String generateRequestBody(MethodInfo methodInfo, boolean isDefault) {
        Map<String, Object> rootMap = new LinkedHashMap<>();
        Set<String> nestedTypes = new HashSet<>();
        for (MethodParamInfo parameter : methodInfo.getParameters()) {
            rootMap.put(parameter.getParamName(), buildValue(parameter.getParamType(), isDefault, nestedTypes));
        }
        return JSON.toJSONString(rootMap);
    }

    /**
     * 根据不同类型映射对应的转换器
     *
     * @param typeInfo    类型信息
     * @param isDefault   是否设置默认值(true:所有对象的值为类型的默认值 / false:所有对象的值为随机有效值)
     * @param nestedTypes 嵌套类型路径集合
     * @return 转换后的对象
     */
    private static Object buildValue(TypeInfo typeInfo, boolean isDefault, Set<String> nestedTypes) {
        String category = typeInfo.getCategory();
        FieldTypeEnum typeEnum = FieldTypeEnum.findByName(category);
        if (Objects.isNull(typeEnum)) {
            return null;
        }

        String fullName = typeInfo.getFullName();
        if (StringUtils.isEmpty(fullName)) {
            return null;
        }

        // 监测循环嵌套
        boolean clzFace = Objects.equals(FieldTypeEnum.CLASS, typeEnum) || Objects.equals(FieldTypeEnum.INTERFACE, typeEnum);
        if (clzFace) {
            if (nestedTypes.contains(fullName)) {
                return null;
            }
            nestedTypes.add(fullName);
        }

        // 基本类型包装类、日期类、BigDecimal 等特殊常用类型，需要另处理
        if (clzFace) {
            if (isBoxedWrapper(fullName)) {
                return getBasicDefault(boxedMap.getOrDefault(fullName, BasicTypeEnum.DEFAULT).getName(), isDefault);
            } else if (isDateTimeWrapper(fullName)) {
                return getDatetimeDefault();
            } else if (Objects.equals(fullName, BIG_DECIMAL_STRING)) {
                return ClassUtil.toValue(BigDecimal.class, isDefault);
            }
        }

        switch (typeEnum) {
            case BASIC:
                return getBasicDefault(typeInfo.getSimpleName(), isDefault);
            case STRING:
            case ENUM:
                return ClassUtil.toValue(String.class, isDefault);
            case CLASS:
            case INTERFACE:
                return buildObject(typeInfo, isDefault, new HashSet<>(nestedTypes));
            case LIST:
            case ARRAY:
                return buildList(typeInfo, isDefault, new HashSet<>(nestedTypes));
            case MAP:
                return buildMap(typeInfo, isDefault, new HashSet<>(nestedTypes));
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
     * @param typeInfo    类型信息
     * @param isDefault   是否设置默认值(true:所有对象的值为类型的默认值 / false:所有对象的值为随机有效值)
     * @param nestedTypes 嵌套参数路径集合
     * @return 转换后的对象
     */
    private static Object buildObject(TypeInfo typeInfo, boolean isDefault, Set<String> nestedTypes) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (FieldInfo field : typeInfo.getFields()) {
            map.put(field.getFiledName(), buildValue(field.getFieldType(), isDefault, new HashSet<>(nestedTypes)));
        }
        return map;
    }

    /**
     * 构建集合对象
     *
     * @param typeInfo    类型信息
     * @param isDefault   是否设置默认值(true:所有对象的值为类型的默认值 / false:所有对象的值为随机有效值)
     * @param nestedTypes 嵌套参数路径集合
     * @return 转换后的对象
     */
    private static Object buildList(TypeInfo typeInfo, boolean isDefault, Set<String> nestedTypes) {
        List<Object> list = new ArrayList<>();
        List<TypeInfo> genericTypes = typeInfo.getGenericTypes();
        if (!genericTypes.isEmpty()) {
            list.add(buildValue(genericTypes.get(0), isDefault, new HashSet<>(nestedTypes)));
        }
        return list;
    }

    /**
     * 构建哈希对象
     *
     * @param typeInfo    类型信息
     * @param isDefault   是否设置默认值(true:所有对象的值为类型的默认值 / false:所有对象的值为随机有效值)
     * @param nestedTypes 嵌套参数路径集合
     * @return 转换后的对象
     */
    private static Object buildMap(TypeInfo typeInfo, boolean isDefault, Set<String> nestedTypes) {
        Map<String, Object> map = new LinkedHashMap<>();
        List<TypeInfo> genericTypes = typeInfo.getGenericTypes();
        if (genericTypes.size() > MIN_MAP_NUM) {
            String key = getDefaultKey(genericTypes.get(0));
            Object value = buildValue(genericTypes.get(1), isDefault, new HashSet<>(nestedTypes));
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
     * @param isDefault  是否设置默认值(true:所有对象的值为类型的默认值 / false:所有对象的值为随机有效值)
     * @return 默认值
     */
    private static Object getBasicDefault(String simpleName, boolean isDefault) {
        if (StringUtils.isEmpty(simpleName)) {
            return null;
        }

        BasicTypeEnum typeEnum = BasicTypeEnum.findByName(simpleName);
        if (Objects.isNull(typeEnum) || Objects.equals(typeEnum, BasicTypeEnum.DEFAULT)) {
            return null;
        }

        switch (typeEnum) {
            case BYTE:
                return ClassUtil.toValue(Byte.class, isDefault);
            case SHORT:
                return ClassUtil.toValue(Short.class, isDefault);
            case INT:
                return ClassUtil.toValue(Integer.class, isDefault);
            case LONG:
                return ClassUtil.toValue(Long.class, isDefault);
            case FLOAT:
                return ClassUtil.toValue(Float.class, isDefault);
            case DOUBLE:
                return ClassUtil.toValue(Double.class, isDefault);
            case BOOLEAN:
                return ClassUtil.toValue(Boolean.class, isDefault);
            case CHAR:
                return ClassUtil.toValue(Character.class, isDefault);
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
