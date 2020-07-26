package net.benoodle.empleado.model;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import static net.benoodle.empleado.MainActivity.catalog;

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
    @SerializedName("pagado")
    private Boolean pagado;

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

    public String getTotal(){
        return total;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void recalculateTotal() {
        Float total = new Float(0.00 );
        try{
            for (OrderItem orderItem : orderItems){
                Node node = catalog.getNodeById(orderItem.getProductID());
                String formatPrice = node.getPrice();
                //quitar carácteres y comas, se quedan los puntos como separador de decimales
                formatPrice = formatPrice.replaceAll("[^0-9\\.]", "");
                Float subtotal = Float.parseFloat(formatPrice);
                total += subtotal * orderItem.getQuantity();
            }
        } catch (Exception e){
            this.total = e.getLocalizedMessage();
        }
        this.total = String.format("%.2f", total);
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

    public Boolean getPagado() {
        if (pagado == null){
            return false;
        }else{
            return pagado;
        }
    }

    public void setPagado(Boolean pagado) {
        this.pagado = pagado;
    }

    public void removeOrderItem(int i){
        orderItems.remove(i);
    }

    public void removeOrderItem(ArrayList<OrderItem> orderItemsToRemove){
        orderItems.removeAll(orderItemsToRemove);
        recalculateTotal();
    }

    public void changeQuantity(int pos, int quantity){
        this.orderItems.get(pos).setQuantity(quantity);
        recalculateTotal();
    }

    public void removeOrderItemsOnZero(){
        ArrayList<OrderItem> orderItemsToRemove = new ArrayList<>();
        for (OrderItem orderItem : orderItems){
            if (orderItem.getQuantity() == 0){
                orderItemsToRemove.add(orderItem);
            }
        }
        orderItems.removeAll(orderItemsToRemove);
    }
}