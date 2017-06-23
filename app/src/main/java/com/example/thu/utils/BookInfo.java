package com.example.thu.utils;

import com.example.thu.taxinhanhdriver.MainActivity;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by thu on 6/23/2017.
 */

//var data = {
//        distance: "2km",
//        destination: "Bến thành",
//        pickUpLocation: "Đại học Khoa học Tự nhiên",
//        customerName: "Nguyễn Hương Thu",
//        phone: "0967561458",
//        fee: 200000
//        }

public class BookInfo {
    final static String DISTANCE = "distance";
    final static String DESTINATION = "destination";
    final static String PICK_UP_LOCATION = "pickUpLocation";
    final static String CUSTOMER_NAME = "customerName";
    final static String PHONE = "phone";
    final static String FEE = "fee";
    final static String PICK_UP_LAT_LNG = "pkLatLng";
    final static String DES_LAT_LNG = "desLatLng";
    final static String LAT = "Lat";
    final static String LNG = "Lng";

    private String mDistance = "";
    private String mDestination = "";
    private String mPickUpLocation = "";
    private String mCustomerName = "";
    private String mPhone = "";
    private double mFee = 0;
    private LatLng mPKLatLng = new LatLng(0, 0);
    private LatLng mDesLatLng = new LatLng(0, 0);

    public String getDistance () { return mDistance; }
    public String getDestination () { return mDestination; }
    public String getPickUpLocation () { return mPickUpLocation; }
    public String getCustomerName () { return mCustomerName; }
    public String getPhone () { return mPhone; }
    public double getFee () { return mFee; }
    public LatLng getPKLatLng () {return mPKLatLng; }
    public LatLng getDesLatLng () {return mDesLatLng; }

    public BookInfo(JSONObject obj) {
        try {
            mDistance = obj.getString(DISTANCE);
            mDestination = obj.getString(DESTINATION);
            mPickUpLocation = obj.getString(PICK_UP_LOCATION);
            mCustomerName = obj.getString(CUSTOMER_NAME);
            mPhone = obj.getString(PHONE);
            mFee = obj.getDouble(FEE);
            mPKLatLng = parseLatLngJSON(obj.getJSONObject(PICK_UP_LAT_LNG));
            mDesLatLng = parseLatLngJSON(obj.getJSONObject(DES_LAT_LNG));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static LatLng parseLatLngJSON(JSONObject obj) {
        try {
            return new LatLng(obj.getDouble(LAT), obj.getDouble(LNG));
        } catch (JSONException e) {
            return new LatLng(0, 0);
        }
    }
}
