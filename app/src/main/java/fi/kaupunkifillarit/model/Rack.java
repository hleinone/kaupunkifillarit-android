package fi.kaupunkifillarit.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Rack {
    public final String id;
    public final String name;
    public final double longitude;
    public final double latitude;
    public final int bikes;
    public final int slots;

    @JsonCreator
    public Rack(@JsonProperty("id") String id,
                @JsonProperty("name") String name,
                @JsonProperty("lat") double latitude,
                @JsonProperty("lon") double longitude,
                @JsonProperty("bikesAvailable") int bikes,
                @JsonProperty("spacesAvailable") int spaces) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.bikes = bikes;
        this.slots = bikes + spaces;
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
