package it.mahd.taxi.database;

/**
 * Created by salem on 4/2/16.
 */
public class Taxi {
    private String token;
    private String picture;
    private String color;
    private String username;
    private String dateN;
    private Float pt;
    private String model;
    private String serial;
    private String places;
    private String luggages;

    public Taxi(String token, String picture, String color, String username, String dateN, Float pt, String model, String serial, String places, String luggages) {
        this.token = token;
        this.picture = picture;
        this.color = color;
        this.username = username;
        this.dateN = dateN;
        this.pt = pt;
        this.model = model;
        this.serial = serial;
        this.places = places;
        this.luggages = luggages;
    }

    public String getToken() {
        return token;
    }

    public String getPicture() {
        return picture;
    }

    public String getColor() {
        return color;
    }

    public String getUsername() {
        return username;
    }

    public String getDateN() {
        return dateN;
    }

    public Float getPt() {
        return pt;
    }

    public String getModel() {
        return model;
    }

    public String getSerial() {
        return serial;
    }

    public String getPlaces() {
        return places;
    }

    public String getLuggages() {
        return luggages;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDateN(String dateN) {
        this.dateN = dateN;
    }

    public void setPt(Float pt) {
        this.pt = pt;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public void setPlaces(String places) {
        this.places = places;
    }

    public void setLuggages(String luggages) {
        this.luggages = luggages;
    }
}
