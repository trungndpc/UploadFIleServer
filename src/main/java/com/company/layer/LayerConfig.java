package com.company.layer;

import java.awt.*;

public abstract class LayerConfig {
    protected int layerId;
    protected int top;
    protected int left;
    protected int width;
    protected int height;
    protected boolean drawRect = false;
    protected boolean visibility = true;

    protected LayerType type;

    public int getLayerId() {
        return layerId;
    }

    public void setLayerId(int layerId) {
        this.layerId = layerId;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public LayerType getType() {
        return type;
    }

    public void setType(LayerType type) {
        this.type = type;
    }

    public boolean isDrawRect() {
        return drawRect;
    }

    public void setDrawRect(boolean drawRect) {
        this.drawRect = drawRect;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }


    public abstract void applyLayerConfig(Graphics2D graphics) throws Exception;
}
