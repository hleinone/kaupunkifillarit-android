package fi.kaupunkifillarit.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class MapLocation {
    @JsonField
    public double latitude;
    @JsonField
    public double longitude;
    @JsonField
    public float zoom;
    @JsonField
    public float bearing;
    @JsonField
    public float tilt;

    public MapLocation() { }

    public MapLocation(double latitude,
                       double longitude,
                       float zoom,
                       float bearing,
                       float tilt) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.zoom = zoom;
        this.bearing = bearing;
        this.tilt = tilt;
    }

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