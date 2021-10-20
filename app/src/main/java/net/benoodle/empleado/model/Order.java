package net.benoodle.empleado.model;

import com.google.gson.annotations.SerializedName;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static net.benoodle.empleado.MainActivity.catalog;
import static java.lang.Boolean.TRUE;

public class Order {
    @SerializedName("type")
    private String type;
    @SerializedName("email")
    private String email;
    @SerializedName("store_id")
    private String store_id;
    /*@SerializedName("store") CAMBIADO
    private String store;*/
    @SerializedName("placed")
    private Boolean placed = TRUE;
    @SerializedName("state")
    private String state;
    //@SerializedName("orderItems") CAMBIADO
    @SerializedName ("order_items")
    private ArrayList<OrderItem> orderItems = new ArrayList<>();
    //@SerializedName("orderId") CAMBIADO
    @SerializedName("order_id")
    private String orderId;
    @SerializedName("total")
    private String total;
    @SerializedName("empleado")
    private String empleado = "0";
    @SerializedName("field_voluntario")
    private String voluntario;
    @SerializedName("pagado")
    private Boolean pagado;
    /*@SerializedName("cuppons")
    private HashMap<String, String> cuppons = new HashMap<>();*/
    @SerializedName("cuppons")
    private ArrayList<Cuppon> cuppons = new ArrayList<>();
    /*@SerializedName("customer") CAMBIADO
    private String customer;*/
    @SerializedName("updated")
    private String updated;


    public Order() {
    }

    public Order(String store_id) {
        this.type = "default";
        this.store_id = store_id;
    }

    public String getStore() {
        return store_id;
    }

    public void setStore(String store) {
        this.store_id = store;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getVoluntario() {
        return voluntario;
    }

    public void setVoluntario(String voluntario) {
        this.voluntario = voluntario;
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

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTotalasString(){
        return this.total;
    }
    public Float getTotal() throws Exception {
        float total = (float) 0;
        float discounts = (float) 0;
        for (OrderItem orderItem : orderItems) {
            Node node = catalog.getNodeById(orderItem.getProductID());
            String formatPrice = node.getPrice();
            //quitar carácteres y comas, se quedan los puntos como separador de decimales
            formatPrice = formatPrice.replaceAll("[^0-9\\.]", "");
            float subtotal = Float.parseFloat(formatPrice);
            total += subtotal * orderItem.getQuantity();
        }
        /*for (Map.Entry<String, Integer> cuppon : cuppons.entrySet()) {
            discounts += total * cuppon.getValue() / 100;
        }*/
        for (Cuppon cuppon : cuppons) {
            if (cuppon.getType().compareTo("product") == 0) {
                Node node = catalog.getNodeById(String.valueOf(cuppon.getProduct()));
                total -= Float.parseFloat(node.getPrice());
            }
        }
        for (Cuppon cuppon : cuppons) {
            if (cuppon.getType().compareTo("percentage") == 0) {
                discounts += total * cuppon.getPercentage() / 100;
            }
        }
        total -= discounts;
        this.total = String.valueOf(total);
        return total;
    }

    public float recalculateTotal() throws Exception {
        float total = (float) 0;
        float discounts = (float) 0;
        for (OrderItem orderItem : orderItems) {
            Node node = catalog.getNodeById(orderItem.getProductID());
            String formatPrice = node.getPrice();
            //quitar carácteres y comas, se quedan los puntos como separador de decimales
            formatPrice = formatPrice.replaceAll("[^0-9\\.]", "");
            float subtotal = Float.parseFloat(formatPrice);
            total += subtotal * orderItem.getQuantity();
        }
        /*for (Map.Entry<String, Integer> cuppon : cuppons.entrySet()) {
            discounts += total * cuppon.getValue() / 100;
        }*/
        for (Cuppon cuppon : cuppons) {
            if (cuppon.getType().compareTo("product") == 0) {
                Node node = catalog.getNodeById(String.valueOf(cuppon.getProduct()));
                total -= Float.parseFloat(node.getPrice());
            }
        }
        for (Cuppon cuppon : cuppons) {
            if (cuppon.getType().compareTo("percentage") == 0) {
                discounts += total * cuppon.getPercentage() / 100;
            }
        }
        total -= discounts;
        this.total = String.valueOf(total);
        return total;
    }


    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /*public void recalculateTotal() {
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
    }*/

    public void setTotal(String total) {
        this.total = total;
    }

    public String getEmpleado() {
        return empleado;
    }

    public String getCustomer() {
        return email;
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
    }

    public void changeQuantity(int pos, int quantity) {
        this.orderItems.get(pos).setQuantity(quantity);
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
            for (Cuppon cuppon : this.cuppons) {
                if (cuppon.getType().compareTo("percentage") == 0){
                    cpString = cpString.concat(cuppon.getCuppon() + " : " + cuppon.getPercentage() + " %");
                }else{
                    cpString = cpString.concat(cuppon.getCuppon() + " : " + cuppon.getProduct() + " Producto");
                }
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

    /*
 Añade un producto al pedido, primero comprueba que el producto no exista
 para actualizar la cantidad. Lanza una excepción si no hay stock.
 Con stock -1 no hay control de stock.
  */
    public void addOrderItem(String productID, int quantity)
            throws Exception {
        int pos = catalog.getPosById(productID);
        Node node = catalog.getNode(pos);
        Integer stock = node.getStock();
        if (!catalog.isStock(productID, quantity)){
            throw new Exception();
        }
        for (int i=0; i<orderItems.size(); i++) {
            //Comprobar si está este producto en el carrito de una compra anterior
            if (orderItems.get(i).getProductID().compareTo(productID) == 0) {
                int newQuantity = quantity + orderItems.get(i).getQuantity();
                //Si la cantidad de orderItems llega a 0 eliminamos el orderItem
                if (newQuantity == 0){
                    orderItems.remove(i);
                }else{
                    orderItems.get(i).setQuantity(newQuantity);
                }
                //Actualizar el stock del product del catálogo
                if (stock != -1){
                    catalog.getNode(pos).updateStock(quantity);
                }
                deleteOrphanCuppons();
                return;
            }
        }
        //Si no se encontraba es una compra nueva, no puede ser una cantidad negativa
        if (quantity > 0){
            OrderItem orderItem = new OrderItem(productID, quantity);
            orderItems.add(orderItem);
            if (stock != -1){
                try{
                    catalog.getNode(pos).updateStock(quantity);
                }catch (Exception e){
                    e.getLocalizedMessage();
                }
            }
        }
    }

    /*No dejar cupones de productos sin al menos un producto asociado*/
    public void deleteOrphanCuppons ()
            throws NoSuchElementException {
        for (Cuppon cuppon : cuppons) {
            if (cuppon.getType().compareTo("product") == 0) {
                if (!orderContainsProduct(cuppon.getProduct().toString())) {
                    cuppons.remove(cuppon);
                    throw new NoSuchElementException();
                }
            }
        }
    }

    public boolean orderContainsProduct(String id) {
        for (OrderItem orderItem : orderItems) {
            if (orderItem.getProductID().compareTo(id) == 0) {
                return true;
            }
        }
        return false;
    }

    public void addMenuItem(String productID, ArrayList<String> selecciones, int quantity)
            throws Exception{
        /*
        Aunque el sku exista en algún OrderItem de la Order actual debemos crear uno nuevo porque
        al tener diferentes selecciones de menú conforma una línea de pedido nueva, con el mismo sku.
         */
        if (!catalog.isStock(productID, quantity)){
            throw new Exception();
        }
        for(String id : selecciones){
            try{
                Integer i = catalog.getPosById(id);
                Node node = catalog.getNode(i);
                if (node.getStock() != -1){
                    //En teoría no permite hacer selecciones sin stock el menu activity
                    //por el método catalog.opcionesMenu
                    if (!catalog.isStock(id, 1)) {
                        throw new Exception();
                    }
                    catalog.getNode(i).updateStock(1);
                }
            }catch (Exception e){
                e.getLocalizedMessage();
            }
        }
        OrderItem orderItem = new OrderItem(productID, quantity, selecciones);
        //Actualizar el stock del menú en sí mismo
        try{
            int pos = catalog.getPosById(productID);
            Node node = catalog.getNode(pos);
            if (node.getStock() != -1){
                catalog.getNode(pos).updateStock(quantity);
            }
        }catch (Exception e){
            e.getLocalizedMessage();
        }
        orderItems.add(orderItem);
    }

    /*
  Método que calcula las cantidades de cada producto que se van a pedir,
  ya que en los menús se piden productos como elecciones que no reflejan las cantidades reales.
  Útil para verificar el stock en el server antes de procesarlo, se envía como Map adjunto.
  Se llama justo antes de hacer la compra cuando ya no va a haber más cambios
  Ej: Si se piden 3 colas y dos menús que llevan colas y ramens, en realidad son 5 colas y 2 ramens
   */
    public Map<String, Integer> calculateTotalsQuantity (){
        Map <String, Integer> totalQuantity = new HashMap<>();
        for(OrderItem orderItem : orderItems){
            String productID = orderItem.getProductID();
            Integer quantity = orderItem.getQuantity();
            if (!orderItem.getSelecciones().isEmpty()){
                for(String seleccion : orderItem.getSelecciones()){
                    if (totalQuantity.containsKey(seleccion)){
                        totalQuantity.put(seleccion, totalQuantity.get(seleccion)+1);
                    }else{
                        totalQuantity.put(seleccion, 1);
                    }
                }
            }
            if (totalQuantity.containsKey(productID)){
                totalQuantity.put(productID, totalQuantity.get(productID)+quantity);
            }else{
                totalQuantity.put(productID, quantity);
            }
        }
        return totalQuantity;
    }

    public boolean applyCuppon(Cuppon cuppon) {
        if (this.cuppons.contains(cuppon)) {
            return false;
        } else {
            this.cuppons.add(cuppon);
            return true;
        }
    }

    /*Método que elimina las líneas de pedidos para la id dado,
   mira también las selecciones de menú.
   Útil por si el servidor deniega la compra por falta de stock.
    */
    public void removeOrderItemByStock (String id){
        ArrayList<OrderItem> found = new ArrayList<>();
        for (int i=0; i<orderItems.size(); i++) {
            OrderItem orderItem = orderItems.get(i);
            if (orderItem.getProductID().compareTo(id) == 0){
                found.add(orderItem);
                continue;
            }
            for (String seleccion : orderItem.getSelecciones()) {
                if (seleccion.compareTo(id) == 0){
                    found.add(orderItem);
                    continue;
                }
            }
        }
        orderItems.removeAll(found);
        deleteOrphanCuppons();
    }

    /*
   Método que devuelve los productos asociados a un OrderItem pasado por parámetro.
   Útil para saber los productos asociados al pedido de un menú.
    */
    public String getSeleccionesByID (OrderItem orderItem) {
        String titulos = new String();
        for (String seleccion : orderItem.getSelecciones()){
            try {
                Node node = catalog.getNodeById(seleccion);
                titulos = titulos.concat(node.getTitle()+" ");
            } catch (Exception e){
                titulos.concat(e.getMessage());
            }
        }
        return titulos;
    }

    public OrderItem getOrderItem(int i) { return orderItems.get(i); }

}