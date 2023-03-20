package com.example.voice;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity { //implements View.OnClickListener {
    private Handler handler;
    TextView textView;
    ImageView imageView;
    Animation bounce;
    Animation up;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); // actionBar 숨기기
        setContentView(R.layout.splash_layout);
         textView =findViewById(R.id.textView6);
         imageView = findViewById(R.id.imageView4);
         bounce = AnimationUtils.loadAnimation(this,R.anim.up);
         up = AnimationUtils.loadAnimation(this,R.anim.realup);

         up.setAnimationListener(new Animation.AnimationListener() {
             @Override
             public void onAnimationStart(Animation animation) {

             }

             @Override
             public void onAnimationEnd(Animation animation) {

                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                overridePendingTransition(R.anim.top,R.anim.down);
                 finish();
             }

             @Override
             public void onAnimationRepeat(Animation animation) {

             }
         });
        textView.startAnimation(bounce);
        imageView.startAnimation(up);


       // handler = new Handler(Looper.getMainLooper());
    }

   /* @Override
    protected void onStart() {
        super.onStart();

        // Splash Screen 화면이 시작되고, 딜레이를 준 후에 run() 안의 코드를 동작한다.
        // 1s = 1000ms

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.startAnimation(bounce);

            }
        }, 300);	// 3.5초 뒤에 run() 안의 동작을 함
    }

    @Override
    public void onClick(View view) {
        // Splash Screen 화면이 클릭되면 바로 MainActivity 를 실행한다.
        beginMainActivity();
    }

    private void beginMainActivity() {
        // MainActivity 를 실행하는 intent 생성 및 호출
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }*/
}