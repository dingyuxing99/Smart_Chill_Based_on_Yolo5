package com.example.finalpoject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.List;

public class IntroductoryActivity extends AppCompatActivity {
    ImageView logo, appName,splashImg;
    LottieAnimationView lottieAnimationView;

//    private static final int NUM_PAGES = 3; //页面数
//    private ViewPager viewPager; //视图翻页工具
//    private ScreenSlidePagerAdapter pagerAdapter; //页面适配器
    List<Integer> pics = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introductory);
        logo = findViewById(R.id.logo);
        appName = findViewById(R.id.app_name);
        splashImg = findViewById(R.id.img);
        lottieAnimationView = findViewById(R.id.lottie);

        splashImg.animate().translationY(-2200).setDuration(1000).setStartDelay(4000);
        appName.animate().translationY(1600).setDuration(1000).setStartDelay(4000);
        logo.animate().translationY(1600).setDuration(1000).setStartDelay(4000);
        lottieAnimationView.animate().translationY(1600).setDuration(1000).setStartDelay(4000);

//        viewPager = findViewById(R.id.viewpager); //找到视图翻页工具
//        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
//        viewPager.setAdapter(pagerAdapter); //适配器传过来
        pics.add(R.mipmap.bg1);
        pics.add(R.mipmap.bg2);
        pics.add(R.mipmap.bg3);

        //实例化适配器(RecyclerView.Adapter
        RecyclerView.Adapter adapter = new RecyclerView.Adapter() {
            //创建
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                //此时用的布局是ViewPager每一页中用来盛放图片的布局
                //该布局不设置任何控件，因为直接用图片作为根布局的背景
                View v = LayoutInflater.from(IntroductoryActivity.this).inflate(R.layout.item,parent,false);
                return new ViewHolder(v);
            }
            //绑定(ViewHolder里面的控件设置显示内容)
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                ViewHolder h = (ViewHolder) holder;
                h.container.setBackgroundResource(pics.get(position));
            }
            //数量
            @Override
            public int getItemCount() {
                return pics.size();
            }
        };
        //找到ViewPager,设置适配器
        ViewPager2 pagers = findViewById(R.id.viewpager);
        pagers.setAdapter(adapter);

    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        public LinearLayout container;

        public ViewHolder(View itemView){
            super(itemView);
            container = itemView.findViewById(R.id.container);
        }
    }

//    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter{
//
//        public ScreenSlidePagerAdapter(@NonNull FragmentManager fm) {
//            super(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
//        }
//
//        @NonNull
//        @Override
//        public Fragment getItem(int position) {
//            switch (position){
//                case 0:
//                    OnBoardingFragment1 tab1 = new OnBoardingFragment1();
//                    return tab1;
//                case 1:
//                    OnBoardingFragment2 tab2 = new OnBoardingFragment2();
//                    return tab2;
//                case 2:
//                    OnBoardingFragment3 tab3 = new OnBoardingFragment3();
//                    return tab3;
//            }
//            return null;
//        }
//
//        @Override
//        public int getCount() {
//            return NUM_PAGES;
//        }
//    }

//    private class ScreenSlidePagerAdapter extends FragmentStateAdapter{
//
//
//    public ScreenSlidePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
//        super(fragmentActivity);
//    }
//
//    @NonNull
//    @Override
//    public Fragment createFragment(int position) {
//        switch (position){
//            case 0:
//                OnBoardingFragment1 tab1 = new OnBoardingFragment1();
//                return tab1;
//            case 1:
//                OnBoardingFragment2 tab2 = new OnBoardingFragment2();
//                return tab2;
//            case 2:
//                OnBoardingFragment2 tab3 = new OnBoardingFragment2();
//                return tab3;
//            }
//            return null;
//    }
//
//    @Override
//    public int getItemCount() {
//        return NUM_PAGES;
//    }
//}
}