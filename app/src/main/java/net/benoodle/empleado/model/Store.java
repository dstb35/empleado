package net.benoodle.empleado.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Store {
    @SerializedName("name")
    private String name;
    @SerializedName("store_id")
    private String store_id;
    @SerializedName("modus")
    private String modus;
    @SerializedName("activa")
    private String activa;
    @SerializedName("saltarubi")
    private String saltarubi;
    @SerializedName("voluntarios")
    private String voluntarios;
    @SerializedName("zipcode")
    private String zipcode;
    @SerializedName("country")
    private String country;
    @SerializedName("price2")
    private String price2;
    @SerializedName("total")
    private String total;
    @SerializedName("count")
    private String count;
    @SerializedName("day")
    private String day;
    @SerializedName("productos")
    private Map<String, Map<String, String>> productos;

    public Store(String name){
        this.name = name.toLowerCase();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStore_id() {
        return store_id;
    }

    public String getModus() {
        return modus;
    }

    public void setModus(String modus) {
        this.modus = modus;
    }

    public String getActiva() {
        return activa;
    }

    public String getVoluntarios() {
        return voluntarios;
    }

    public void setActiva(String activa) {
        this.activa = activa;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSaltarubi() {
        return saltarubi;
    }

    public void setSaltarubi(String saltarubi) {
        this.saltarubi = saltarubi;
    }

    public String getPricealt() {
        return price2;
    }

    public void setPricealt(String pricealt) {
        this.price2 = pricealt;
    }

    public String getTotal() {
        return total;
    }

    public String getCount() {
        return count;
    }

    public String getDay() {
        return day;
    }
    public Map<String, Map<String, String>> getProductos() {return productos; }

}