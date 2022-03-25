package com.company.layer;

import com.company.service.ImageService;
import org.json.JSONObject;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BackgroundLayerConfig extends LayerConfig{

    private String sourceUrl;
    private String color;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    @Override
    public void applyLayerConfig(Graphics2D graphics)  throws Exception{
        if (this.sourceUrl != null) {
            Image imageFromLocalCache = ImageService.INSTANCE.getImageFromLocalCache(this.sourceUrl);
            graphics.drawImage(imageFromLocalCache, this.left, this.top, this.width, this.height, null);
        }
        if (this.color != null) {
            Color grba = grba(this.color);
            if (grba == null) {
                throw new Exception("Color must be RGBA");
            }
            graphics.setColor(grba);
            graphics.fillRect(0, 0, width, height);
        }
    }

    private Color grba(String color) throws Exception {
        Pattern c = Pattern.compile("rgba *\\( *([0-9]+), *([0-9]+), *([0-9]+), *(\\d*\\.?\\d*) *\\)");
        Matcher m = c.matcher(color);
        if (m.matches()) {
            return new Color(Float.valueOf(m.group(1)) / 255, // r
                    Float.valueOf(m.group(2)) / 255, // g
                    Float.valueOf(m.group(3)) / 255, // b
                    Float.valueOf(m.group(4))); // a
        }
        return null;
    }

    public static BackgroundLayerConfig fromJson(JSONObject layerConfig) throws Exception {
        BackgroundLayerConfig config = new BackgroundLayerConfig();
        config.setLayerId(layerConfig.getInt("layerId"));
        config.setSourceUrl(layerConfig.optString("sourceUrl", null));
        config.setLeft(layerConfig.getInt("left"));
        config.setTop(layerConfig.getInt("top"));
        config.setWidth(layerConfig.getInt("width"));
        config.setHeight(layerConfig.getInt("height"));
        config.setColor(layerConfig.optString("color", null));
        config.setType(LayerType.BACKGROUND);
        return config;
    }

}
