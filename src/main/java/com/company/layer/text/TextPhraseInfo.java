package com.company.layer.text;

import java.text.AttributedString;

public class TextPhraseInfo {
    private AttributedString textPhrase;
    private int relativePos;

    public TextPhraseInfo(AttributedString textPhrase, int relativePos) {
        this.textPhrase = textPhrase;
        this.relativePos = relativePos;
    }

    public AttributedString getTextPhrase() {
        return textPhrase;
    }

    public void setTextPhrase(AttributedString textPhrase) {
        this.textPhrase = textPhrase;
    }

    public int getRelativePos() {
        return relativePos;
    }

    public void setRelativePos(int relativePos) {
        this.relativePos = relativePos;
    }
}
