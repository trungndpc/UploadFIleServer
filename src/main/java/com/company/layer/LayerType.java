package com.company.layer;

public enum LayerType {
    IMAGE(2), TEXT(3), BACKGROUND(1);
    private int order;

    LayerType(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
