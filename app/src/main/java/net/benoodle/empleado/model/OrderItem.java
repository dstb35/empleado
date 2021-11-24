package net.benoodle.empleado.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class OrderItem {

    @SerializedName("quantity")
    private int quantity;
    /*@SerializedName("id")
    private String id;*/
    @SerializedName("variation_id")
    private String productID;
    //Selecciones apunta al productID (variation ID) en el server, elecciones del menú.
    @SerializedName("selecciones")
    private ArrayList<String> selecciones = new ArrayList<>();

    public OrderItem(String id, int quantity){
        this.productID = id;
        this.quantity = quantity;
    }

    public OrderItem(String id, int quantity, ArrayList<String> selecciones){
        this.productID = id;
        this.quantity = quantity;
        this.selecciones = selecciones;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setId(String id) {
        this.productID = id;
    }

    public String getProductID() {
        return productID;
    }

    public ArrayList<String> getSelecciones() {
        return selecciones;
    }

    public void setSelecciones(ArrayList<String> selecciones) {
        this.selecciones = selecciones;
    }
}


