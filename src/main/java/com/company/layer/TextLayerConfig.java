package com.company.layer;

import com.company.common.TextAlign;
import com.company.layer.text.DrawLineInfo;
import com.company.layer.text.EmojiInfo;
import com.company.layer.text.TextPhraseInfo;
import com.company.model.EmojiModel;
import com.company.model.FontModel;
import com.vdurmont.emoji.EmojiParser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.text.AttributedString;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextLayerConfig extends LayerConfig{
    private static final Pattern LINE_END = Pattern.compile("\\n");
    private static final String THREE_DOT = "...";
    private static final int WHITE_SPACE = " ".codePointAt(0);
    private static final String EMOJI_FONT = "NotoColorEmoji";
    private String content;
    private Color color;
    private Font font;
    private TextAlign align;
    private int fontSize;
    private int lineHeight;
    private TextAlign alignVertical;


    public TextAlign getAlignVertical() {
        return alignVertical;
    }

    public void setAlignVertical(TextAlign alignVertical) {
        this.alignVertical = alignVertical;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public TextAlign getAlign() {
        return align;
    }

    public void setAlign(TextAlign align) {
        this.align = align;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    @Override
    public void applyLayerConfig(Graphics2D graphics) {
        if (this.isVisibility()) {
            graphics.setColor(this.color);
            if (this.isDrawRect()) {
                graphics.drawRect(this.left, this.top, this.width, this.height);
            }
            String[] textLines = LINE_END.split(this.content.replaceAll("\ufe0f", ""));
            List<DrawLineInfo> drawLineInfoList = buildDrawLines(textLines);
            drawLines(graphics, drawLineInfoList);
        }

    }

    private List<DrawLineInfo> buildDrawLines(String[] textLines) {
        try {
            int maxLine = this.height / this.lineHeight;
            String[] fontPriorities = FontModel.INSTANCE.getFallBackFontPriority();
            Map<String, Font> fbFonts = FontModel.INSTANCE.getFallbackFontsWithSize(this.fontSize);
            Map<String, FontMetrics> metricsMap = getFontMetrics(fbFonts);

            List<DrawLineInfo> drawLineInfoList = new ArrayList<>();
            for (int idx = 0; idx < textLines.length; idx++) {
                String textLine = textLines[idx];
                List<DrawLineInfo> infoList = new ArrayList<>();
                if (!textLine.trim().isEmpty()) {
                    Map<Integer, EmojiInfo> emojiMap = new HashMap<>();
                    int[] codePoints = textLine.codePoints().toArray();
                    //xac dinh font cho tung code point
                    String[] fontAlc = allocateFont(codePoints, fbFonts, fontPriorities);

                    //xac dinh dòng này có dùng emojis không, nếu có thì đổi nó thành list code point
                    List<String> emojis = EmojiParser.extractEmojis(textLine);
                    List<int[]> emojisCodePoints = emojis.stream().map(s -> s.codePoints().toArray()).collect(Collectors.toList());

                    int emojiIdx = 0; //ví trí của emoji
                    int curWidth = 0;
                    int markWidth = 0;
                    int start = 0;
                    int end = 0;
                    int step = 1;
                    for (int i = 0; i < codePoints.length;) {
                        String allocatedFont = fontAlc[i];
                        int codePoint = codePoints[i];
                        if (codePoint == WHITE_SPACE || allocatedFont.equals(EMOJI_FONT)) {
                            markWidth = curWidth;
                            end = i;
                        }
                        // lấy font đó để đo độ dài của từng ký tự

                        int charWidth = metricsMap.get(allocatedFont).charWidth(codePoint);

                        //neu la emoji tinh độ dài
                        if (allocatedFont.equals(EMOJI_FONT)) {
                            if (emojiIdx < emojisCodePoints.size() && emojisCodePoints.get(emojiIdx)[0] == codePoint) {
                                charWidth = this.fontSize + this.fontSize / 9 * 2;
                                emojiMap.put(i - start, new EmojiInfo(emojisCodePoints.get(emojiIdx), curWidth + this.fontSize / 9));
                                step = emojisCodePoints.get(emojiIdx).length;
                                emojiIdx++;
                            } else {
                                charWidth = metricsMap.get("System").charWidth(codePoint);
                                fontAlc[i] = "System";
                                step = 1;
                            }
                        }
                        if (curWidth + charWidth <= this.width) {
                            curWidth += charWidth;
                        } else {
                            if (end == start) {
                                markWidth = curWidth;
                                end = i;
                            }
                            for (int j = end; j <= i; j++) {
                                if (emojiMap.remove(j - start) != null) {
                                    emojiIdx--;
                                }
                            }

                            boolean endWith3Dots = false;
                            if (drawLineInfoList.size() + infoList.size() + 1 == maxLine && end != codePoints.length) {
                                endWith3Dots = true;
                            }

                            DrawLineInfo drawLineInfo = buildDrawLine(Arrays.copyOfRange(codePoints, start, end),
                                    Arrays.copyOfRange(fontAlc, start, end), fbFonts, markWidth, endWith3Dots, emojiMap, metricsMap);
                            infoList.add(drawLineInfo);

                            if (codePoints[end] == WHITE_SPACE) {
                                ++end;
                            }

                            i = end - step;
                            start = end;
                            markWidth = 0;
                            curWidth = 0;
                            emojiMap.clear();
                        }
                        i += step;
                    }
                    boolean endWith3Dots = false;
                    if (drawLineInfoList.size() + infoList.size() + 1 == maxLine && idx + 1 < textLines.length) {
                        endWith3Dots = true;
                    }

                    DrawLineInfo drawLineInfo = buildDrawLine(Arrays.copyOfRange(codePoints, start, codePoints.length),
                            Arrays.copyOfRange(fontAlc, start, codePoints.length), fbFonts, curWidth, endWith3Dots, emojiMap, metricsMap);
                    infoList.add(drawLineInfo);
                } else {
                    DrawLineInfo drawLineInfo = new DrawLineInfo();
                    if (drawLineInfoList.size() + 1 == maxLine && idx < textLines.length - 1) {
                        AttributedString attributedString = new AttributedString(THREE_DOT);
                        attributedString.addAttribute(TextAttribute.FONT, this.font, 0, 3);
                        TextPhraseInfo textPhraseInfo = new TextPhraseInfo(attributedString, 0);

                        drawLineInfo.setTextPhrases(Collections.singletonList(textPhraseInfo));
                        drawLineInfo.setEmojis(new ArrayList<>());
                        drawLineInfo.setLineWidth(metricsMap.get(this.font.getFontName()).stringWidth(THREE_DOT));
                    } else {
                        drawLineInfo.setLineWidth(0);
                        drawLineInfo.setTextPhrases(new ArrayList<>());
                        drawLineInfo.setEmojis(new ArrayList<>());
                    }
                    infoList.add(drawLineInfo);
                }
                drawLineInfoList.addAll(infoList);
            }
            return drawLineInfoList;
        } finally {
        }
    }

    private DrawLineInfo buildDrawLine(int[] codePoints, String[] fontAlc, Map<String, Font> fbFonts, int markWidth,
                                       boolean endWith3Dots, Map<Integer, EmojiInfo> emojiMap, Map<String, FontMetrics> metricsMap) {
        DrawLineInfo drawLineInfo = new DrawLineInfo();
        if (!emojiMap.isEmpty()) {
            List<TextPhraseInfo> textPhrases = new ArrayList<>();
            List<Integer> emojiPos = new ArrayList<>(emojiMap.keySet());
            emojiPos.sort(Comparator.naturalOrder());
            int start = 0;
            int relativePos = 0;
            for (int end : emojiPos) {
                if (end > start) {
                    textPhrases.add(buildTextPhrase(Arrays.copyOfRange(codePoints, start, end), Arrays.copyOfRange(fontAlc, start, end), fbFonts, relativePos, false));
                    for (int j = start; j < end; j++) {
                        relativePos += metricsMap.get(fontAlc[j]).charWidth(codePoints[j]);
                    }
                }
                start = end + emojiMap.get(end).getCodePoints().length;
                relativePos += this.fontSize + this.fontSize / 9 * 2;
            }
            if (start < codePoints.length) {
                textPhrases.add(buildTextPhrase(Arrays.copyOfRange(codePoints, start, codePoints.length), Arrays.copyOfRange(fontAlc, start, codePoints.length), fbFonts, relativePos, endWith3Dots));
            }
            drawLineInfo.setTextPhrases(textPhrases);
        } else {
            drawLineInfo.setTextPhrases(Collections.singletonList(buildTextPhrase(codePoints, fontAlc, fbFonts, 0, endWith3Dots)));
        }

        drawLineInfo.setEmojis(new ArrayList<>(emojiMap.values()));
        drawLineInfo.setLineWidth(markWidth);
        return drawLineInfo;

    }

    // TextPhraseInfo : vẽ từng chữ
    private TextPhraseInfo buildTextPhrase(int[] codePoints, String[] fontAlc, Map<String, Font> fbFonts, int relativePos,
                                           boolean endWith3Dots) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < fontAlc.length; i++) {
            builder.appendCodePoint(codePoints[i]);
        }
        String phrase;
        int length;
        // If need to be ended with .. then replace last 2 chars of line by ...
        if (endWith3Dots) {
            length = fontAlc.length - 2;
            phrase = builder.substring(0, Math.max(0, length)) + THREE_DOT;
        } else {
            phrase = builder.toString();
            length = fontAlc.length;
        }
        AttributedString attributedString = new AttributedString(phrase);
        attributedString.addAttribute(TextAttribute.FONT, this.font, 0, phrase.length());
        int curLen = 0;
        for (int i = 0; i < length; i++) {
            int charCount = Character.charCount(codePoints[i]);
            if (!fontAlc[i].equals(this.font.getFontName())) {
                attributedString.addAttribute(TextAttribute.FONT, fbFonts.get(fontAlc[i]), curLen, curLen + charCount);
            }
            curLen += charCount;

        }
        return new TextPhraseInfo(attributedString, relativePos);
    }

    /*
    Find down which code point supposed to be displayed by which font
     */
    private String[] allocateFont(int[] codePoints, Map<String, Font> fbFonts, String[] fontPriorities) {
        try {
            String[] fontAlc = new String[codePoints.length];
            Arrays.fill(fontAlc, this.font.getFontName());
            for (int i = 0; i < codePoints.length; i++) {
                int codePoint = codePoints[i];

                // Emoji font has highest priority
                if (codePoint > 255 && fbFonts.get(EMOJI_FONT).canDisplay(codePoint)) {
                    fontAlc[i] = EMOJI_FONT;
                    continue;
                }
                if (!this.font.canDisplay(codePoint)) {
                    for (String fontName : fontPriorities) {
                        if (fbFonts.get(fontName).canDisplay(codePoint)) {
                            fontAlc[i] = fontName;
                            break;
                        }
                    }
                }
            }
            return fontAlc;
        } finally {
        }
    }

    private void drawLines(Graphics graphics, List<DrawLineInfo> drawLineInfoList) {
        try {
            int numLine = drawLineInfoList.size();
            for (int i = 1; i <= numLine && i * this.lineHeight <= this.height; i++) {
                DrawLineInfo drawLineInfo = drawLineInfoList.get(i - 1);
                Collection<TextPhraseInfo> textPhrases = drawLineInfo.getTextPhrases();
                Collection<EmojiInfo> emojis = drawLineInfo.getEmojis();
                int xStart = coordinatesAlign(drawLineInfo, i);
                int yStart = coordinatesAlignVertical(drawLineInfo, i, numLine);
                for (TextPhraseInfo textPhrase : textPhrases) {
                    graphics.drawString(textPhrase.getTextPhrase().getIterator(), xStart + textPhrase.getRelativePos(), yStart);
                }
                drawEmoji(emojis, graphics, xStart, yStart);
            }
        } finally {
        }
    }

    private int coordinatesAlignVertical(DrawLineInfo drawLineInfo, int pos, int numLine){
        int yStart;
        switch (this.alignVertical) {
            case TOP:
                yStart = this.top  + pos * this.lineHeight;
                break;
            case CENTER:
                if(numLine * this.lineHeight > this.height){
                    yStart = this.top + pos * this.lineHeight - this.lineHeight / 2;
                } else {
                    yStart = this.top + pos * this.lineHeight + (this.height - numLine * this.lineHeight) / 2 - this.lineHeight / 2;
                }
                break;
            case BOTTOM:
                if(numLine * this.lineHeight > this.height){
                    yStart = this.top + pos * this.lineHeight - this.lineHeight / 2;
                } else {
                    yStart = this.top + pos * this.lineHeight + this.height - numLine * this.lineHeight;
                }
                break;
            default:
                throw new IllegalArgumentException("Align type not support");
        }
        return yStart;
    }

    private int coordinatesAlign(DrawLineInfo drawLineInfo, int pos){
        int xStart;
        switch (this.align) {
            case LEFT:
                xStart = this.left;
                break;
            case CENTER:
                xStart = this.left + (this.width - drawLineInfo.getLineWidth()) / 2;
                break;
            case RIGHT:
                xStart = this.left + this.width - drawLineInfo.getLineWidth();
                break;
            default:
                throw new IllegalArgumentException("Align type not support");
        }
        return xStart;
    }

    private Map<String, FontMetrics> getFontMetrics(Map<String, Font> fontMap) {
        Map<String, FontMetrics> fontMetricsMap = new HashMap<>(fontMap.size());
        Canvas canvas = new Canvas();
        for (Map.Entry<String, Font> entry : fontMap.entrySet()) {
            fontMetricsMap.put(entry.getKey(), canvas.getFontMetrics(entry.getValue().deriveFont(this.fontSize * 1.0f)));
        }
        fontMetricsMap.put(this.font.getFontName(), canvas.getFontMetrics(this.font));
        return fontMetricsMap;
    }

    private void drawEmoji(Collection<EmojiInfo> emojiInfoList, Graphics graphics, int lineLeft, int lineBase) {
        try {
            for (EmojiInfo emojiInfo : emojiInfoList) {
                BufferedImage image = EmojiModel.INSTANCE.getEmojiImage(emojiInfo.getCodePoints());
                int ascent = new Canvas().getFontMetrics(this.font).getAscent();
                int descent = new Canvas().getFontMetrics(this.font).getDescent();
                int center = (ascent + descent) / 2;
                graphics.drawImage(image, lineLeft + emojiInfo.getRelativePos(), lineBase + descent - center - this.fontSize / 2, this.fontSize, this.fontSize, null);
            }
        } finally {
        }
    }

    public static TextLayerConfig fromJson(JSONObject layerConfig, JSONArray layersData) throws Exception {
        int layerId = layerConfig.getInt("layerId");
        JSONObject layerData = null;
        for (int i = 0; i < layersData.length(); i++) {
            if (layersData.getJSONObject(i).getInt("layerId") == layerId) {
                layerData = layersData.getJSONObject(i);
                break;
            }
        }
        if (layerData == null && layerConfig.optString("content", null) == null) {
            throw new Exception("Missing data for layerId=" + layerId);
        }

        String content = layerData != null ? layerData.optString("content", null) : null;
        if (content == null) {
            content = layerConfig.getString("content");
        }

        TextLayerConfig config = new TextLayerConfig();
        config.setLayerId(layerConfig.getInt("layerId"));
        config.setContent(content);
        config.setDrawRect(layerData != null ? layerData.optBoolean("draw_rect", false) : false);
        config.setVisibility(layerData != null ? layerData.optBoolean("visibility", true) : true);

        config.setLeft(layerConfig.getInt("left"));
        config.setTop(layerConfig.getInt("top"));
        config.setWidth(layerConfig.getInt("width"));
        config.setHeight(layerConfig.getInt("height"));
        config.setColor(Color.decode(layerConfig.getString("color")));
        config.setAlignVertical(TextAlign.valueOf(layerConfig.optString("alignVertical", "top").toUpperCase()));
        config.setAlign(TextAlign.valueOf(layerConfig.getString("align").toUpperCase()));
        config.setLineHeight(layerConfig.getInt("lineHeight"));
        config.setFontSize(layerConfig.getInt("fontSize"));
        if(layerData != null){
            if(layerData.optInt("lineHeight", 0) != 0){
                config.setLineHeight(layerData.optInt("lineHeight"));
            }
            if(layerData.optInt("fontSize", 0) != 0){
                config.setFontSize(layerData.optInt("fontSize"));
            }
        }
        Font font = FontModel.INSTANCE.getFont(layerConfig.getString("font"));
        font = font.deriveFont((float) config.getFontSize());
        config.setFont(font);
        config.setType(LayerType.TEXT);
        return config;

    }
}
