package net.benoodle.empleado.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


public class Order {

    @SerializedName("store")
    private String store;
    @SerializedName("state")
    private String state;
    @SerializedName ("orderItems")
    //private String orderItems;
    private ArrayList<OrderItem> orderItems = new ArrayList<>() ;
    @SerializedName("orderId")
    private String orderId;
    @SerializedName("total")
    private String total;
    @SerializedName("empleado")
    private String empleado = "0";

    public Order() {
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public ArrayList<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(ArrayList<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getEmpleado() {
        return empleado;
    }

    public void setEmpleado(String empleado) {
        this.empleado = empleado;
    }
}