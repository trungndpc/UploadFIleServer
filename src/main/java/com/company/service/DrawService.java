package com.company.service;

import com.company.layer.ConfigParser;
import com.company.layer.LayerConfig;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Comparator;
import java.util.List;

public class DrawService {
    public static final DrawService INSTANCE = new DrawService();
    public static final int DEFAULT_WIDTH = 540;
    public static final int DEFAULT_HEIGHT = 960;

    public BufferedImage drawImg(String jsonData) throws Exception {
        JSONObject json = new JSONObject(jsonData);
        List<LayerConfig> layerConfigs = ConfigParser.INSTANCE.parseDrawConfig(json);
        JSONObject jsonDataObj = ConfigParser.INSTANCE.getConfig(json);
        int width = DEFAULT_WIDTH;
        int height = DEFAULT_HEIGHT;
        int borderRadius = 0;
        if (jsonDataObj.has("config")) {
            JSONObject jsonConfig = jsonDataObj.getJSONObject("config");
            if (jsonConfig.has("width")) {
                width = jsonConfig.getInt("width");
            }
            if (jsonConfig.has("height")) {
                height = jsonConfig.getInt("height");
            }
            if (jsonConfig.has("borderRadius")) {
                borderRadius = jsonConfig.getInt("borderRadius");
            }
        }
        return draw(layerConfigs, width, height, borderRadius);
    }

    private BufferedImage draw(List<LayerConfig> layerConfigs, int width, int height, int borderRadius) throws Exception {
        BufferedImage finalImg = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = finalImg.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        layerConfigs.sort(Comparator.comparingInt(config -> config.getLayerId()));
        for (LayerConfig config : layerConfigs) {
            config.applyLayerConfig(graphics);
        }
        if (borderRadius != 0) {
            finalImg = makeRoundedCorner(finalImg, borderRadius);
        }
        return finalImg;
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

    public static void main(String[] args) throws Exception {
        String jsonData = "{\"eventId\":\"phenman2022\",\"styleId\":0,\"layers\":[{\"layerId\":2,\"type\":\"TEXT\",\"content\":\"Nguyễn Đình Trung\"},{\"layerId\":3,\"type\":\"IMAGE\",\"sourceUrl\":\"https://s240-ava-talk.zadn.vn/f/f/a/9/10/240/87069ccaa43702ad56ec93fe5a75f24f.jpg\"}]}";
        System.out.println(new JSONObject(jsonData));
        BufferedImage bufferedImage = INSTANCE.drawImg(jsonData);

        File file = new File("src/main/resources/data/images/test.png");
        ImageIO.write(bufferedImage, "png", file);
    }
}
