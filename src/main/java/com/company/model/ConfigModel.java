package com.company.model;

import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ConfigModel {
    public static final ConfigModel INSTANCE = new ConfigModel();
    private static final String KEY_FORMAT = "%s-%d";
    private static final Map<String, JSONObject> CONFIG_MAP = new HashMap<>();
    public static final String CONFIG_FOLDER = "data/config";

    static {
        try{
            Resource resource = new ClassPathResource(CONFIG_FOLDER);
            File configFolder = resource.getFile();
            for (File file : Objects.requireNonNull(configFolder.listFiles())) {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String config = br.lines().collect(Collectors.joining());
                    JSONObject jsonConfig = new JSONObject(config);
                    String key = String.format(KEY_FORMAT, jsonConfig.getString("eventId"), jsonConfig.getInt("styleId"));
                    CONFIG_MAP.put(key, jsonConfig);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public JSONObject getConfig(String eventId, int styleId) throws Exception {
        String key = String.format(KEY_FORMAT, eventId, styleId);
        JSONObject config = CONFIG_MAP.get(key);
        if (config == null) {
            throw new Exception(String.format("Can not find style with eventId = %s and id=%d", eventId, styleId));
        }
        return config;
    }

}
