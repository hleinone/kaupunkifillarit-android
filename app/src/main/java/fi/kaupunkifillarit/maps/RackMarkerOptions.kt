package fi.kaupunkifillarit.maps

import android.content.Context
import android.graphics.*
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import fi.kaupunkifillarit.R
import fi.kaupunkifillarit.model.Rack

object RackMarkerBitmap {
    fun bitmap(isEmpty: Boolean, text: String, context: Context): Bitmap {
        val regular = ResourcesCompat.getFont(context, R.font.montserrat_regular)
        val paint = Paint()
        paint.typeface = regular
        paint.textSize =
            context.resources.getDimensionPixelSize(R.dimen.indicator_text_size).toFloat()
        paint.color = Color.BLACK
        paint.textAlign = Paint.Align.CENTER
        paint.isAntiAlias = true
        val marker = BitmapFactory.decodeResource(
            context.resources,
            if (isEmpty) R.drawable.rack_marker_empty else R.drawable.rack_marker
        )
        val bitmap = Bitmap.createBitmap(marker.width, marker.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(marker, 0f, 0f, null)
        canvas.drawText(text, canvas.width.toFloat() / 2f, canvas.height.toFloat() / 2f, paint)

        return bitmap
    }
}

fun rackMarker(rack: Rack, context: Context): MarkerOptions = MarkerOptions().apply {
    icon(
        BitmapDescriptorFactory.fromBitmap(
            RackMarkerBitmap.bitmap(
                rack.bikes < 2,
                "${rack.bikes}",
                context
            )
        )
    )
    flat(true)
    position(LatLng(rack.latitude, rack.longitude))
    anchor(0.5f, 1f)
}

