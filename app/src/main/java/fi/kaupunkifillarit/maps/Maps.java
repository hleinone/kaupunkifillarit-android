package fi.kaupunkifillarit.maps;

import android.graphics.Bitmap;

import fi.kaupunkifillarit.model.MapLocation;

public abstract class Maps {
    public interface MapWrapper<MW extends Maps.MarkerWrapper, MOW extends Maps.MarkerOptionsWrapper> {
        void animateToMapLocation(MapLocation mapLocation);

        void setMyLocationEnabled(boolean enabled);

        void setTrafficEnabled(boolean enabled);

        void setPadding(int left, int top, int right, int bottom);

        void setOnMarkerClickListener(OnMarkerClickListener listener);

        void setOnMapLocationChangeListener(OnMapLocationChangeListener listener);

        void setOnMyLocationButtonClickListener(OnMyLocationButtonClickListener listener);

        MOW createMarkerOptions();

        MW addMarker(MOW markerOptions);
    }

    public interface MarkerWrapper<T> {
        T getMarker();

        void setPosition(double latitude, double longitude);
    }

    public interface MarkerOptionsWrapper<T> {
        T getMarkerOptions();

        MarkerOptionsWrapper<T> icon(Bitmap bitmap);

        MarkerOptionsWrapper<T> flat(boolean flat);

        MarkerOptionsWrapper<T> position(double latitude, double longitude);

        MarkerOptionsWrapper<T> anchor(float x, float y);
    }

    public interface OnMarkerClickListener {
        boolean onMarkerClick(MarkerWrapper marker);
    }

    public interface OnMapLocationChangeListener {
        void onMapLocationChange(MapLocation mapLocation);
    }

    public interface OnMyLocationButtonClickListener {
        boolean onMyLocationButtonClick();
    }
}
