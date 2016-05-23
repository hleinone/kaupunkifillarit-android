package fi.kaupunkifillarit.model;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.Set;

@JsonObject
public class Racks {
    @JsonField(name = "bikeRentalStations")
    public Set<Rack> racks;

    public Racks() { }

    public Racks(Set<Rack> racks) {
        this.racks = racks;
    }
}
