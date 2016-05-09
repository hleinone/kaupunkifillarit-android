package fi.kaupunkifillarit.marker;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import fi.kaupunkifillarit.model.Rack;

public class RackMarker {
    private final Marker marker;

    RackMarker(Marker marker) {
        this.marker = marker;
    }

    public void update(Rack rack) {
        LatLng position = new LatLng(rack.latitude, rack.longitude);
        marker.setPosition(position);
    }
}
