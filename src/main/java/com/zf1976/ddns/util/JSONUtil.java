package com.zf1976.ddns.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author ant
 * Create by Ant on 2021/3/24 2:51 PM
 */
@SuppressWarnings("all")
public class JSONUtil {

    private final static ObjectMapper JSON_MAPPER = new ObjectMapper();

    static {
        // 忽略在json字符串中存在，但是在java对象中不存在对应属性的情况
        JSON_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 忽略空Bean转json的错误
        JSON_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);
        // 允许不带引号的字段名称
        JSON_MAPPER.configure(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES.mappedFeature(), true);
        // 允许单引号
        JSON_MAPPER.configure(JsonReadFeature.ALLOW_SINGLE_QUOTES.mappedFeature(), true);
        // allow int startWith 0
        JSON_MAPPER.configure(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS.mappedFeature(), true);
        // 允许字符串存在转义字符：\r \n \t
        JSON_MAPPER.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
        // 排除空值字段
        JSON_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 使用驼峰式
        JSON_MAPPER.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
        // 使用bean名称
        JSON_MAPPER.enable(MapperFeature.USE_STD_BEAN_NAMING);
        // 忽略空字段
        JSON_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 所有日期格式都统一为固定格式
        JSON_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        // 设置时区
        JSON_MAPPER.setTimeZone(TimeZone.getTimeZone("GMT+8"));
    }


    /**
     * 对象转换为json字符串
     * @param o 要转换的对象
     */
    public static String toJsonString(Object o) {
        return toJsonString(o, false);
    }

    /**
     * 对象转换为json字符串
     * @param o 要转换的对象
     * @param format 是否格式化json
     */
    public static String toJsonString(Object o, boolean format) {
        try {
            if (o == null) {
                return "";
            }
            if (o instanceof Number) {
                return o.toString();
            }
            if (o instanceof String) {
                return (String)o;
            }
            if (format) {
                return JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(o);
            }
            return JSON_MAPPER.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 字符串转换为指定对象
     * @param json json字符串
     * @param cls 目标对象
     */
    public static <T> T toObject(String json, Class<T> cls) {
        if(StringUtil.isEmpty(json) || cls == null){
            return null;
        }
        try {
            return JSON_MAPPER.readValue(json, cls);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 字符串转换为指定对象，并增加泛型转义
     * 例如：List<Integer> test = toObject(jsonStr, List.class, Integer.class);
     * @param json json字符串
     * @param parametrized 目标对象
     * @param parameterClasses 泛型对象
     */
    public static <T> T toObject(String json, Class<?> parametrized, Class<?>... parameterClasses) {
        if(StringUtil.isEmpty(json) || parametrized == null){
            return null;
        }
        try {
            JavaType javaType = JSON_MAPPER.getTypeFactory().constructParametricType(parametrized, parameterClasses);
            return JSON_MAPPER.readValue(json, javaType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 字符串转换为指定对象
     * @param json json字符串
     * @param typeReference 目标对象类型
     */
    public static <T> T toObject(String json, TypeReference<T> typeReference) {
        if(StringUtil.isEmpty(json) || typeReference == null){
            return null;
        }
        try {
            return JSON_MAPPER.readValue(json, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 字符串转换为JsonNode对象
     * @param jsonObject json Object
     */
    public static JsonNode parse(Object jsonObject){
        return parse(jsonObject.toString());
    }

    /**
     * 字符串转换为JsonNode对象
     * @param json json字符串
     */
    public static JsonNode parse(String json) {
        if (StringUtil.isEmpty(json)) {
            return null;
        }
        try {
            return JSON_MAPPER.readTree(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 对象转换为map对象
     * @param o 要转换的对象
     */
    public static Map<?, ?> toMap(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof String) {
            return toObject((String)o, Map.class);
        }
        return JSON_MAPPER.convertValue(o, Map.class);
    }

    /**
     * json字符串转换为list对象
     * @param json json字符串
     */
    public static List<?> toList(String json) {
        if (StringUtil.isEmpty(json)) {
            return null;
        }
        try {
            return JSON_MAPPER.readValue(json, List.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * json字符串转换为list对象，并指定元素类型
     * @param json json字符串
     * @param cls list的元素类型
     */
    public static <T> List<T> toList(String json, Class<T> cls) throws IOException {
        if (StringUtil.isEmpty(json)) {
            return null;
        }
        try {
            JavaType javaType = JSON_MAPPER.getTypeFactory().constructParametricType(List.class, cls);
            return JSON_MAPPER.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static ObjectMapper getJsonMapper(){
        return JSON_MAPPER;
    }

    public static <T> T readValue(String json, Class<T> tClass) {
        try {
            return JSON_MAPPER.readValue(json, tClass);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
