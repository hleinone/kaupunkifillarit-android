package fi.kaupunkifillarit.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

@JsonObject
public class Rack {
    @JsonField
    public String id;
    @JsonField
    public String name;
    @JsonField(name = "lat")
    public double latitude;
    @JsonField(name = "lon")
    public double longitude;
    @JsonField(name = "bikesAvailable")
    public int bikes;
    @JsonField(name = "spacesAvailable")
    public int spaces;

    public Rack() { }

    public Rack(String id,
                String name,
                double latitude,
                double longitude,
                int bikes,
                int spaces) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.bikes = bikes;
        this.spaces = spaces;
    }

    public int getSlots() {
        return bikes + spaces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rack tram = (Rack) o;

        return id.equals(tram.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
