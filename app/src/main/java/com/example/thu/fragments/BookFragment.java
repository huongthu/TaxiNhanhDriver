package com.example.thu.fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thu.taxinhanhdriver.R;
import com.example.thu.utils.AreaController;
import com.example.thu.utils.DirectionFinder;
import com.example.thu.utils.DirectionFinderListener;
import com.example.thu.utils.LatLngInterpolator;
import com.example.thu.utils.MarkerAnimation;
import com.example.thu.utils.Route;
import com.example.thu.utils.Utils;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by thu on 6/12/2017.
 * Guide at http://manishkpr.webheavens.com/android-navigation-drawer-example-using-fragments/
 */

public class BookFragment extends Fragment implements OnMapReadyCallback, DirectionFinderListener {
    private boolean isBookAvailable = false;
    int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    private GoogleMap mMap;
    MapView mMapView;

    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;

    private static final String[] LOCATION_PERMS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int LOCATION_REQUEST = 1340;

    public static Fragment newInstance(Context context) {
        BookFragment f = new BookFragment();
        return f;
    }

    ViewGroup root = null;
    Marker mMarker = null;
    LatLng currentLocation = null;

    private Socket mSocket;

    {
        try {
            mSocket = IO.socket("http://thesisk13.ddns.net:3002/");
        } catch (URISyntaxException e) {
        }
    }

    Socket mSocketControlCenter;

    {
        try {
            mSocketControlCenter = IO.socket("http://thesisk13.ddns.net:3003");
        } catch (URISyntaxException e) {
        }
    }

    AreaController areaController = new AreaController();

    Polygon currentPolygon = null;

    public LatLng pickUpLocation = new LatLng(10.7622739, 106.6822471);

    private class GetAddressSync extends AsyncTask<LatLng, Void, String> {

        @Override
        protected String doInBackground(LatLng... params) {
            return getAddress(params[0].latitude, params[0].longitude);
        }

        @Override
        protected void onPostExecute(String result) {
            ((LinearLayout) root.findViewById(R.id.llLoading)).setVisibility(View.GONE);

            TextView tvPickUp = (TextView) root.findViewById(R.id.tvPickUp);
            tvPickUp.setText(result);
        }

        @Override
        protected void onPreExecute() {
            ((LinearLayout) root.findViewById(R.id.llLoading)).setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    private boolean firstCofusGps = false;

    //https://stackoverflow.com/questions/13756261/how-to-get-the-current-location-in-google-maps-android-api-v2
    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(final Location location) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            currentLocation = loc;
            if (mMarker != null) {
                mMarker.remove();
            }

            mMarker = mMap.addMarker(new MarkerOptions().position(loc));
            if ((mMap != null) && (firstCofusGps == false)) {
                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15.0f));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15.0f));
                firstCofusGps = true;
            }

            LatLng b = new LatLng(location.getLatitude(), location.getLongitude());
            if (areaController.canJoinQueuingZone(b)) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(getActivity(), "Vào vùng ahihi", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    };

    private ArrayList<Marker> lstVehicles = new ArrayList<Marker>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.activity_book, null);

        final ImageButton btnBook = (ImageButton) root.findViewById(R.id.btnBook);
        btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBookAvailable) {
                    isBookAvailable = false;
                }

                btnBook.setImageResource(R.drawable.book_invisible);
            }
        });


        mMapView = (MapView) root.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                mSocketControlCenter.on("INITIAL_AREAS", QueuingListener);
                mSocketControlCenter.on("DRIVER_REQUESTION", CustomerRequest);
                mSocketControlCenter.connect();

                Button btnJoinQueue = (Button) getActivity().findViewById(R.id.btnJoin);

                final Socket finalMSocketControlCenter = mSocketControlCenter;
                btnJoinQueue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (currentLocation == null) {
                                return;
                            }
                            JSONObject data = new JSONObject();
                            data.put("_uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            data.put("lat", currentLocation.latitude);
                            data.put("lng", currentLocation.longitude);

                            finalMSocketControlCenter.emit(getResources().getString(R.string.DRIVER_JOIN), data);
                        } catch (JSONException e) {
                        }

                    }
                });

                ((Button) getActivity().findViewById(R.id.btn2)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Dialog dialog2 = new Dialog(getActivity(), android.R.style.Theme_DeviceDefault_Wallpaper_NoTitleBar);
                        dialog2.setContentView(R.layout.activity_customer_request);
                        dialog2.setTitle("Title...");
                        dialog2.show();
                    }
                });

                mMap = map;

                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(LOCATION_PERMS, LOCATION_REQUEST);
                    return;
                }
                mMap.setMyLocationEnabled(true);

                mMap.setOnMyLocationChangeListener(myLocationChangeListener);

                mSocket.on("INIT_VEHICELS", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        //JSONObject objVehicles = (JSONObject) args[0];
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "connected", Toast.LENGTH_SHORT);
                            }
                        });
                    }
                }).on("VEHICLE_UPDATE", VehicleUpdate);

                mSocket.connect();
            }
        });


        return root;
    }

    private Emitter.Listener CustomerRequest = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final JSONObject objData = (JSONObject) args[0];
                    try {
                        objData.put("isAccept", false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    final Dialog dialog2 = new Dialog(getActivity(), android.R.style.Theme_DeviceDefault_Wallpaper_NoTitleBar);
                    dialog2.setContentView(R.layout.activity_customer_request);
                    dialog2.show();

                    // Hide after some seconds
                    final Handler handler = new Handler();
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            if (dialog2.isShowing()) {
                                dialog2.dismiss();
                            }
                        }
                    };

                    try {
                        ((TextView) dialog2.findViewById(R.id.tvDistance)).setText(objData.getString("distance"));
                        ((TextView) dialog2.findViewById(R.id.tvPickUp)).setText(objData.getString("pickUpLocation"));
                        ((TextView) dialog2.findViewById(R.id.tvDropOff)).setText(objData.getString("destination"));
                        ((TextView) dialog2.findViewById(R.id.tvFee)).setText(String.format("%,.0f VNĐ", objData.getDouble("fee")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Button btnAgreeBooking = (Button) dialog2.findViewById(R.id.btnConfirm);
                    btnAgreeBooking.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                objData.put("isAccept", true);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            mSocketControlCenter.emit(getResources().getString(R.string.DRIVER_REQUESTION_RESULT), objData);

                            try {
                                showPickUpDirection(objData);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            dialog2.dismiss();
                        }
                    });

                    dialog2.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            handler.removeCallbacks(runnable);

                            try {
                                if (!objData.getBoolean("isAccept")) {
                                    //Toast.makeText(MainActivity.this, "Auto exit", Toast.LENGTH_SHORT).show();
                                    mSocketControlCenter.emit(getResources().getString(R.string.DRIVER_REQUESTION_RESULT), objData);

                                }
                            } catch (JSONException e) {
                            }
                        }
                    });

                    handler.postDelayed(runnable, 15000);
                }
            });
        }
    };

    private void showPickUpDirection(final JSONObject customerInfo) throws JSONException {
        getActivity().findViewById(R.id.llTitle).setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.llControl).setVisibility(View.VISIBLE);
        ((TextView) getActivity().findViewById(R.id.tvTitle)).setText("Đón khách");
        ((TextView) getActivity().findViewById(R.id.tvLocation)).setText(customerInfo.getString("pickUpLocation"));
        ((TextView) getActivity().findViewById(R.id.tvCustomerName)).setText(customerInfo.getString("customerName"));
        ((TextView) getActivity().findViewById(R.id.tvPrice)).setText(customerInfo.getString("fee"));

        getActivity().findViewById(R.id.btnCall).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    makeAPhoneCall(customerInfo.getString("phone"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        getActivity().findViewById(R.id.btnSMS).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    makeASMS(customerInfo.getString("phone"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void makeAPhoneCall(String mobilePhone) {
        final int REQUEST_PHONE_CALL = 1;
        Intent intent = new Intent(Intent.ACTION_CALL);

        intent.setData(Uri.parse("tel:" + mobilePhone));
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
            return;
        }
        getActivity().startActivity(intent);
    }

    private void makeASMS(String mobilePhone) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + mobilePhone)));
    }

    private  Emitter.Listener VehicleUpdate = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject objUpdate = (JSONObject) args[0];
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {

                        Marker vehicle = findVehicleMarker(objUpdate.getString("licensePlate"));
                        LatLng newLocation = new LatLng(objUpdate.getDouble("lat"),objUpdate.getDouble("lng"));
                        LatLng oldLocation = new LatLng(objUpdate.getDouble("latOld"),objUpdate.getDouble("lngOld"));

                        String licensePlate = objUpdate.getString("licensePlate");
                        if (null == vehicle) {
                            lstVehicles.add(mMap.addMarker(new MarkerOptions()
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                                    .title(licensePlate)
                                    .position(newLocation)));
                        } else {
                            Location prevLoc = new Location("");
                            prevLoc.setLatitude(oldLocation.latitude);
                            prevLoc.setLongitude(oldLocation.longitude);

                            Location nextLoc = new Location("");
                            nextLoc.setLatitude(newLocation.latitude);
                            nextLoc.setLongitude(newLocation.longitude);

                            float bearing = prevLoc.bearingTo(nextLoc) ;

                            vehicle.setRotation(bearing);
                            MarkerAnimation.animateMarkerToICS(vehicle, newLocation, new LatLngInterpolator.Spherical());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    };

    private Emitter.Listener QueuingListener = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Catch!!!", Toast.LENGTH_SHORT).show();
                    JSONObject areas = (JSONObject)args[0];

                    try {
                        ArrayList<LatLng> lstQueuingZone = Utils.parseJSONArea(areas.getJSONArray("queuingArea"));
                        ArrayList<LatLng> lstWarningZone = Utils.parseJSONArea(areas.getJSONArray("warningArea"));
                        ArrayList<LatLng> lstSwitchZone = Utils.parseJSONArea(areas.getJSONArray("switchArea"));

                        areaController.updateQueuingInformation(lstSwitchZone, lstWarningZone, lstQueuingZone);
                        PolygonOptions polygonOptions = new PolygonOptions();
                        polygonOptions.addAll(lstSwitchZone)
                                .addHole(lstWarningZone)
                                .fillColor(ContextCompat.getColor(getContext(), R.color.colorQueuing))
                                .strokeColor(Color.RED);

                        if (currentLocation != null) {
                            currentPolygon.remove();
                        }

                        currentPolygon =  mMap.addPolygon(polygonOptions);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private class LoadPlaceAutocomplete extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Intent intent =
                        new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                .build(getActivity());
                startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
            } catch (GooglePlayServicesRepairableException e) {
                // TODO: Handle the error.
            } catch (GooglePlayServicesNotAvailableException e) {
                // TODO: Handle the error.
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((LinearLayout)getActivity().findViewById(R.id.llLoading)).setVisibility(View.GONE);
                }
            });
        }

        @Override
        protected void onPreExecute() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((LinearLayout)getActivity().findViewById(R.id.llLoading)).setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        protected void onProgressUpdate(Void... values) { }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        TextView tvDropOff = (TextView) getActivity().findViewById(R.id.tvDropOff);

        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                tvDropOff.setText(place.getAddress());
                TextView tvPickUp = (TextView) getActivity().findViewById(R.id.tvPickUp);
                isBookAvailable = true;
                sendRequest(tvPickUp.getText().toString(), place.getAddress().toString());
                //isBookAvailable = false;

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                tvDropOff.setText(getResources().getText(R.string.please_choose_dropoff));
                tvDropOff.setTypeface(null, Typeface.ITALIC);
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                // Do nothing
            }
        }
    }

    private void sendRequest(String origin, String destination) {
        try {
            new DirectionFinder(this, origin, destination).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public String getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            String add = obj.getAddressLine(0) +  ", " + obj.getAddressLine(2) +  ", " + obj.getAddressLine(3);
            return add;
            // TennisAppActivity.showDialog(add);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            return "Không tìm thấy vị trí, bấm để thử lại";
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) { }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(getActivity(), "Loading...",
                "Đang lấy thông tin địa điểm..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }

        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));
    }

    private Marker findVehicleMarker (String licensePlate) {
        for (int i = 0; i < lstVehicles.size(); i++) {
            if (lstVehicles.get(i).getTitle().toLowerCase().equals(licensePlate.toLowerCase())) {
                return lstVehicles.get(i);
            }
        }
        return null;
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));

            //distance = route.distance.text;
            String[] km = route.distance.text.split(" ");

            double distance = Double.parseDouble(km[0]);
            updatePriceUI(distance);


            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.empty_flag_40))
                    .title(route.startAddress)
                    .position(route.startLocation)));

            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    //.icon(BitmapDescriptorFactory.fromResource(R.drawable.empty_flag_40))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(6);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(route.startLocation);
            builder.include(route.endLocation);
            LatLngBounds bounds = builder.build();

            //mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 15));
        }
    }

    public void updatePriceUI(double distance) {
        if (isBookAvailable) {
        }
    }
}
