package net.benoodle.empleado.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class OrderItem {

    @SerializedName("quantity")
    private int quantity;
    @SerializedName("id")
    private String id;

    //Selecciones apunta al productID (variation ID) en el server, elecciones del menú.
    @SerializedName("selecciones")
    private ArrayList<String> selecciones = new ArrayList<String>();


    public OrderItem(String id, int quantity){
        this.id = id;
        this.quantity = quantity;
    }

    public OrderItem(String id, int quantity, ArrayList<String> selecciones){
        this.id = id;
        this.quantity = quantity;
        this.selecciones = selecciones;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getSelecciones() {
        return selecciones;
    }

    public void setSelecciones(ArrayList<String> selecciones) {
        this.selecciones = selecciones;
    }

}


