package com.example.test8;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.faltenreich.skeletonlayout.Skeleton;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.shashank.sony.fancytoastlib.FancyToast;
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SummaryFragment extends Fragment {
    private static final int LOCATION_REQUEST_CODE = 1000;
    private Skeleton skeleton, skeleton2, skeleton3, skeleton4;
    private SpringDotsIndicator dotsIndicator;
    private FusedLocationProviderClient fusedLocationClient;
    private final OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private ViewPager2 viewPager;
    private ScreenSlidePagerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

        viewPager = view.findViewById(R.id.pager);
        dotsIndicator = view.findViewById(R.id.spring_dots_indicator);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());
        skeleton = view.findViewById(R.id.skeletonLayout);
        skeleton2 = view.findViewById(R.id.skeletonLayout2);
        skeleton3 = view.findViewById(R.id.skeletonLayout3);
        skeleton4 = view.findViewById(R.id.skeletonLayout4);
        skeleton.showSkeleton();
        skeleton2.showSkeleton();
        skeleton3.showSkeleton();
        skeleton4.showSkeleton();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // 권한 확인 및 요청
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            getCurrentLocation();
        }
        TextView privacyPolicy = view.findViewById(R.id.privacy_policy);
        privacyPolicy.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.hoseo.ac.kr/Home/Contents.mbz?action=MAPP_2302082206"));
            startActivity(intent);
        });
        TextView reload = view.findViewById(R.id.customer_service);
        reload.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            } else {
                getCurrentLocation();
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private static class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        private final List<String> timeList;
        private final List<String> desList;

        public ScreenSlidePagerAdapter(FragmentActivity fa, List<String> timeList, List<String> desList) {
            super(fa);
            this.timeList = timeList;
            this.desList = desList;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return ScreenSlidePageFragment.newInstance(timeList.get(position), desList.get(position));
        }

        @Override
        public int getItemCount() {
            return timeList.size();
        }
    }

    private void getCurrentLocation() {
        // 권한이 부여된 상태에서 위치 가져오기
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                try {
                                    postRequest(location.getLatitude(), location.getLongitude());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                showSnackBar("위치 사용 불가", FancyToast.ERROR);
                            }
                        }
                    });
        }
    }


    private void postRequest(double latitude, double longitude) throws IOException {
        RequestBody body = RequestBody.create(String.format("{ \"latitude\": %s, \"longitude\":%s }", latitude, longitude), JSON);
        Request request = new Request.Builder()
                .url("https://bus.salmakis.online")
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }


            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseBody = response.body().string();
                    requireActivity().runOnUiThread(() -> {
                        try {
                            // JSON 파싱
                            JSONObject jsonObject = new JSONObject(responseBody);
                            List<String> timeList = new ArrayList<>();
                            List<String> desList = new ArrayList<>();

                            timeList.add(jsonObject.optString("천안캠퍼스행", "Unknown"));
                            timeList.add(jsonObject.optString("아산캠퍼스행", "Unknown"));
                            desList.add("천안캠퍼스행");
                            desList.add("아산캠퍼스행");

                            adapter = new ScreenSlidePagerAdapter(requireActivity(), timeList, desList);
                            viewPager.setAdapter(adapter);
                            dotsIndicator.attachTo(viewPager);
                            viewPager.setVisibility(View.VISIBLE);
                            String stationValue = jsonObject.optString("station", "Unknown");
                            String distanceValue = jsonObject.optString("distance", "Unknown");// station 키 값 추출
                            String latitude = jsonObject.optString("latitude", "Unknown");
                            String longitude = jsonObject.optString("longitude", "Unknown");
                            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("Coords", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("latitude", latitude);
                            editor.putString("longitude", longitude);
                            editor.putString("station", stationValue);
                            editor.apply(); // 저장

                            // TextView에 값 설정
                            TextView station = requireView().findViewById(R.id.stationInfoTextView);
                            TextView distance = requireView().findViewById(R.id.distanceTextView);
                            distance.setText(distanceValue + " km");
                            station.setText(stationValue);
                            skeleton.showOriginal();
                            skeleton2.showOriginal();
                            skeleton3.showOriginal();
                            skeleton4.showOriginal();
                            showSnackBar("로드 성공", FancyToast.SUCCESS);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(requireContext(), "JSON Parse Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    showSnackBar("오류 발생 : " + response.code(), FancyToast.ERROR);
                }
            }
        });
    }


    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    getCurrentLocation();
                } else {
                    showSnackBar("위치 권한이 필요합니다.", FancyToast.ERROR);
                }
            });

    private void showSnackBar(String text, int type){
        requireActivity().runOnUiThread(() -> {
            FancyToast.makeText(requireContext(), text, FancyToast.LENGTH_SHORT, type,false).show();
        });
    }
}
