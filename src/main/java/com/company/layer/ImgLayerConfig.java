package com.company.layer;

import com.company.service.ImageService;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class ImgLayerConfig extends LayerConfig {
    private String sourceUrl;
    private boolean isCrop = false;
    private boolean isCache = false;
    private Integer borderRadius;

    public int getBorderRadius() {
        return borderRadius;
    }

    public void setBorderRadius(Integer borderRadius) {
        this.borderRadius = borderRadius;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public boolean isIsCrop() {
        return isCrop;
    }

    public void setIsCrop(boolean isCrop) {
        this.isCrop = isCrop;
    }

    public boolean isIsCache() {
        return isCache;
    }

    public void setIsCache(boolean isCache) {
        this.isCache = isCache;
    }

    @Override
    public void applyLayerConfig(Graphics2D graphics) throws Exception {
        if (this.isVisibility()) {
            if (this.isDrawRect()) {
                graphics.drawRect(this.left, this.top, this.width, this.height);
            }
            BufferedImage bufferedImage = null;
            if (this.isCache) {
                bufferedImage = ImageService.INSTANCE.getImageFromLocalCache(this.sourceUrl);
            } else {
                bufferedImage = ImageService.INSTANCE.getImage(this.sourceUrl);
            }
            if (this.isCrop) {
                bufferedImage = crop(bufferedImage, width, height);
            }
            if (this.borderRadius != 0) {
                bufferedImage = makeRoundedCorner(bufferedImage, this.borderRadius);
            }

            graphics.drawImage(bufferedImage, this.left, this.top, this.width, this.height, null);
        }

    }

    public int[] getBestSize(int frameWidth, int frameHeight, int originWidth, int originHeight) {
        float rate = (float) frameWidth / frameHeight;
        int targetW = (int) (originHeight * rate);
        if (targetW <= originWidth) {
            return new int[]{targetW, originHeight};
        }
        int targetH = (int) (originWidth * (1 / rate));
        return new int[]{originWidth, targetH};
    }

    public BufferedImage crop(BufferedImage originalImage, int frameWidth, int frameHeight) {
        int originHeight = originalImage.getHeight();
        int originWidth = originalImage.getWidth();
        int[] best = getBestSize(frameWidth, frameHeight, originWidth, originHeight);
        int targetWidth = best[0];
        int targetHeight = best[1];
        // Coordinates of the image's middle
        int xc = (originWidth - targetWidth) / 2;
        int yc = (originHeight - targetHeight) / 2;

        // Crop
        BufferedImage croppedImage = originalImage.getSubimage(
                xc,
                yc,
                targetWidth, // widht
                targetHeight // height
        );
        return croppedImage;
    }

    public BufferedImage makeRoundedCorner(BufferedImage image, int cornerRadius) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage output = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);

        Graphics2D g2 = output.createGraphics();

        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));

        g2.setComposite(AlphaComposite.SrcAtop);
        g2.drawImage(image, 0, 0, null);

        g2.dispose();

        return output;
    }

    public static ImgLayerConfig fromJson(JSONObject layerConfig, JSONArray layersData) throws Exception {
        ImgLayerConfig config = new ImgLayerConfig();
        int layerId = layerConfig.getInt("layerId");
        JSONObject layerData = null;
        for (int i = 0; i < layersData.length(); i++) {
            if (layersData.getJSONObject(i).getInt("layerId") == layerId) {
                layerData = layersData.getJSONObject(i);
                break;
            }
        }
        if (layerData == null && layerConfig.optString("sourceUrl", null) == null) {
            throw new Exception("Missing data for layerId=" + layerId);
        }
        String sourceUrl = layerData != null ? layerData.optString("sourceUrl", null) : null;
        if (sourceUrl == null) {
            config.setIsCache(true);
            sourceUrl = layerConfig.getString("sourceUrl");
        }

        config.setLayerId(layerConfig.getInt("layerId"));
        config.setSourceUrl(sourceUrl);
        config.setDrawRect(layerData != null ? layerData.optBoolean("draw_rect", false) : false);
        config.setVisibility(layerData != null ? layerData.optBoolean("visibility", true) : true);
        config.setIsCrop(layerData != null ? layerConfig.optBoolean("is_crop", false) : false);
        config.setBorderRadius(layerConfig.optInt("borderRadius", 0));

        config.setLeft(layerConfig.getInt("left"));
        config.setTop(layerConfig.getInt("top"));
        config.setWidth(layerConfig.getInt("width"));
        config.setHeight(layerConfig.getInt("height"));
        config.setType(LayerType.IMAGE);
        return config;
    }
}
