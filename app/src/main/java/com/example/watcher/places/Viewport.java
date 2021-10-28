package com.example.watcher.places;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Viewport {
    @SerializedName("northeast")
    @Expose
    private Location northeast;
    @SerializedName("southwest")
    @Expose
    private Location southwest;

    public Location getNortheast() {
        return northeast;
    }

    public Location getSouthwest() {
        return southwest;
    }

    public void setNortheast(Location northeast) {
        this.northeast = northeast;
    }

    public void setSouthwest(Location southwest) {
        this.southwest = southwest;
    }
}
