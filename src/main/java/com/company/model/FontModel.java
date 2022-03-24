package com.company.model;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class FontModel {
    private static final String FONT_FOLDER;
    private static final String FALL_BACK_FONT_FOLDER;
    private static final String[] FALL_BACK_FONT_PRIORITY;
    public static final Pattern COMMA = Pattern.compile(",");

    private static final Map<String, Font> FONT_MAP = new HashMap<>();
    private static final Map<String, Font> FALLBACK_FONT_MAP = new HashMap<>();
    private static final Map<Integer, Map<String,Font>> FALL_BACK_FONT_MAP_WITH_SIZE = new HashMap<>();
    public static FontModel INSTANCE = new FontModel();



    static  {

        FONT_FOLDER = "resource/fonts";
        FALL_BACK_FONT_FOLDER = "resource/fallback-fonts";
        FALL_BACK_FONT_PRIORITY = COMMA.split( "System");

        File fontFolder = new File(FONT_FOLDER);
        for (File file : Objects.requireNonNull(fontFolder.listFiles())) {
            try {
                Font font = Font.createFont(Font.TRUETYPE_FONT, file);
                String fileName = FilenameUtils.getBaseName(file.getName());
                FONT_MAP.put(fileName, font);
            } catch (Exception e) {
                LOGGER.error(e);
            }
        }

        File fallbackFontFolder = new File(FALL_BACK_FONT_FOLDER);
        for (File file : Objects.requireNonNull(fallbackFontFolder.listFiles())) {
            try {
                Font font = Font.createFont(Font.TRUETYPE_FONT, file);
                String fileName = FilenameUtils.getBaseName(file.getName());
                FALLBACK_FONT_MAP.put(fileName, font);
            } catch (Exception ex) {
                LOGGER.error(ex);
            }
        }
        FALLBACK_FONT_MAP.put("System", Font.decode(""));
    }

    public Font getFont(String fontName) throws Exception{
        Font font = FONT_MAP.get(fontName);
        if (font == null) {
            throw new Exception("Font not found");
        }
        return font;
    }

    public Map<String, Font> getFallbackFonts() {
        return FALLBACK_FONT_MAP;
    }

    public String[] getFallBackFontPriority() {
        return FALL_BACK_FONT_PRIORITY;
    }

    public Map<String, Font> getFallbackFontsWithSize(int fontSize) {
        Map<String, Font> fontMap = FALL_BACK_FONT_MAP_WITH_SIZE.get(fontSize);
        if (fontMap == null) {
            fontMap = new HashMap<>();
            for (Map.Entry<String, Font> entry : FALLBACK_FONT_MAP.entrySet()) {
                fontMap.put(entry.getKey(), entry.getValue().deriveFont(fontSize * 1.0f));
            }
            FALL_BACK_FONT_MAP_WITH_SIZE.put(fontSize, fontMap);
        }
        return fontMap;
    }
}
