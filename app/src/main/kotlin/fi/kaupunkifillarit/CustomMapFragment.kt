package fi.kaupunkifillarit

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.gms.maps.MapFragment

class CustomMapFragment : MapFragment() {
    private var originalView: View? = null
    private var mapWrapperLayout: MapWrapperLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        originalView = super.onCreateView(inflater, container, savedInstanceState)

        mapWrapperLayout = MapWrapperLayout(activity)
        mapWrapperLayout?.addView(originalView)

        return mapWrapperLayout
    }

    override fun getView(): View? {
        return originalView
    }

    fun setOnTouchListener(onTouchListener: View.OnTouchListener) {
        mapWrapperLayout?.setOnTouchListener(onTouchListener)
    }

    class MapWrapperLayout : FrameLayout {
        private var onTouchListener: OnTouchListener? = null

        constructor(context: Context) : super(context)
        constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
        constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
        constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

        override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
            onTouchListener?.onTouch(this, ev)
            return super.dispatchTouchEvent(ev)
        }

        override fun setOnTouchListener(onTouchListener: OnTouchListener) {
            this.onTouchListener = onTouchListener
        }
    }
}
