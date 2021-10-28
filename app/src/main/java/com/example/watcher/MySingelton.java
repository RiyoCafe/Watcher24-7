package com.example.watcher;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class MySingelton {
    private static MySingelton mInstance;
    private RequestQueue requestQueue;
    private static Context context;

    private MySingelton(Context context){
        this.context=context;
        requestQueue=getRequestQueue();
    }

    public RequestQueue getRequestQueue(){
        if(requestQueue==null){
            requestQueue= Volley.newRequestQueue(context);
        }
        return requestQueue;
    }

    public static synchronized MySingelton getInstance(Context context){
        if(mInstance==null){
            mInstance=new MySingelton(context);
        }
        return mInstance;
    }

    public<T> void addToRequestQueue(Request request){
        requestQueue.add(request);
    }
}