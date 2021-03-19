package fi.kaupunkifillarit.maps

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import fi.kaupunkifillarit.R
import fi.kaupunkifillarit.model.Rack

object RackMarkerBitmapDescriptorFactory {
    private var shape: Bitmap? = null

    private fun shape(context: Context): Bitmap {
        if (shape == null) {
            synchronized(this) {
                if (shape == null) {
                    val d =
                        VectorDrawableCompat.create(context.resources, R.drawable.ic_marker, null)!!
                    d.bounds = Rect(0, 0, d.intrinsicWidth, d.intrinsicHeight)
                    val b = Bitmap.createBitmap(
                        d.intrinsicWidth,
                        d.intrinsicHeight,
                        Bitmap.Config.ARGB_8888
                    )
                    val c = Canvas(b)
                    d.draw(c)
                    shape = b
                }
            }
        }

        return shape!!
    }

    fun fromRack(rack: Rack, context: Context): BitmapDescriptor {
        val isEmpty = rack.bikes < 2
        val text = "${rack.bikes}"

        val regular = ResourcesCompat.getFont(context, R.font.montserrat_regular)
        val paint = Paint()
        paint.typeface = regular
        paint.textSize =
            context.resources.getDimensionPixelSize(R.dimen.indicator_text_size).toFloat()
        paint.color = ContextCompat.getColor(
            context,
            if (isEmpty) R.color.rack_marker_empty_text else R.color.rack_marker_text
        )
        paint.textAlign = Paint.Align.CENTER
        paint.isAntiAlias = true

        val colorPaint = Paint().apply {
            colorFilter = PorterDuffColorFilter(
                ContextCompat.getColor(
                    context,
                    if (isEmpty) R.color.rack_marker_empty else R.color.rack_marker
                ), PorterDuff.Mode.SRC_IN
            )
        }
        val shape = shape(context)

        val bitmap = Bitmap.createBitmap(shape.width, shape.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(shape, 0f, 0f, colorPaint)
        canvas.drawText(text, canvas.width.toFloat() / 2f, canvas.height.toFloat() / 2f, paint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}

fun rackMarker(rack: Rack, context: Context): MarkerOptions = MarkerOptions().apply {
    icon(
        RackMarkerBitmapDescriptorFactory.fromRack(rack, context)
    )
    flat(true)
    position(LatLng(rack.latitude, rack.longitude))
    anchor(0.5f, 1f)
}

