package com.smartai.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.Getter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtil.class);

    @Getter
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 单位缩进字符串。
     */
    private static final String SPACE = "   ";

    static {
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    private JsonUtil() {

    }

    /**
     * 获取objectMapper实例
     */
    public static ObjectMapper getInstance() {
        return OBJECT_MAPPER;
    }

    /**
     * javaBean、列表数组转换为json字符串
     */
    public static String objToJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    /**
     * javaBean、列表数组转换为json字符串,忽略空值
     */
    public static String objToJsonIgnoreNull(Object obj) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsString(obj);
    }

    /**
     * json 转对象
     */
    public static <T> T jsonToObj(String jsonString, Class<T> clazz) {
        // 允许出现单引号
        OBJECT_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        // 接受只有一个元素的数组的反序列化
        OBJECT_MAPPER.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        try {
            return OBJECT_MAPPER.readValue(jsonString, clazz);
        } catch (IOException e) {
            LOGGER.error("jsonToObj error:{}", e.getMessage());
            throw new RuntimeException(e.toString());
        }
    }

    /**
     * json字符串转换为map
     */
    public static <T> Map<String, Object> jsonToMap(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            return mapper.readValue(jsonString, Map.class);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    /**
     * jsonToJsonNode
     */
    public static <T> JsonNode jsonToJsonNode(String jsonString) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try {
            return mapper.readValue(jsonString, JsonNode.class);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
            return null;
        }

    }

    /**
     * json字符串转换为map
     */
    public static <T> Map<String, T> jsonToMap(String jsonString, Class<T> clazz) {
        Map<String, Map<String, Object>> map = null;
        try {
            map = (Map<String, Map<String, Object>>) OBJECT_MAPPER.readValue(jsonString, new TypeReference<Map<String, T>>() {
            });
        } catch (JsonProcessingException | ClassCastException exception) {
            LOGGER.error(exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }
        Map<String, T> result = new HashMap<String, T>();
        for (Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
            result.put(entry.getKey(), mapToObj(entry.getValue(), clazz));
        }
        return result;
    }

    /**
     * 深度转换json成map
     *
     * @param json json
     * @return map
     */
    public static Map<String, Object> jsonToMapDeeply(String json) throws Exception {
        return jsonToMapRecursion(json, OBJECT_MAPPER);
    }

    /**
     * 把json解析成list，如果list内部的元素存在jsonString，继续解析
     *
     * @param json json
     * @param mapper 解析工具
     * @return List<Object>
     * @throws Exception 异常
     */
    private static List<Object> jsonToListRecursion(String json, ObjectMapper mapper) throws Exception {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        List<Object> list = mapper.readValue(StringEscapeUtils.escapeJson(json), List.class);

        for (Object obj : list) {
            if (obj != null && obj instanceof String) {
                String str = (String) obj;
                if (str.startsWith("[")) {
                    jsonToListRecursion(str, mapper);
                } else if (obj.toString().startsWith("{")) {
                    jsonToMapRecursion(str, mapper);
                }
            }
        }
        return list;
    }

    /**
     * 把json解析成map，如果map内部的value存在jsonString，继续解析
     *
     * @param json json
     * @param mapper ObjectMapper
     * @return Map<String, Object>
     * @throws Exception 异常
     */
    private static Map<String, Object> jsonToMapRecursion(String json, ObjectMapper mapper) throws Exception {
        if (StringUtils.isBlank(json)) {
            return null;
        }

        Map<String, Object> map = mapper.readValue(StringEscapeUtils.escapeJson(json), Map.class);

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object obj = entry.getValue();
            if (obj != null && obj instanceof String) {
                String str = ((String) obj);
                if (str.startsWith("[")) {
                    List<?> list = jsonToListRecursion(str, mapper);
                    map.put(entry.getKey(), list);
                } else if (str.startsWith("{")) {
                    Map<String, Object> mapRecursion = jsonToMapRecursion(str, mapper);
                    map.put(entry.getKey(), mapRecursion);
                }
            }
        }
        return map;
    }

    /**
     * 与javaBean json数组字符串转换为列表
     */
    public static <T> List<T> jsonToList(String jsonArrayStr, Class<T> clazz) {

        JavaType javaType = getCollectionType(ArrayList.class, clazz);
        List<T> list = null;
        try {
            list = (List<T>) OBJECT_MAPPER.readValue(jsonArrayStr, javaType);
        } catch (JsonProcessingException | ClassCastException e) {
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        return list;
    }


    /**
     * 获取泛型的Collection Type
     *
     * @param collectionClass 泛型的Collection
     * @param elementClasses  元素类
     * @return JavaType Java类型
     * @since 1.0
     */
    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return OBJECT_MAPPER.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }


    /**
     * map  转JavaBean
     */
    public static <T> T mapToObj(Map map, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.convertValue(map, clazz);
        } catch (IllegalArgumentException e) {
            LOGGER.error("mapToObj exceprion {}",e);
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * map 转json
     *
     * @param map map
     * @return String
     */
    public static String mapToJson(Map map) {
        try {
            return OBJECT_MAPPER.writeValueAsString(map);
        } catch (Exception e) {
            LOGGER.error("mapToJson error:{}", e);
            throw new RuntimeException("mapToJson exception");
        }
    }

    /**
     * map  转JavaBean
     */
    public static <T> T objToObj(Object obj, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.convertValue(obj, clazz);
        } catch (IllegalArgumentException e) {
            LOGGER.error("objToObj exception {}",e);
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * 返回格式化JSON字符串。
     *
     * @param json 未格式化的JSON字符串。
     * @return 格式化的JSON字符串。
     */
    public static String formatJson(String json) {
        if (StringUtils.isBlank(json)) {
            throw new RuntimeException("json is blank");
        }
        StringBuilder result = new StringBuilder();
        int length = json.length();
        int number = 0;
        char key = 0;
        // 遍历输入字符串。
        for (int i = 0; i < length; i++) {
            int j = i;
            // 1、获取当前字符。
            key = json.charAt(j);
            // 2、如果当前字符是前方括号、前花括号做如下处理：
            if ((key == '[') || (key == '{')) {
                // （1）如果前面还有字符，并且字符为“：”，打印：换行和缩进字符字符串。
                if ((j - 1 > 0) && (json.charAt(j - 1) == ':')) {
                    result.append('\n');
                    result.append(indent(number));
                }
                // （2）打印：当前字符。
                result.append(key);
                // （3）前方括号、前花括号，的后面必须换行。打印：换行。
                result.append('\n');
                // （4）每出现一次前方括号、前花括号；缩进次数增加一次。打印：新行缩进。
                number++;
                result.append(indent(number));
                // （5）进行下一次循环。
                continue;
            }
            // 3、如果当前字符是后方括号、后花括号做如下处理：
            if ((key == ']') || (key == '}')) {
                // （1）后方括号、后花括号，的前面必须换行。打印：换行。
                result.append('\n');
                // （2）每出现一次后方括号、后花括号；缩进次数减少一次。打印：缩进。
                number--;
                result.append(indent(number));
                // （3）打印：当前字符。
                result.append(key);
                // （4）如果当前字符后面还有字符，并且字符不为“，”，打印：换行。
                if (((j + 1) < length) && (json.charAt(j + 1) != ',')) {
                    result.append('\n');
                }
                // （5）继续下一次循环。
                continue;
            }
            result.append(key);
        }
        return result.toString();
    }

    /**
     * 返回指定次数的缩进字符串。每一次缩进三个空格，即SPACE。
     *
     * @param number 缩进次数。
     * @return 指定缩进次数的字符串。
     */
    private static String indent(int number) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < number; i++) {
            result.append(SPACE);
        }
        return result.toString();
    }
}
