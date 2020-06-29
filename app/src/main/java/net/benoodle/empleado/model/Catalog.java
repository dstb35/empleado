package net.benoodle.empleado.model;

import android.widget.Toast;
import net.benoodle.empleado.MainActivity;
import net.benoodle.empleado.StockActivity;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class Catalog {
    private ArrayList<Node> catalog = new ArrayList<>();

    public Catalog (ArrayList<Node> catalog) {
        this.catalog = catalog;
    }

    public Node getNodeById (String id) throws Exception{
        for (Node node : catalog){
            if (node.getProductID().compareTo(id) == 0){
                return node;
            }
        }
        throw new Exception("Producto no encontrado con id: " + id);
    }

    public int getSize() {
        return catalog.size();
    }

    public Node getNodeByPos (int i){
        return catalog.get(i);
    }

    public void switchStock(String productID, boolean status){
        try{
            int index = catalog.indexOf(getNodeById(productID));
            Node node = catalog.get(index);
            node.setStatus(status);
            catalog.set(index, node);
        }catch (Exception e){
           e.getLocalizedMessage();
        }
    }

    public void setStock(String productID, String quantity){
        try{
            int index = catalog.indexOf(getNodeById(productID));
            Node node = catalog.get(index);
            node.setStock(quantity);
            catalog.set(index, node);
        }catch (Exception e){
            e.getLocalizedMessage();
        }
    }

    public void doActivateAll() {
        for (int i=0; i < catalog.size(); i++){
            catalog.get(i).setStatus(true);
            catalog.get(i).setStock("-1");
        }
    }

    public ArrayList<Node> getCatalog() {
        return catalog;
    }

    public void setCatalog(ArrayList<Node> catalog) {
        this.catalog = catalog;
    }
}
