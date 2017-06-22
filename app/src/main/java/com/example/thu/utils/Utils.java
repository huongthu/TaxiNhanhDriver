package com.example.thu.utils;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by thu on 6/13/2017.
 */

public class Utils {
    public static boolean isNullOrEmpty(String... strs) {
        for (String str : strs) {
            if (null == strs || str.equals("")) {
                return true;
            }
        }
        return false;
    }

    //https://stackoverflow.com/questions/2075836/read-contents-of-a-url-in-android
    public static String getContentFromUrl (String urlStr) {
        String ret = "";
        URL url = null;
        BufferedReader in = null;
        String inputLine;
        try {
            url = new URL(urlStr);
            in = new BufferedReader(
                    new InputStreamReader(
                            url.openStream()));
            while ((inputLine = in.readLine()) != null) {
                ret += inputLine;
            }
            in.close();
        } catch ( IOException ioe) {
            ret = "";
        }
        return ret;
    }

    public static boolean isPointInPolygon(LatLng tap, ArrayList<LatLng> vertices) {
        int intersectCount = 0;
        for (int j = 0; j < vertices.size() - 1; j++) {
            if (rayCastIntersect(tap, vertices.get(j), vertices.get(j + 1))) {
                intersectCount++;
            }
        }

        return ((intersectCount % 2) == 1); // odd = inside, even = outside;
    }

    //https://stackoverflow.com/questions/26014312/identify-if-point-is-in-the-polygon
    private static boolean rayCastIntersect(LatLng tap, LatLng vertA, LatLng vertB) {

        double aY = vertA.latitude;
        double bY = vertB.latitude;
        double aX = vertA.longitude;
        double bX = vertB.longitude;
        double pY = tap.latitude;
        double pX = tap.longitude;

        if ((aY > pY && bY > pY) || (aY < pY && bY < pY)
                || (aX < pX && bX < pX)) {
            return false; // a and b can't both be above or below pt.y, and a or
            // b must be east of pt.x
        }

        double m = (aY - bY) / (aX - bX); // Rise over run
        double bee = (-aX) * m + aY; // y = mx + b
        double x = (pY - bee) / m; // algebra is neat!

        return x > pX;
    }

    public static double cos(double a1, double b1, double a2, double b2) {
        return (a1*b1 + a2*b2) / ((Math.sqrt(a1*a1 + a2*a2)) * (Math.sqrt(b1*b1 + b2*b2)));
    }

    public static ArrayList<LatLng> parseJSONArea(JSONArray array) {
        ArrayList<LatLng> ret = new ArrayList<LatLng>();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject e = (JSONObject)array.get(i);
                LatLng tmp = new LatLng(e.getDouble("lat"), e.getDouble("lng"));
                ret.add(tmp);
            } catch (JSONException e1) {
                return ret;
            }

        }
        return ret;
    }
}
