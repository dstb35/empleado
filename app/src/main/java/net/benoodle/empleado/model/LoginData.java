package net.benoodle.empleado.model;

public class LoginData {

    final String name;
    final String pass;
    String lang;

    public LoginData(String name, String pass, String lang) {
        this.name = name;
        this.pass = pass;
        this.lang = lang;
    }
}
