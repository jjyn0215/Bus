package com.example.test8;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;


import com.faltenreich.skeletonlayout.Skeleton;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.tbuonomo.viewpagerdotsindicator.SpringDotsIndicator;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Skeleton skeleton;
    private Skeleton skeleton2;
    private Skeleton skeleton3;
    private Snackbar snackbar;
    private SpringDotsIndicator dotsIndicator;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_REQUEST_CODE = 1000;

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();

    private ViewPager2 viewPager;
    private ScreenSlidePagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Either use an existing Skeletonlayout

        skeleton = findViewById(R.id.skeletonLayout);
        skeleton2 = findViewById(R.id.skeletonLayout2);
        skeleton3 = findViewById(R.id.skeletonLayout3);
        dotsIndicator = findViewById(R.id.spring_dots_indicator);
                // or create a new SkeletonLayout from a given View
//        skeleton = SkeletonLayoutUtils.createSkeleton(view);

        // or apply a new SkeletonLayout to a RecyclerView
//        skeleton = SkeletonLayoutUtils.applySkeleton(recyclerView, R.layout.list_item_recyclerview);

//         or apply a new SkeletonLayout to a ViewPager2
//        skeleton = SkeletonLayoutUtils.applySkeleton(viewPager, R.layout.activity_main);
//
        skeleton.showSkeleton();
        skeleton2.showSkeleton();
        skeleton3.showSkeleton();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        viewPager = findViewById(R.id.pager);

//        ArrayList<Fragment> fragments = new ArrayList<>();
//        fragments.add(Fragment1.newInstance(0));
//        fragments.add(Fragment1.newInstance(0));
//        fragments.add(Fragment1.newInstance(0));
//        fragments.add(Fragment1.newInstance(0));

//        ViewPager2Adapter viewPager2Adapter = new ViewPager2Adapter(this, fragments);
//        viewPager.setAdapter(viewPager2Adapter);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // 위치 정보를 성공적으로 가져온 경우
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                try {
                                    postRequest(latitude, longitude);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                showSnackBar("위치 사용 불가");
                            }
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                showSnackBar("설정에서 위치 권한을 허용해 주세요.");
            }
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
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseBody = response.body().string();
                    runOnUiThread(() -> {
                        try {
                            // JSON 파싱
                            JSONObject jsonObject = new JSONObject(responseBody);
                            List<String> timeList = new ArrayList<>();
                            List<String> desList = new ArrayList<>();

                            timeList.add(jsonObject.optString("천안캠퍼스행", "Unknown"));
                            timeList.add(jsonObject.optString("아산캠퍼스행", "Unknown"));
                            desList.add("천안캠퍼스행");
                            desList.add("아산캠퍼스행");

                            adapter = new ScreenSlidePagerAdapter(MainActivity.this, timeList, desList);
                            viewPager.setAdapter(adapter);
                            dotsIndicator.attachTo(viewPager);
                            viewPager.setVisibility(View.VISIBLE);
                            String stationValue = jsonObject.optString("station", "Unknown"); // station 키 값 추출

                            // TextView에 값 설정
                            TextView station = findViewById(R.id.stationInfoTextView);
                            station.setText(stationValue);
                            skeleton.showOriginal();
                            skeleton2.showOriginal();
                            skeleton3.showOriginal();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "JSON Parse Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    showSnackBar("오류 발생 : " + response.code());
                }
            }
        });
    }

    private void showSnackBar(String text){
        snackbar = Snackbar.make(findViewById(R.id.main), text, Snackbar.LENGTH_SHORT);
        snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE);
        runOnUiThread(snackbar::show);
    }
}

