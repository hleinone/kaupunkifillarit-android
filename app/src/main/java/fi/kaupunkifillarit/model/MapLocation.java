package fi.kaupunkifillarit.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MapLocation {
    public double latitude;
    public double longitude;
    public float zoom;
    public float bearing;

    @JsonCreator
    public MapLocation(@JsonProperty("latitude") double latitude,
                       @JsonProperty("longitude") double longitude,
                       @JsonProperty("zoom") float zoom,
                       @JsonProperty("bearing") float bearing) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.zoom = zoom;
        this.bearing = bearing;
    }
}
