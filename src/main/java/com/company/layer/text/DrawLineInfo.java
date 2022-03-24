package com.company.layer.text;

import java.util.Collection;

public class DrawLineInfo {
    private Collection<TextPhraseInfo> textPhrases;
    private Collection<EmojiInfo> emojis;
    private int lineWidth;

    public Collection<TextPhraseInfo> getTextPhrases() {
        return textPhrases;
    }

    public void setTextPhrases(Collection<TextPhraseInfo> textPhrases) {
        this.textPhrases = textPhrases;
    }

    public Collection<EmojiInfo> getEmojis() {
        return emojis;
    }

    public void setEmojis(Collection<EmojiInfo> emojis) {
        this.emojis = emojis;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }
}
