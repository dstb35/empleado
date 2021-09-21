package net.benoodle.empleado.model;

import android.media.Image;

import com.google.gson.annotations.SerializedName;

public class Tipo {
    @SerializedName("name")
    private String name;
    @SerializedName("url")
    private String url;
    @SerializedName("id")
    private String id;

    private Image image;

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Tipo(String name, String url, Image image){
        this.name = name.toLowerCase();
        this.url = url;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() { return id;}
}

