package net.benoodle.empleado.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import static java.lang.Boolean.FALSE;

import java.util.ArrayList;


public class Node implements Parcelable {

    @SerializedName("variation_id")
    private String productID;
    private String title;
    private String type;
    @SerializedName("status")
    private Boolean status;
    @SerializedName("stock")
    private Integer stock;
    @SerializedName("price")
    private String price;
    @SerializedName("productos")
    private ArrayList<String> productos;
    @SerializedName("extras")
    private ArrayList<String> extras;
    @SerializedName("url")
    private String url;
    private String body;
    private String id;
    private String name;
    private String grade;

    public Node(String productID, String title, String url, String type, Integer stock) {
        this.productID = productID;
        this.title = title;
        this.url = url;
        this.type = type;
        this.stock = stock;
        this.status = true;
    }

    public Node(Parcel in) {
        String[] data = new String[3];

        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.id = data[0];
        this.name = data[1];
        this.grade = data[2];
    }

    public String getPrice() {
        return price;
    }

    public String getType() {
        return type;
    }

    public String getBody() {
        return body;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Integer getStock() {
        if (stock != null) {
            return stock;
        } else {
            return -1;
        }
    }

    public ArrayList<String> getProductos() {
        return productos;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Node))
            return false;
        if (obj == this)
            return true;
        return this.getProductID() == ((Node) obj).getProductID();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{this.id,
                this.name,
                this.grade});
    }

    public static final Creator CREATOR = new Creator() {
        public Node createFromParcel(Parcel in) {
            return new Node(in);
        }

        public Node[] newArray(int size) {
            return new Node[size];
        }
    };

    //Actuliza el stock del producto
    public void updateStock(Integer quantity)
            throws Exception {
        if (this.stock != -1) {
            if (quantity > this.stock) {
                throw new Exception();
            }
            this.stock = this.stock - quantity;
        }
    }

    public ArrayList<String> getExtras() {
        return extras;
    }

    public String getUrl() {
        return url;
    }
}
