package fi.kaupunkifillarit.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MapLocation {
    public double latitude;
    public double longitude;
    public float zoom;
    public float bearing;
    public float tilt;

    @JsonCreator
    public MapLocation(@JsonProperty("latitude") double latitude,
                       @JsonProperty("longitude") double longitude,
                       @JsonProperty("zoom") float zoom,
                       @JsonProperty("bearing") float bearing,
                       @JsonProperty("tilt") float tilt) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.zoom = zoom;
        this.bearing = bearing;
        this.tilt = tilt;
    }

    @JsonIgnore
    public boolean isWithinDesiredMapBounds() {
        return latitude > 60.151568 &&
                latitude < 60.194072 &&
                longitude > 24.903618 &&
                longitude < 24.984335;
    }

    @Override
    public String toString() {
        return "MapLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", zoom=" + zoom +
                ", bearing=" + bearing +
                ", tilt=" + tilt +
                '}';
    }
}
