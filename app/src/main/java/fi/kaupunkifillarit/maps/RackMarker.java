package fi.kaupunkifillarit.maps;

import fi.kaupunkifillarit.model.Rack;

public class RackMarker {
    private final Maps.MarkerWrapper marker;

    RackMarker(Maps.MarkerWrapper marker) {
        this.marker = marker;
    }

    public void update(Rack rack) {
        marker.setPosition(rack.latitude, rack.longitude);
    }
}
