package fi.kaupunkifillarit

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.gms.maps.MapFragment
import io.reactivex.Observable

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

    fun touching(): Observable<Boolean> {
        return Observable.create<Boolean> { observableEmitter ->
            setOnTouchListener(View.OnTouchListener { _, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> observableEmitter.onNext(false)
                    else -> observableEmitter.onNext(true)
                }
                false
            })
        }
    }

    fun setOnTouchListener(onTouchListener: View.OnTouchListener) {
        mapWrapperLayout?.setOnTouchListener(onTouchListener)
    }

    class MapWrapperLayout : FrameLayout {
        private var onTouchListener: OnTouchListener? = null

        constructor(context: Context) : super(context)
        constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
        constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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
