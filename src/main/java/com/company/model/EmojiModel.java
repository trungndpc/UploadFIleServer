package com.company.model;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

public class EmojiModel {
    public static final EmojiModel INSTANCE = new EmojiModel();
    private static final String EMOJI_FOLDER = "../public/data/emojis";
    private static final String PREFIX = "emoji_u";
    private static final String EXT = ".png";

    public BufferedImage getEmojiImage(int[] codePoints) {
        String hex_name = Arrays.stream(codePoints).mapToObj(Integer::toHexString).collect(Collectors.joining("_"));
        File file = new File(EMOJI_FOLDER + PREFIX + hex_name + EXT);
        if (!file.exists()) {
            return null;
        }
        try {
            return ImageIO.read(file);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
