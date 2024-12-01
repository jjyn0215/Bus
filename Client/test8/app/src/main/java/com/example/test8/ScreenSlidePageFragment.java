package com.example.test8;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.fragment.app.Fragment;

// ScreenSlidePageFragment : ViewPager2의 요소 Fragment
public class ScreenSlidePageFragment extends Fragment {

    // Factory 메서드로 데이터 전달
    public static ScreenSlidePageFragment newInstance(String time, String destination) {
        ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
        Bundle args = new Bundle();
        args.putString("arg_time", time);
        args.putString("arg_des", destination);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time, container, false);
        TextView timeTextView = view.findViewById(R.id.timeInfoTextView);
        TextView desTextView = view.findViewById(R.id.desTextView);
        if (getArguments() != null) {
            timeTextView.setText(getArguments().getString("arg_time", "No Data"));
            desTextView.setText(getArguments().getString("arg_des", "No Data"));
        }
        return view;
    }
}