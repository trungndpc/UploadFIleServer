package com.company.model;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;

public class EmojiModel {
    public static final EmojiModel INSTANCE = new EmojiModel();
    private static final String EMOJI_FOLDER;
    private static final String PREFIX;
    private static final String EXT;
    private static final IntPredicate NOT_VARIATION_SELECTOR = i -> i != 65039;

    static {
        EMOJI_FOLDER =  "resource/emojis";
        PREFIX = "emoji_u";
        EXT = ".png";
    }

    public BufferedImage getEmojiImage(int[] codePoints) {
        String hex_name = Arrays.stream(codePoints).mapToObj(Integer::toHexString).collect(Collectors.joining("_"));
        File file = new File(EMOJI_FOLDER + PREFIX + hex_name + EXT);
        if (!file.exists()) {
            return null;
        }
        try {
            return ImageIO.read(file);
        } catch (Exception ex) {
            return null;
        }
    }
}
