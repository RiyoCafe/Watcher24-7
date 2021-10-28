package com.example.watcher;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity {

    Animation toanim,bottomanim;
    private ProgressBar progressBar;
    ImageView imageView;
    TextView textView,textView2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        toanim= AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottomanim=AnimationUtils.loadAnimation(this,R.anim.bottom_animation);
        imageView=findViewById(R.id.imageView4);
        textView=findViewById(R.id.textView_welcome);
        textView2=findViewById(R.id.loading_percentage);
        progressBar=findViewById(R.id.bar);
        imageView.setAnimation(toanim);
        textView.setAnimation(bottomanim);

        progressBar.setMax(100);
        progressBar.setScaleY(1);
        ProgressbarAnimation anim=new ProgressbarAnimation(this,progressBar,textView2,0,100);
        anim.setDuration(4000);
        progressBar.setAnimation(anim);


    }
}