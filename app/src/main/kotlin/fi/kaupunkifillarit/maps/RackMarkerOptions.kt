package fi.kaupunkifillarit.maps

import android.content.res.Resources
import android.graphics.*
import fi.kaupunkifillarit.R
import fi.kaupunkifillarit.model.Rack

class RackMarkerOptions {
    private val status: Maps.MarkerOptionsWrapper<*>

    constructor(rack: Rack, res: Resources, map: Maps.MapWrapper<Maps.MarkerWrapper<*>, Maps.MarkerOptionsWrapper<*>>) {
        this.status = map.createMarkerOptions().icon(getMarkerBitmap(
                rack.bikes < 2,
                "${rack.bikes}/${rack.slots}",
                res)).flat(false).position(rack.latitude, rack.longitude).anchor(0.5f, 1f)
    }

    private fun getMarkerBitmap(empty: Boolean, text: String, res: Resources): Bitmap {
        val regular = Typeface.createFromAsset(res.assets, "fonts/Montserrat-Regular.ttf")
        val paint = Paint()
        paint.typeface = regular
        paint.textSize = res.getDimensionPixelSize(R.dimen.indicator_text_size).toFloat()
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        paint.isAntiAlias = true
        val marker = BitmapFactory.decodeResource(res, if (empty) R.drawable.rack_marker_empty else R.drawable.rack_marker)
        val bitmap = Bitmap.createBitmap(marker.width, marker.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(marker, 0f, 0f, null)
        canvas.drawText(text, canvas.width.toFloat() / 2f, canvas.height.toFloat() / 2f, paint)

        return bitmap
    }

    fun makeMarker(map: Maps.MapWrapper<Maps.MarkerWrapper<*>, Maps.MarkerOptionsWrapper<*>>): RackMarker {
        return RackMarker(map.addMarker(status))
    }
}