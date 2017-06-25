package com.example.thu.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by thu on 6/12/2017.
 */

public class BookHistory {
    private static String PICK_UP = "pickUp";
    private static String DROP_OFF = "dropOff";
    private static String DATE = "date";
    private static String TOTAL_TIME = "totalTime";
    private static String CUSTOMER_NAME = "customer";
    private static String CUSTOMER_UID = "uidCustomer";

    private String mPickUp = "";
    private String mDropOff = "";
    private String mDate = "";
    private String mDuringTime = "";
    private String mCustomerName = "";
    private String mCustomerUrl = "";
    private String mCustomerUid = "";

    public BookHistory(String pickUp, String dropOff, String date,
                       String duringTime, String customerName, String customerUrl) {
        mPickUp = pickUp;
        mDropOff = dropOff;
        mDate = date;
        mDuringTime = duringTime;
        mCustomerName = customerName;
        mCustomerUrl = customerUrl;
    }

    public BookHistory(String pickUp, String dropOff, String date,
                       String duringTime, String taxiDriver) {
        new BookHistory(pickUp, dropOff, date, duringTime, taxiDriver, "");
    }

    public BookHistory(JSONObject history) {
        try {
            mPickUp = history.getString(PICK_UP);
            mDropOff = history.getString(DROP_OFF);
            mDate = history.getString(DATE);
            mDuringTime = history.getString(TOTAL_TIME);
            mCustomerName = history.getString(CUSTOMER_NAME);
            mCustomerUid = history.getString(CUSTOMER_UID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getPickUp() {
        return mPickUp;
    }
    public String getDropOff() {
        return mDropOff;
    }
    public String getDate() {
        return mDate;
    }
    public String getDuringTime() {
        return mDuringTime;
    }
    public String getCustomerName() {
        return mCustomerName;
    }
    public String getCustomerUrl() {
        return mCustomerUrl;
    }

    public void setPickUp(String pickUp) {
        mPickUp = pickUp;
    }
    public void setDropOff(String dropOff) {
        mDropOff = dropOff;
    }
    public void setDate(String date) {
        mDate = date;
    }
    public void setDuringTime(String duringTime) {
        mDuringTime = duringTime;
    }
    public void setCustomerName(String customerName) {
        mCustomerName = customerName;
    }
    public void setCustomerUrl(String customerUrl) {
        mCustomerUrl = customerUrl;
    }
    public void setmCustomerUid(String customerUid) {mCustomerUid = customerUid;}
}
