package fi.kaupunkifillarit.maps;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import fi.kaupunkifillarit.R;
import fi.kaupunkifillarit.model.Rack;

public class RackMarkerOptions {
    private final Maps.MarkerOptionsWrapper status;

    public RackMarkerOptions(Rack rack, Resources res, Maps.MapWrapper map) {
        this.status = map.createMarkerOptions()
                .icon(RackMarkerOptions.getMarkerBitmap(
                        rack.bikes < 2,
                        rack.bikes + "/" + rack.slots,
                        res))
                .flat(false)
                .position(rack.latitude, rack.longitude)
                .anchor(0.5f, 1f);
    }

    private static Bitmap getMarkerBitmap(boolean empty, String text, Resources res) {
        Typeface regular = Typeface.createFromAsset(res.getAssets(), "fonts/Montserrat-Regular.ttf");
        Paint paint = new Paint();
        paint.setTypeface(regular);
        paint.setTextSize(res.getDimensionPixelSize(R.dimen.indicator_text_size));
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

    public RackMarker makeMarker(Maps.MapWrapper map) {
        return new RackMarker(map.addMarker(status));
    }
}
