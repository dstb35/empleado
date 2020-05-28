package net.benoodle.empleado.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import static java.lang.Boolean.FALSE;


public class Node implements Parcelable {

    @SerializedName("variation_id")
    private String productID;
    @SerializedName("sku")
    private String sku;
    private String title;
    private String type;
    private String body;
    @SerializedName("field_image")
    private String url;
    @SerializedName("price__number")
    private String price;
    @SerializedName("field_productos")
    private String productos;
    @SerializedName("status")
    private Integer status;
    @SerializedName("field_stock")
    private String stock;
    private String id;
    private String name;
    private String grade;

    public Node(String productID, String title, String body, String url, String price, String type, String stock) {
        this.productID = productID;
        this.title = title;
        this.body = body;
        this.url = url;
        this.price = price;
        this.type = type;
        this.stock = stock;
    }

    public Node (Parcel in){
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

    public void setPrice(String price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getProductos() {
        return productos;
    }

    public void setProductos(String productos) {
        this.productos = productos;
    }

    public Integer getStatus() {
        if (status != null){
            return status;
        }else{
            return 0;
        }
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStock() {
        return stock;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Node))
            return false;
        if (obj == this)
            return true;
        return this.getSku() == ((Node) obj).getSku();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.id,
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
}
