package com.example.watcher.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.example.watcher.R;
import com.google.android.gms.maps.model.LatLng;


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
    // http://www.codecodex.com/wiki/Calculate_Distance_Between_Two_Points_on_a_Globe
    // https://stackoverflow.com/questions/8832071/how-can-i-get-the-distance-between-two-point-by-latlng
    public static double distance(LatLng from, LatLng to){
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(to.latitude-from.latitude);
        double lngDiff = Math.toRadians(to.longitude - from.longitude);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(from.latitude)) * Math.cos(Math.toRadians(to.latitude)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return distance * meterConversion;
    }
}
