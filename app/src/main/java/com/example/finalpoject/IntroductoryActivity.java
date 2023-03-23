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

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.airbnb.lottie.LottieAnimationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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

        final Intent intent = new Intent(getApplicationContext(), SignupActivity.class); //你要转向的Activity
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                startActivity(intent); //执行
            }
        };
        timer.schedule(task, 1000 * 10); //10秒后


    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        public LinearLayout container;

        public ViewHolder(View itemView){
            super(itemView);
            container = itemView.findViewById(R.id.container);
        }
    }


}