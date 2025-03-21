package io.jenkins.plugins.json_editor_parameter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

@UtilityClass
public class JsonUtil {

    String toJson(Map<String, Object> objectMap) {
        return JSONSerializer.toJSON(objectMap).toString();
    }

    Map<String, Object> toMap(String json) {
        if (json == null) {
            return Map.of();
        }
        json = json.trim();
        if (json.isEmpty()) {
            return Map.of();
        }

        JSON jsonObject = JSONSerializer.toJSON(json);
        if (jsonObject instanceof JSONObject) {
            return toJavaUtilMap((JSONObject) jsonObject);
        }
        throw new IllegalArgumentException("Not a json object");
    }

    Object toObject(String json) {
        if (json == null) {
            return null;
        }
        json = json.trim();
        if (json.isEmpty()) {
            return null;
        }

        return toJavaUtil(JSONSerializer.toJSON(json));
    }

    Object toJavaUtil(Object object) {
        if (isNull(object)) {
            return null;
        } else if (object instanceof JSONArray) {
            return toJavaUtilList((JSONArray) object);
        } else if (object instanceof JSONObject) {
            return toJavaUtilMap((JSONObject) object);
        }
        return object;
    }

    List<Object> toJavaUtilList(JSONArray array) {
        return array.stream().map(JsonUtil::toJavaUtil).collect(Collectors.toList());
    }

    Map<String, Object> toJavaUtilMap(JSONObject object) {
        Map<String, Object> result = new LinkedHashMap<>();

        Set<Map.Entry<String, Object>> entries = object.entrySet();
        entries.forEach(e -> result.put(e.getKey(), toJavaUtil(e.getValue())));

        return result;
    }

    boolean isNull(Object object) {
        if (object == null || object instanceof JSONNull) {
            return true;
        }

        if (object instanceof JSONObject jsonObject) {
	        return jsonObject.isNullObject();
        }
        return false;
    }
}
