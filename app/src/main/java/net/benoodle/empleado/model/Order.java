package net.benoodle.empleado.model;

import com.google.gson.annotations.SerializedName;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static net.benoodle.empleado.MainActivity.catalog;

public class Order {

    @SerializedName("store")
    private String store;
    @SerializedName("state")
    private String state;
    @SerializedName("orderItems")
    //private String orderItems;
    private ArrayList<OrderItem> orderItems = new ArrayList<>();
    @SerializedName("orderId")
    private String orderId;
    @SerializedName("total")
    private String total;
    @SerializedName("empleado")
    private String empleado = "0";
    @SerializedName("pagado")
    private Boolean pagado;
    @SerializedName("cuppons")
    private HashMap<String, String> cuppons = new HashMap<>();
    @SerializedName("customer")
    private String customer;
    @SerializedName("updated")
    private String updated;


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

    public String getTotal() {
        return total;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void recalculateTotal() {
        float total = (float) 0;
        try {
            for (OrderItem orderItem : orderItems) {
                Node node = catalog.getNodeById(orderItem.getProductID());
                String formatPrice = node.getPrice();
                //quitar carácteres y comas, se quedan los puntos como separador de decimales
                formatPrice = formatPrice.replaceAll("[^0-9\\.]", "");
                float subtotal = Float.parseFloat(formatPrice);
                total += subtotal * orderItem.getQuantity();
            }
        } catch (Exception e) {
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

    public String getCustomer() {
        return customer;
    }

    public void setEmpleado(String empleado) {
        this.empleado = empleado;
    }

    public Boolean getPagado() {
        if (pagado == null) {
            return false;
        } else {
            return pagado;
        }
    }

    public void setPagado(Boolean pagado) {
        this.pagado = pagado;
    }

    public void removeOrderItem(int i) {
        orderItems.remove(i);
    }

    public void removeOrderItem(ArrayList<OrderItem> orderItemsToRemove) {
        orderItems.removeAll(orderItemsToRemove);
        recalculateTotal();
    }

    public void changeQuantity(int pos, int quantity) {
        this.orderItems.get(pos).setQuantity(quantity);
        recalculateTotal();
    }

    public void removeOrderItemsOnZero() {
        ArrayList<OrderItem> orderItemsToRemove = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getQuantity() == 0) {
                orderItemsToRemove.add(orderItem);
            }
        }
        orderItems.removeAll(orderItemsToRemove);
    }

    public String getCupponsasString() {
        if (this.cuppons.isEmpty()) {
            return "";
        } else {
            String cpString = "Cupones aplicados :";
            for (String label : cuppons.keySet()) {
                cpString = cpString.concat(label + " : " + cuppons.get(label) + " €");
            }
            return cpString;
        }
    }

    public String getUpdated() {
        return updated;

        /*Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();//get your local time zone.
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        sdf.setTimeZone(tz);//set time zone.
        String localTime = sdf.format(new Date(updated*1000));
        Date date = new Date();
        try {
            date = sdf.parse(localTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.toString();
        /*Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(updated*1000);
        //return calendar.getTime().toString();s
        //System.out.println("Date1:"+calendar.getTime().toString());

        calendar.set(Calendar.HOUR_OF_DAY, 0);  //For 12 AM use 0 and for 12 PM use 12.
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        Date date = calendar.getTime();
        return date.toString();
        //System.out.println("Date2:"+date.toString());*/
    }
}