package com.tinderapp.model.api_data;

public class LocationDTO {
    private double lon, lat;

    public LocationDTO(){}

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
