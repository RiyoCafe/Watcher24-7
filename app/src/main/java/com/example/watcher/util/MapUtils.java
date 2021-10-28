package com.example.watcher.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.example.watcher.R;


public class MapUtils {
    public static Bitmap getOriginDestinationMarkerBitmap(){
        int  height = 20;
        int width = 20;
        Bitmap bitmap = Bitmap.createBitmap(height, width, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor( Color.BLACK );
        paint.setStyle( Paint.Style.FILL );
        paint.setAntiAlias(true);
        canvas.drawRect(0F, 0F, width, height, paint);
        return bitmap;
    }

    public static Bitmap getSatelliteBitmap(Context context){
        Bitmap bitmap = BitmapFactory.decodeResource( context.getResources(), R.drawable.satellite_one);
        return Bitmap.createScaledBitmap(bitmap, 150 , 150, false);
    }
}
