package fi.kaupunkifillarit.marker;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import fi.kaupunkifillarit.R;
import fi.kaupunkifillarit.model.Rack;

public class RackMarkerOptions {
    private static Float markerTextSize = null;

    private final MarkerOptions status;

    public RackMarkerOptions(Rack rack, Resources res) {
        this.status = new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(RackMarkerOptions.getMarkerBitmap(rack.bikes < 2, rack.bikes + "/" + rack.slots, res)))
                .flat(false)
                .position(new LatLng(rack.latitude, rack.longitude))
                .anchor(0.5f, 1f);
    }

    public static void setUpMarkerText(float textSize) {
        if (markerTextSize == null) {
            markerTextSize = textSize;
        }
    }

    private static Bitmap getMarkerBitmap(boolean empty, String text, Resources res) {
        Typeface regular = Typeface.createFromAsset(res.getAssets(), "fonts/Montserrat-Regular.ttf");
        Paint paint = new Paint();
        paint.setTypeface(regular);
        paint.setTextSize(markerTextSize);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        Bitmap marker = BitmapFactory.decodeResource(res, empty ? R.drawable.rack_marker_empty : R.drawable.rack_marker);
        Bitmap bitmap = Bitmap.createBitmap(marker.getWidth(), marker.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(marker, 0, 0, null);
        canvas.drawText(text, (float) canvas.getWidth() / 2f, (float) canvas.getHeight() / 2f, paint);

        return bitmap;
    }

    public RackMarker makeMarker(GoogleMap googleMap) {
        return new RackMarker(googleMap.addMarker(status));
    }
}
