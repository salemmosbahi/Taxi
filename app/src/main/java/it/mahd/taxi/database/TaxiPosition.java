package it.mahd.taxi.database;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by salem on 3/18/16.
 */
public class TaxiPosition {
    private String token, socket;
    private Double latitude, longitude;
    private Marker marker;

    public TaxiPosition(String token, String socket, Double latitude, Double longitude, Marker marker) {
        this.token = token;
        this.socket = socket;
        this.latitude = latitude;
        this.longitude = longitude;
        this.marker = marker;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSocket() {
        return socket;
    }

    public void setSocket(String socket) {
        this.socket = socket;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}
