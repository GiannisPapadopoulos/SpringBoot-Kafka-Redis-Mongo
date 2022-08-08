package com.github.gpapadopoulos.colorcounting.redis.model;

import org.springframework.data.redis.core.RedisHash;

@RedisHash("Color")
public class Color {

    private String id;
    private String color;

    public Color() {}
    public Color(String color) {
        this.color = color;
    }

    public Color(String id,  String color) {
        this.id = id;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "Color{" +
                "id='" + id + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
