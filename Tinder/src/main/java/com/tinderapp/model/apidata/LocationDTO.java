package com.tinderapp.model.apidata;

public class LocationDTO {
    private double lon;
    private double lat;

    public LocationDTO(){
        //needed for serialization
    }

    public LocationDTO(double lat, double lon) {
        this.lon = lon;
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
}
