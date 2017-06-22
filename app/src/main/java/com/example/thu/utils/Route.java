package com.example.thu.utils;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by NHATNGUYEN on 5/31/2017.
 */

public class Route {
    public Distance distance;
    public Duration duration;
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;

    public List<LatLng> points;



}