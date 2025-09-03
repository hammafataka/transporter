package dev.mfataka.transporter.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.vavr.control.Try;

import lombok.extern.slf4j.Slf4j;

/**
 * @author HAMMA FATAKA
 */

@Slf4j
public class JsonUtils {

    protected static final ObjectMapper mapper = JsonMapper.builder()
            .findAndAddModules()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .build();

    /**
     * method to prettify  json schema String
     *
     * @param json a valid Json String
     * @return {@link Try<String>} returns formatted string without Json skeleton body( "{", ",", "}" ),
     * {@link  Try} will return fail in case exception
     */
    public static Try<String> prettifyJson(String json) {
        return Try.of(() -> {
            final var jsonData = mapper.readValue(json, Object.class);
            var data = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonData);
            data = data.replace("{", "");
            data = data.replace("}", "");
            data = data.replace(",", "");
            return data;
        }).onFailure(e -> log.warn("Parser: Exception while parsing json, message:[{}]", e.getMessage()));
    }

    /**
     * Method to convert any object to json
     *
     * @param object object to be converted
     * @return {@link Try}  in a Json format
     */
    public static Try<String> parseObjectToJson(Object object) {
        return Try.of(() -> mapper.writeValueAsString(object))
                .onFailure(e -> log.warn("Error occurred while converting object [{}] to json", object.toString()));
    }

    /**
     * Convert Json formatted String to java Object of type T
     *
     * @param json String to be converted on
     * @param type Object type
     * @return {@link Try} returns converted passed object type, in case of success, if not returns Try.failure()
     */
    public static <T> Try<T> parseJsonToObject(String json, Class<? extends T> type) {
        return Try.of(() -> mapper.readValue(json, (Class<T>) type))
                .onFailure(e -> log.error("Error parsing json, message[{}]", e.getMessage(), e));
    }
}
