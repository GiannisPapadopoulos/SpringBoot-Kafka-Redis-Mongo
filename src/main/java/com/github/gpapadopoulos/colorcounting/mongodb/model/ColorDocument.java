package com.github.gpapadopoulos.colorcounting.mongodb.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "colors")
public class ColorDocument {

    @Id
    private String id;

    private String color;

    public ColorDocument() {}

    public ColorDocument(String color) {
        this.color = color;
    }

    public ColorDocument(String id, String color) {
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
        return "ColorDocument{" +
                "id='" + id + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}