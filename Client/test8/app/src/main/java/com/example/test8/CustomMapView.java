package com.example.test8;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.naver.maps.map.MapView;

// CustonMapView : ViewPager2와 터치가 중복되어서 해결하기 위한 별도 클래스
public class CustomMapView extends MapView {
    public CustomMapView(Context context) {
        super(context);
    }

    public CustomMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 부모인 ViewPager2로 터치 이벤트가 전달되지 않도록 설정
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }
}