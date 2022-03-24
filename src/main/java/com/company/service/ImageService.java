package com.company.service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class ImageService {
    public static final ImageService INSTANCE = new ImageService();
    private static final ConcurrentHashMap<String, BufferedImage> BG_IMG_CACHE = new ConcurrentHashMap<>();

    static {
        ImageIO.setUseCache(false);
    }

    public BufferedImage getImage(String sourceUrl) throws Exception {
        return ImageIO.read(new URL(sourceUrl));
    }

    /*
    Only for background image
     */
    public BufferedImage getImageFromLocalCache(String sourceUrl) throws Exception {
        BufferedImage img = BG_IMG_CACHE.get(sourceUrl);
        if (img == null) {
            img = getImage(sourceUrl);
            BG_IMG_CACHE.putIfAbsent(sourceUrl, img);
        }
        return img;
    }
}
