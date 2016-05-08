package fi.kaupunkifillarit;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.MapFragment;

public class CustomMapFragment extends MapFragment {

    private View originalView;
    private MapWrapperLayout mapWrapperLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        originalView = super.onCreateView(inflater, container, savedInstanceState);

        mapWrapperLayout = new MapWrapperLayout(getActivity());
        mapWrapperLayout.addView(originalView);

        return mapWrapperLayout;
    }

    @Override
    public View getView() {
        return originalView;
    }

    public void setOnTouchListener(View.OnTouchListener onTouchListener) {
        mapWrapperLayout.setOnTouchListener(onTouchListener);
    }

    private static class MapWrapperLayout extends FrameLayout {
        private OnTouchListener onTouchListener;

        public MapWrapperLayout(Context context) {
            super(context);
        }

        public MapWrapperLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public MapWrapperLayout(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public MapWrapperLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            if (onTouchListener != null) {
                onTouchListener.onTouch(this, ev);
            }
            return super.dispatchTouchEvent(ev);
        }

        @Override
        public void setOnTouchListener(OnTouchListener onTouchListener) {
            this.onTouchListener = onTouchListener;
        }
    }
}
