package it.mahd.taxi.activity;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import it.mahd.taxi.Main;
import it.mahd.taxi.R;
import it.mahd.taxi.util.Controllers;

/**
 * Created by salem on 3/2/16.
 */
public class BookAdvancePosition extends Fragment {
    Controllers conf = new Controllers();
    //GPSTracker gps;
    MapView mMapView;
    private GoogleMap googleMap;
    Service service;
    protected LocationManager locationManager;// Declaring a Location Manager
    Location location; // location
    private CameraPosition cameraPosition;
    private double latitude, longitude;
    private Boolean isStart = false;
    boolean isGPSEnabled = false;// flag for GPS status
    boolean isNetworkEnabled = false;// flag for network status
    boolean canGetLocation = false;// flag for GPS status
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 3;// The minimum distance to change Updates in meters // 3 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 3 * 1;// The minimum time between updates in milliseconds // 3 seconds


    public BookAdvancePosition() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.booknow, container, false);

        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        googleMap = mMapView.getMap();
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        getLocation();
        if(canGetLocation()){
            latitude = getLatitude();
            longitude = getLongitude();
        }else{
            latitude = 0;
            longitude = 0;
        }
        MarkerOptions x = new MarkerOptions().position(new LatLng(latitude, longitude)).title("Hello Maps").snippet("First Marker");
        x.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        googleMap.addMarker(x);
        cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(10).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker arg0) {
                arg0.remove();
                Toast.makeText(getActivity()
                        , "Remove Marker " + String.valueOf(arg0.getId())
                        , Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        googleMap.setOnMapClickListener(new OnMapClickListener() {
            public void onMapClick(LatLng arg0) {
                googleMap.clear();
                latitude = arg0.latitude;
                longitude = arg0.longitude;
                googleMap.addMarker(new MarkerOptions().position(arg0)
                        .title("Hello Maps").snippet("First Marker")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
        });
        return v;
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) getActivity().getSystemService(service.LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);// getting GPS status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);// getting network status

            if (!isGPSEnabled) {// no GPS provider is enabled
                showSettingsAlert();
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {// First get location from Network Provider
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                if (isGPSEnabled) {// if GPS Enabled get lat/long using GPS Services
                    if (location == null) {
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    public void stopUsingGPS(){//Stop using GPS listener & Calling this function will stop using GPS in your app
        if(locationManager != null){
            locationManager = null;
        }
    }

    public double getLatitude(){//Function to get latitude
        if(location != null){
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public double getLongitude(){//Function to get longitude
        if(location != null){
            longitude = location.getLongitude();
        }
        return longitude;
    }
    //Function to check GPS/wifi enabled
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    public void showSettingsAlert(){//Function to show settings alert dialog & On pressing Settings button will lauch Settings Options
        final AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
        final String action = android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String title = "GPS is settings";// Setting Dialog Title
        final String message = "GPS is not enabled. Do you want open GPS setting?";// Setting Dialog Message
        builder.setTitle(title).setMessage(message)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {// On pressing Settings button
                            public void onClick(DialogInterface d, int id) {
                                getActivity().startActivity(new Intent(action));
                                d.dismiss();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {// on pressing cancel button
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                            }
                        });
        builder.create().show();// Showing Alert Message
    }

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
        stopUsingGPS();
        Fragment fr = new BookAdvance();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Bundle args = new Bundle();
        args.putDouble(conf.tag_latitude, latitude);
        args.putDouble(conf.tag_longitude, longitude);
        fr.setArguments(args);
        ft.replace(R.id.container_body, fr);
        ft.addToBackStack(null);
        ft.commit();
        ((Main) getActivity()).getSupportActionBar().setTitle(getString(R.string.advance));
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    /*@Override
    public void onMarkerDrag(Marker arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMarkerDragEnd(Marker arg0) {
        // TODO Auto-generated method stub
        LatLng dragPosition = arg0.getPosition();
        double dragLat = dragPosition.latitude;
        double dragLong = dragPosition.longitude;
        //Log.i("info", "on drag end :" + dragLat + " dragLong :" + dragLong);
        Toast.makeText(getActivity(), "Marker Dragged..!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMarkerDragStart(Marker arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMapClick(LatLng arg0) {
        // TODO Auto-generated method stub
        googleMap.clear();
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(arg0));
        MarkerOptions x = new MarkerOptions().position(new LatLng(arg0.latitude, arg0.longitude)).title("Hello Maps").snippet("First Marker");
        x.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));
        googleMap.addMarker(x);
    }


    @Override
    public void onMapLongClick(LatLng arg0) {
        // TODO Auto-generated method stub

        //create new marker when user long clicks
        googleMap.addMarker(new MarkerOptions()
                .position(arg0)
                .draggable(true));
    }*/
}
