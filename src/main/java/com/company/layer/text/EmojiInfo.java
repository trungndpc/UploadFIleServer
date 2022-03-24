package com.company.layer.text;

public class EmojiInfo {
    private int[] codePoints;
    private int relativePos;

    public EmojiInfo(int[] codePoints, int relativePos) {
        this.codePoints = codePoints;
        this.relativePos = relativePos;
    }

    public int[] getCodePoints() {
        return codePoints;
    }

    public void setCodePoints(int[] codePoints) {
        this.codePoints = codePoints;
    }

    public int getRelativePos() {
        return relativePos;
    }

    public void setRelativePos(int relativePos) {
        this.relativePos = relativePos;
    }
}
