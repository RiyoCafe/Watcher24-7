package com.example.watcher;

import android.content.Context;
import android.content.Intent;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProgressbarAnimation extends Animation {

    private Context context;
    private ProgressBar progressBar;
    private TextView textView;
    private float from,to;

    public ProgressbarAnimation(Context context, ProgressBar progressBar,TextView textView, int from, int to) {
        this.context = context;
        this.progressBar = progressBar;
        this.textView = textView;
        this.from = from;
        this.to = to;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        float value=from+(to-from)*interpolatedTime;
        int v=(int)value;
        progressBar.setProgress(v);
        textView.setText(v+" %");
        if(v==to){
            context.startActivity(new Intent(context,MainActivity.class));

        }

    }
}