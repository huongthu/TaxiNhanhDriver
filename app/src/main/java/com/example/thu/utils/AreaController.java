package com.example.thu.utils;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import com.google.maps.android.PolyUtil;

/**
 * Created by thu on 6/22/2017.
 */

public class AreaController {
    ArrayList<LatLng> mSwitchArea = null;
    ArrayList<LatLng> mWarningArea = null;
    ArrayList<LatLng> mQueuingArea = null;

    public AreaController() {
        ArrayList<LatLng> mSwitchArea = new ArrayList<LatLng>();
        ArrayList<LatLng> mWarningArea = new ArrayList<LatLng>();
        ArrayList<LatLng> mQueuingArea = new ArrayList<LatLng>();
    }

    public AreaController(ArrayList<LatLng> lstSwitchArea,
                          ArrayList<LatLng> lstWarningArea,
                          ArrayList<LatLng> lstQueuingArea) {
        updateQueuingInformation(lstSwitchArea, lstWarningArea, lstQueuingArea);
    }

    public void updateQueuingInformation(ArrayList<LatLng> lstSwitchArea,
                                         ArrayList<LatLng> lstWarningArea,
                                         ArrayList<LatLng> lstQueuingArea) {
        mSwitchArea = lstSwitchArea;
        mWarningArea = lstWarningArea;
        mQueuingArea = lstQueuingArea;
    }

    public boolean canJoinQueuingZone(LatLng point) {
        if (mWarningArea == null || mSwitchArea == null || mQueuingArea == null) {
            return false;
        }
        return PolyUtil.containsLocation(point, mWarningArea, true);
    }

    public boolean isOnSwitchArea (LatLng point)
    {
        if (mWarningArea == null || mSwitchArea == null || mQueuingArea == null) {
            return false;
        }
        return (PolyUtil.containsLocation(point, mSwitchArea, true) &&
                (!PolyUtil.containsLocation(point, mWarningArea, true)));
    }

    public boolean isOnWraningArea (LatLng point)
    {
        if (mWarningArea == null || mSwitchArea == null || mQueuingArea == null) {
            return false;
        }
        return (PolyUtil.containsLocation(point, mWarningArea, true) &&
                (!PolyUtil.containsLocation(point, mQueuingArea, true)));
    }

    public boolean isOnQueuingArea (LatLng point)
    {
        if (mWarningArea == null || mSwitchArea == null || mQueuingArea == null) {
            return false;
        }
        return PolyUtil.containsLocation(point, mQueuingArea, true);
    }
}
