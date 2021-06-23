package net.benoodle.empleado.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class User {
    @SerializedName("mail")
    private String mail;

    @SerializedName("name")
    private String name;

    @SerializedName("uid")
    private String uid;

    private String empleado;

    @SerializedName("roles")
    private ArrayList<String> roles = new ArrayList<>();

    public User(String mail, String name, String uid){
        this.mail = mail;
        this.name = name;
        this.uid = uid;
    }

    public String getEmpleado() {
        return empleado;
    }

    public void setEmpleado(String empleado) {
        this.empleado = empleado;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isEncargado() {
        return roles.contains("encargado");
    }
}
