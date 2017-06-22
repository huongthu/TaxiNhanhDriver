package com.example.thu.utils;

/**
 * Created by thu on 6/12/2017.
 */

public class BookHistory {
    private String mPickUp = "";
    private String mDropOff = "";
    private String mDate = "";
    private String mDuringTime = "";
    private String mTaxiDriver = "";
    private String mDriverUrl = "";

    public BookHistory(String pickUp, String dropOff, String date,
                       String duringTime, String taxiDriver, String driverUrl) {
        mPickUp = pickUp;
        mDropOff = dropOff;
        mDate = date;
        mDuringTime = duringTime;
        mTaxiDriver = taxiDriver;
        mDriverUrl = driverUrl;
    }

    public BookHistory(String pickUp, String dropOff, String date,
                       String duringTime, String taxiDriver) {
        new BookHistory(pickUp, dropOff, date, duringTime, taxiDriver, "");
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
    public String getTaxiDriver() {
        return mTaxiDriver;
    }
    public String getDriverUrl() {
        return mDriverUrl;
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
    public void setTaxiDriver(String taxiDriver) {
        mTaxiDriver = taxiDriver;
    }
    public void setDriverUrl(String driverUrl) {
        mDriverUrl = driverUrl;
    }
}
