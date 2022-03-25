package com.company.layer;


import com.company.model.ConfigModel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ConfigParser {
    public JSONObject getConfig(JSONObject jsonData) throws Exception{
        int styleId = jsonData.getInt("styleId");
        String eventId = jsonData.getString("eventId");
        JSONObject config = ConfigModel.INSTANCE.getConfig(eventId, styleId);
        if (config == null) {
            config = jsonData;
        }
        return config;
    }

    public List<LayerConfig> parseDrawConfig(JSONObject jsonData) throws Exception {
        List<LayerConfig> layerConfigs = new ArrayList<>();
        JSONObject config = getConfig(jsonData);
        JSONArray configLayers = config.getJSONArray("layers");
        JSONArray layersData = jsonData.getJSONArray("layers");
        for (int i = 0; i < configLayers.length(); i++) {
            layerConfigs.add(parseLayerConfig(configLayers.getJSONObject(i), layersData));
        }
        return layerConfigs;
    }

    private LayerConfig parseLayerConfig(JSONObject layerConfig, JSONArray layersData) throws Exception {
        LayerType type = LayerType.valueOf(layerConfig.getString("type").toUpperCase());
        switch (type) {
            case IMAGE:
                return ImgLayerConfig.fromJson(layerConfig, layersData);
            case TEXT:
                return TextLayerConfig.fromJson(layerConfig, layersData);
            case BACKGROUND:
                return BackgroundLayerConfig.fromJson(layerConfig);
            default:
                throw new IllegalArgumentException();
        }
    }
}
