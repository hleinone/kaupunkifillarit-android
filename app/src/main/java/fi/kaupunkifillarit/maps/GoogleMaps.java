package fi.kaupunkifillarit.maps;

import android.graphics.Bitmap;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import fi.kaupunkifillarit.model.MapLocation;
import rx.Observable;
import rx.Subscriber;

public abstract class GoogleMaps {
    public static Observable<Maps.MapWrapper> create(MapFragment mapFragment) {
        return Observable.create((Subscriber<? super Maps.MapWrapper> subscriber) -> {
            mapFragment.getMapAsync(googleMap -> {
                if (googleMap != null) {
                    subscriber.onNext(new MapsWrapper(googleMap));
                } else {
                    subscriber.onError(new NullPointerException("Could not obtain googleMap"));
                }
            });
        });
    }

    public static class MapsWrapper implements Maps.MapWrapper<MarkerWrapper, MarkerOptionsWrapper> {
        private final GoogleMap googleMap;

        public MapsWrapper(GoogleMap googleMap) {
            this.googleMap = googleMap;
        }

        @Override
        public void animateToMapLocation(MapLocation mapLocation) {
            googleMap.animateCamera(CameraUpdateFactory.
                    newCameraPosition(new CameraPosition.Builder()
                            .target(new LatLng(mapLocation.latitude, mapLocation.longitude))
                            .zoom(mapLocation.zoom)
                            .bearing(mapLocation.bearing)
                            .tilt(mapLocation.tilt)
                            .build()));
        }

        @Override
        public void setMyLocationEnabled(boolean enabled) {
            //noinspection MissingPermission
            googleMap.setMyLocationEnabled(enabled);
        }

        @Override
        public void setTrafficEnabled(boolean enabled) {
            googleMap.setTrafficEnabled(enabled);
        }

        @Override
        public void setPadding(int left, int top, int right, int bottom) {
            googleMap.setPadding(left, top, right, bottom);
        }

        @Override
        public void setOnMarkerClickListener(Maps.OnMarkerClickListener listener) {
            googleMap.setOnMarkerClickListener((Marker marker) ->
                    listener.onMarkerClick(new MarkerWrapper(marker)));
        }

        @Override
        public void setOnMapLocationChangeListener(Maps.OnMapLocationChangeListener listener) {
            googleMap.setOnCameraChangeListener((CameraPosition cameraPosition) ->
                    listener.onMapLocationChange(
                            new MapLocation(cameraPosition.target.latitude,
                                    cameraPosition.target.longitude,
                                    cameraPosition.zoom,
                                    cameraPosition.bearing,
                                    cameraPosition.tilt)));
        }

        @Override
        public void setOnMyLocationButtonClickListener(Maps.OnMyLocationButtonClickListener
                                                               listener) {
            googleMap.setOnMyLocationButtonClickListener(() -> listener.onMyLocationButtonClick());
        }

        @Override
        public MarkerOptionsWrapper createMarkerOptions() {
            return new MarkerOptionsWrapper(new MarkerOptions());
        }

        @Override
        public MarkerWrapper addMarker(MarkerOptionsWrapper markerOptions) {
            return new MarkerWrapper(googleMap.addMarker(markerOptions.getMarkerOptions()));
        }
    }

    public static class MarkerOptionsWrapper implements Maps.MarkerOptionsWrapper<MarkerOptions> {
        private final MarkerOptions markerOptions;

        private MarkerOptionsWrapper(MarkerOptions markerOptions) {
            this.markerOptions = markerOptions;
        }

        @Override
        public MarkerOptions getMarkerOptions() {
            return markerOptions;
        }

        @Override
        public Maps.MarkerOptionsWrapper<MarkerOptions> icon(Bitmap bitmap) {
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
            return this;
        }

        @Override
        public Maps.MarkerOptionsWrapper<MarkerOptions> flat(boolean flat) {
            markerOptions.flat(flat);
            return this;
        }

        @Override
        public Maps.MarkerOptionsWrapper<MarkerOptions> position(double latitude, double longitude) {
            markerOptions.position(new LatLng(latitude, longitude));
            return this;
        }

        @Override
        public Maps.MarkerOptionsWrapper<MarkerOptions> anchor(float x, float y) {
            markerOptions.anchor(x, y);
            return this;
        }
    }

    public static class MarkerWrapper implements Maps.MarkerWrapper<Marker> {
        private final Marker marker;

        private MarkerWrapper(Marker marker) {
            this.marker = marker;
        }

        @Override
        public Marker getMarker() {
            return marker;
        }

        @Override
        public void setPosition(double latitude, double longitude) {
            marker.setPosition(new LatLng(latitude, longitude));
        }
    }
}
