package net.benoodle.empleado.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Catalog {
    private ArrayList<Node> catalog = new ArrayList<>();
    private ArrayList<String> types = new ArrayList<>();
    private ArrayList<Node> kakigoris = new ArrayList<>();
    private String[] kakigorisTitulos;

    public Catalog(ArrayList<Node> catalog) {
        //Agrupar kakigoris para que se muestren como un producto
        Pattern pattern = Pattern.compile("^*kakigori.*");
        for (Node node : catalog) {
            if (pattern.matcher(node.getTitle().toLowerCase()).matches()) {
                kakigoris.add(node);
            }
        }
        this.catalog = catalog;
        if (kakigoris.size() > 0) {
            catalog.removeAll(kakigoris);
            this.catalog.add(new Node("0", "Kakigori", kakigoris.get(0).getUrl(), "bebidas", -1, kakigoris.get(0).getPrice(), kakigoris.get(0).getSku()));
            kakigorisTitulos = new String[kakigoris.size()];
            for (int i = 0; i < kakigoris.size(); i++) {
                kakigorisTitulos[i] = kakigoris.get(i).getTitle();
            }
        }

        // Ordenar el catalogo por SKU
        int size = catalog.size();

        for(int i = 0; i<size-1; i++) {
            for (int j = i+1; j<size-1; j++) {
                if(catalog.get(i).getSku().compareTo(catalog.get(j).getSku())>0) {
                    Node aux = catalog.get(i);
                    catalog.set(i, catalog.get(j));
                    catalog.set(j, aux);
                }
            }
        }
    }

    public Node getNodeById(String id) throws Exception {
        for (Node node : catalog) {
            if (node.getProductID().compareTo(id) == 0) {
                return node;
            }
        }
        ArrayList<Node> kakigorisCopie = kakigoris;
        for (Node node : kakigorisCopie) {
            if (node.getProductID().compareTo(id) == 0) {
                return node;
            }
        }
        throw new Exception("Producto no encontrado con id: " + id);
    }

    public Node getNodeByName(String name) throws Exception {
        for (Node node : catalog) {
            if (node.getTitle().compareTo(name) == 0) {
                return node;
            }
        }
        ArrayList<Node> kakigorisCopie = kakigoris;
        for (Node node : kakigorisCopie) {
            if (node.getTitle().compareTo(name) == 0) {
                return node;
            }
        }
        throw new Exception("Producto no encontrado con nombre: " + name);
    }

    public int getSize() {
        return catalog.size();
    }

    public Node getNodeByPos(int i) {
        return catalog.get(i);
    }

    public void switchStock(String productID, boolean status) {
        try {
            int index = catalog.indexOf(getNodeById(productID));
            Node node = catalog.get(index);
            node.setStatus(status);
            catalog.set(index, node);
        } catch (Exception e) {
            e.getLocalizedMessage();
        }
    }

    public void setStock(String productID, Integer quantity) {
        try {
            int index = catalog.indexOf(getNodeById(productID));
            Node node = catalog.get(index);
            node.setStock(quantity);
            catalog.set(index, node);
        } catch (Exception e) {
            e.getLocalizedMessage();
        }
    }

    public void setPrice(String productID, String price) {
        try {
            int index = catalog.indexOf(getNodeById(productID));
            Node node = catalog.get(index);
            node.setPrice(price);
            catalog.set(index, node);
        } catch (Exception e) {
            e.getLocalizedMessage();
        }
    }

    public void doActivateAll() {
        for (int i = 0; i < catalog.size(); i++) {
            catalog.get(i).setStatus(true);
            catalog.get(i).setStock(-1);
        }
    }

    public ArrayList<Node> getCatalog() {
        return catalog;
    }

    /*public void setCatalog(ArrayList<Node> catalog) {
        this.catalog = catalog;
    }*/

    public float getPriceById(String productId) {
        float price = (float) 0;
        try {
            Node node = getNodeById(productId);
            String formatPrice = node.getPrice();
            formatPrice = formatPrice.replaceAll("[^0-9\\.]", "");
            price = new Float(formatPrice);
        } catch (Exception e) {
            e.getLocalizedMessage();
        }
        return price;
    }

    public String getIdByPosition(int pos) {
        return this.catalog.get(pos).getProductID();
    }

    /*
    Devuelve los productos por tipos.
    catType es el catálogo para un tipo de producto determinado.
    Ej. Si tipo es bebidas devolverá todas los Node de tipo bebidas
     */
    public ArrayList<Node> TypeCatalog(String tipo) {
        ArrayList<Node> catType = new ArrayList<>();
        if (tipo == null) {
            catType = catalog;
        } else {
            for (Node node : catalog) {
                if (node.getType().compareTo(tipo) == 0) {
                    catType.add(node);
                }
            }
        }
        return catType;
    }

    public ArrayList<String> getTypes() {
        return types;
    }

    /*
    Crea los tipos para la variable static types. Los tipos de productos son bebidas, ramen, etc...
     */
    public void CrearTypes() {
        String aux= "";
        int pos;
        for (Node node : catalog) {
            if (!types.contains(node.getType())) {
                types.add(node.getType());
            }
        }
        //Ordenar primero menús, segundo ramnes, tercero tapas
        if (types.contains("menu")){
            pos = types.indexOf("menu");
            aux = types.get(0);
            types.set(0, "menu");
            types.set(pos, aux);
        }

        if (types.contains("ramen")){
            pos = types.indexOf("ramen");
            aux = types.get(1);
            types.set(1, "ramen");
            types.set(pos, aux);
        }

        if (types.contains("tapas")){
            pos = types.indexOf("tapas");
            aux = types.get(2);
            types.set(2, "tapas");
            types.set(pos, aux);
        }

        if (types.contains("posrtes")){
            pos = types.indexOf("postres");
            aux = types.get(3);
            types.set(3, "postres");
            types.set(pos, aux);
        }
    }

    /*
    Devuelve el id del título de producto pasado por parámetro
     */

    public Integer getPosById(String id) {
        for (int i = 0; i < catalog.size(); i++) {
            Node node = catalog.get(i);
            if (node.getProductID().toLowerCase().compareTo(id.toLowerCase()) == 0) {
                return i;
            }
        }
        return 0;
    }

    public Node getNode(Integer i) {
        return catalog.get(i);
    }

    /* Devuelve TRUE si hay stock para el id y cantidades pasados por parámetro */
    public Boolean isStock(String id, Integer quantity) {
        try {
            Node node = this.getNodeById(id);
            if (node.getStock() >= quantity || node.getStock() == -1) {
                return Boolean.TRUE;
            }
        } catch (Exception e) {
            e.getLocalizedMessage();
        }
        return Boolean.FALSE;
    }

    /*
    Devuelve los productos del catálogo que coinciden con el parámetro String dado.
    Comprueba que haya stock.
    Ej. Si el tipo es bebidas devolverá todas las bebidas del catalog: coca-cola, fanta, etc...
     */

    public ArrayList<Node> OpcionesMenu(String tipo) {
        ArrayList<Node> opciones = new ArrayList<>();
        for (Node node : catalog) {
            if ((node.getType().toLowerCase().compareTo(tipo) == 0) && (this.isStock(node.getProductID(), 1) && (node.getProductID().compareTo("0") != 0))) {
                opciones.add(node);
            }
        }
        if (tipo.compareTo("bebidas") == 0) {
            opciones.addAll(kakigoris);
        }
        return opciones;
    }

    public void actualizarStock(String id, String stock) {
        int i = this.getPosById(id);
        this.catalog.get(i).setStock(Integer.valueOf(stock));
    }

    /*Método que sincroniza el stock del catálogo con las línes de pedido del carrito.
    Útil por si se recarga el catalágo teniendo un carrito lleno
    Devuelve false si no hubo que modificar el carrito. True si se quitaron productos.
     */
    public Boolean sincronizarStock(Order order) {
        Boolean result = Boolean.FALSE;
        for (int i = 0; i < order.getOrderItems().size(); i++) {
            OrderItem orderItem = order.getOrderItem(i);
            String productID = orderItem.getProductID();
            Integer quantity = orderItem.getQuantity();
            try {
                Node node = this.getNodeById(productID);
                Integer stock = Integer.valueOf(node.getStock());
                if (stock != -1) {
                    if (stock > quantity) {
                        node.setStock((stock - quantity));
                    } else if (stock < quantity) {
                        order.getOrderItem(i).setQuantity(stock);
                        node.setStock(0);
                        result = Boolean.TRUE;
                    } else if (stock == quantity) {
                        node.setStock(0);
                    }
                }

                //Mirar los menús, en teoría solo los menús tiene selecciones.
                for (String id : orderItem.getSelecciones()) {
                    //Integer pos = this.getPosById(id);
                    //Node seleccion = this.getNode(pos);
                    Node seleccion = this.getNodeById(id);
                    Integer seleccionStock = seleccion.getStock();
                    if (seleccionStock < 1 && seleccionStock != -1) {
                        order.removeOrderItem(i);
                        /* TODO No borra todos los menús */
                        result = Boolean.TRUE;
                    }
                    node.setStock(seleccionStock - 1);
                    //this.setNode(pos, seleccion);
                }
            } catch (Exception e) {
                e.getLocalizedMessage();
            }
        }
        return result;
    }

    public ArrayList<Node> getKakigoris() {
        return kakigoris;
    }

    public String[] getKakigorisTitulos() {
        return kakigorisTitulos;
    }

    public void restaurarKakigoris() {
        Node kakigori = null;
        for (Node node : catalog) {
            if (node.getProductID().compareTo("0") == 0) {
                kakigori = node;
                break;
            }
        }
        if (kakigori != null) {
            catalog.remove(kakigori);
        }

        catalog.addAll(kakigoris);
    }

    public boolean existsMenuKakigori(){
        for (Node node : catalog) {
            if (node.getProductID().compareTo("28") == 0) {
                return true;
            }
        }
        return false;
    }
}
