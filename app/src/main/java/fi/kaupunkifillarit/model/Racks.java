package fi.kaupunkifillarit.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import fj.data.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Racks {
    public final Set<Rack> racks;

    @JsonCreator
    public Racks(@JsonProperty("bikeRentalStations") Set<Rack> racks) {
        this.racks = racks;
    }
}
