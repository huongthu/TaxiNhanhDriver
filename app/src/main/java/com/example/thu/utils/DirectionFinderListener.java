package com.example.thu.utils;

import java.util.List;

/**
 * Created by NHATNGUYEN on 5/31/2017.
 */

public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}

