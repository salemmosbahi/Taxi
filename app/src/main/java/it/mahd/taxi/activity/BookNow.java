package it.mahd.taxi.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import it.mahd.taxi.Main;
import it.mahd.taxi.R;
import it.mahd.taxi.database.ServiceAdapterList;
import it.mahd.taxi.database.ServicesDB;
import it.mahd.taxi.database.Taxi;
import it.mahd.taxi.database.TaxiPosition;
import it.mahd.taxi.util.Calculator;
import it.mahd.taxi.util.Controllers;
import it.mahd.taxi.util.DirectionMap;
import it.mahd.taxi.util.Encrypt;
import it.mahd.taxi.util.ServerRequest;
import it.mahd.taxi.util.SocketIO;

/**
 * Created by salem on 2/13/16.
 */
public class BookNow extends Fragment implements LocationListener {
    SharedPreferences pref;
    Controllers conf = new Controllers();
    ServerRequest sr = new ServerRequest();
    Socket socket = SocketIO.getInstance();
    ArrayList<TaxiPosition> listTaxi = new ArrayList<>();
    ArrayList<TaxiPosition> driverTaxi = new ArrayList<>();
    private ArrayList<Taxi> listPreTaxi = new ArrayList<>();
    private ServiceAdapterList adapter;

    MapView mMapView;
    Service service;
    private static Dialog bookDialog, validDialog;
    private GoogleMap googleMap;
    protected LocationManager locationManager;// Declaring a Location Manager
    Location location; // location
    private CameraPosition cameraPosition;
    private CameraUpdate cameraUpdate;
    private double latitude, longitude;

    boolean isGPSEnabled = false;// flag for GPS status
    boolean isNetworkEnabled = false;// flag for network status
    boolean canGetLocation = false;// flag for GPS status
    //private Boolean isStart = false;
    private Boolean ioPreBook = false;
    private Boolean ioValid = false;
    private Boolean ioPostBook = false;
    private Boolean ioEndCourse = false;
    private String tokenOfDriver;
    private String usernameOfDriver;
    private String idBook;
    //private LatLng ptOfDriver;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 3;// The minimum distance to change Updates in meters // 3 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 3 * 1;// The minimum time between updates in milliseconds // 3 seconds
    private TextView DistanceDuration_txt;
    private FloatingActionButton Valid_btn;

    public BookNow() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.booknow, container, false);
        pref = getActivity().getSharedPreferences(conf.app, Context.MODE_PRIVATE);
        socket.connect();
        ioPreBook = true;
        ioValid = false; ioPostBook = false; ioEndCourse = false;
        tokenOfDriver = "";
        socket.on(conf.io_searchTaxi, handleIncomingPreBook);//listen in taxi driver
        socket.on(conf.io_validBook, handleIncomingValidBook);//listen in driver valid book
        socket.on(conf.io_postBook, handleIncomingPostBook);
        socket.on(conf.io_drawRoute, handleIncomingDrawRoute);
        socket.on(conf.io_endCourse, handleIncomingEndCourse);

        DistanceDuration_txt = (TextView) v.findViewById(R.id.distance_time_txt);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }

        googleMap = mMapView.getMap();
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        getLocation();
        if(canGetLocation()){
            latitude = getLatitude();
            longitude = getLongitude();
        }else{
            showSettingsAlert();
            latitude = 0;
            longitude = 0;
        }
        cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(15).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        Valid_btn = (FloatingActionButton) v.findViewById(R.id.valid_btn);
        Valid_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject jsonx = new JSONObject();
                try{
                    jsonx.put(conf.tag_validRoute, true);
                    jsonx.put(conf.tag_tokenClient, pref.getString(conf.tag_token, ""));
                    socket.emit(conf.io_validRoute, jsonx);
                    ioEndCourse = true;
                    Valid_btn.setVisibility(View.GONE);
                }catch(JSONException e){ }
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            public boolean onMarkerClick(Marker arg0) {
                tokenOfDriver = arg0.getTitle();
                //ptOfDriver = arg0.getPosition();
                bookDialog = new Dialog(getActivity(), R.style.FullHeightDialog);
                bookDialog.setContentView(R.layout.booknow_dialog);
                bookDialog.setCancelable(true);
                ImageView Picture_iv, Color_iv;
                TextView Username_txt, Age_txt, Model_txt, Serial_txt, Places_txt, Luggages_txt;
                RatingBar Point_rb;
                Button Book_btn, Cancel_btn;
                Picture_iv = (ImageView) bookDialog.findViewById(R.id.picture_iv);
                Username_txt = (TextView) bookDialog.findViewById(R.id.username_txt);
                Age_txt = (TextView) bookDialog.findViewById(R.id.age_txt);
                Point_rb = (RatingBar) bookDialog.findViewById(R.id.pt_rb);
                LayerDrawable stars = (LayerDrawable) Point_rb.getProgressDrawable();
                Color_iv = (ImageView) bookDialog.findViewById(R.id.color_iv);
                Model_txt = (TextView) bookDialog.findViewById(R.id.model_txt);
                Serial_txt = (TextView) bookDialog.findViewById(R.id.serial_txt);
                Places_txt = (TextView) bookDialog.findViewById(R.id.places_txt);
                Luggages_txt = (TextView) bookDialog.findViewById(R.id.luggages_txt);
                Book_btn = (Button) bookDialog.findViewById(R.id.book_btn);
                Cancel_btn = (Button) bookDialog.findViewById(R.id.cancel_btn);

                if (listPreTaxi.isEmpty()) {
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair(conf.tag_token, tokenOfDriver));
                    JSONObject json = sr.getJSON(conf.url_getDriver, params);
                    if(json != null){
                        try{
                            if(json.getBoolean(conf.res)) {
                                Encrypt algo = new Encrypt();
                                int keyVirtual = Integer.parseInt(json.getString(conf.tag_key));
                                String newKey = algo.key(keyVirtual);

                                String picture = json.getString(conf.tag_picture);
                                String color = algo.enc2dec(json.getString(conf.tag_color), newKey);
                                usernameOfDriver = algo.enc2dec(json.getString(conf.tag_fname), newKey)
                                        + " " + algo.enc2dec(json.getString(conf.tag_lname), newKey);
                                int[] tab = new Calculator().getAge(algo.enc2dec(json.getString(conf.tag_dateN), newKey));
                                String dateN = tab[0] + "years, " + tab[1] + "month, " + tab[2] + "day";
                                Float pt = (5 * Float.parseFloat(algo.enc2dec(json.getString(conf.tag_pt), newKey))) / Float.parseFloat(algo.enc2dec(json.getString(conf.tag_ptt), newKey));
                                String model = algo.enc2dec(json.getString(conf.tag_model), newKey);
                                String serial = algo.enc2dec(json.getString(conf.tag_serial), newKey);
                                String places = algo.enc2dec(json.getString(conf.tag_places), newKey) + " Places,";
                                String luggages = algo.enc2dec(json.getString(conf.tag_luggages), newKey) + " Kg Luggages";
                                Taxi tx = new Taxi(tokenOfDriver,picture,color,usernameOfDriver,dateN,pt,model,serial,places,luggages);
                                listPreTaxi.add(tx);

                                byte[] imageAsBytes = Base64.decode(picture.getBytes(), Base64.DEFAULT);
                                Picture_iv.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
                                Username_txt.setTextColor(Color.parseColor(color));
                                Username_txt.setText(usernameOfDriver);
                                Age_txt.setTextColor(Color.parseColor(color));
                                Age_txt.setText(dateN);
                                stars.getDrawable(2).setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_ATOP);
                                Point_rb.setRating(pt);
                                Color_iv.setBackgroundColor(Color.parseColor(color));
                                Model_txt.setTextColor(Color.parseColor(color));
                                Model_txt.setText(model);
                                Serial_txt.setTextColor(Color.parseColor(color));
                                Serial_txt.setText(serial);
                                Places_txt.setTextColor(Color.parseColor(color));
                                Places_txt.setText(places);
                                Luggages_txt.setTextColor(Color.parseColor(color));
                                Luggages_txt.setText(luggages);
                            }
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getActivity(), R.string.serverunvalid, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    boolean tokenDriverExist = false;
                    int positionDriver = 0;
                    for (int i = 0; i<listPreTaxi.size(); i++) {
                        if (tokenOfDriver.equals(listPreTaxi.get(i).getToken())) {
                            tokenDriverExist = true;
                            positionDriver = i;
                            break;
                        } else {
                            tokenDriverExist = false;
                        }
                    }
                    if (tokenDriverExist) {
                        byte[] imageAsBytes = Base64.decode(listPreTaxi.get(positionDriver).getPicture().getBytes(), Base64.DEFAULT);
                        Picture_iv.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
                        Username_txt.setTextColor(Color.parseColor(listPreTaxi.get(positionDriver).getColor()));
                        Username_txt.setText(listPreTaxi.get(positionDriver).getUsername());
                        Age_txt.setTextColor(Color.parseColor(listPreTaxi.get(positionDriver).getColor()));
                        Age_txt.setText(listPreTaxi.get(positionDriver).getDateN());
                        stars.getDrawable(2).setColorFilter(Color.parseColor(listPreTaxi.get(positionDriver).getColor()), PorterDuff.Mode.SRC_ATOP);
                        Point_rb.setRating(listPreTaxi.get(positionDriver).getPt());
                        Color_iv.setBackgroundColor(Color.parseColor(listPreTaxi.get(positionDriver).getColor()));
                        Model_txt.setTextColor(Color.parseColor(listPreTaxi.get(positionDriver).getColor()));
                        Model_txt.setText(listPreTaxi.get(positionDriver).getModel());
                        Serial_txt.setTextColor(Color.parseColor(listPreTaxi.get(positionDriver).getColor()));
                        Serial_txt.setText(listPreTaxi.get(positionDriver).getSerial());
                        Places_txt.setTextColor(Color.parseColor(listPreTaxi.get(positionDriver).getColor()));
                        Places_txt.setText(listPreTaxi.get(positionDriver).getPlaces());
                        Luggages_txt.setTextColor(Color.parseColor(listPreTaxi.get(positionDriver).getColor()));
                        Luggages_txt.setText(listPreTaxi.get(positionDriver).getLuggages());
                    } else {
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair(conf.tag_token, tokenOfDriver));
                        JSONObject json = sr.getJSON(conf.url_getDriver, params);
                        if(json != null){
                            try{
                                if(json.getBoolean(conf.res)) {
                                    Encrypt algo = new Encrypt();
                                    int keyVirtual = Integer.parseInt(json.getString(conf.tag_key));
                                    String newKey = algo.key(keyVirtual);

                                    String picture = json.getString(conf.tag_picture);
                                    String color = algo.enc2dec(json.getString(conf.tag_color), newKey);
                                    usernameOfDriver = algo.enc2dec(json.getString(conf.tag_fname), newKey)
                                            + " " + algo.enc2dec(json.getString(conf.tag_lname), newKey);
                                    int[] tab = new Calculator().getAge(algo.enc2dec(json.getString(conf.tag_dateN), newKey));
                                    String dateN = tab[0] + "years, " + tab[1] + "month, " + tab[2] + "day";
                                    Float pt = (5 * Float.parseFloat(algo.enc2dec(json.getString(conf.tag_pt), newKey))) / Float.parseFloat(algo.enc2dec(json.getString(conf.tag_ptt), newKey));
                                    String model = algo.enc2dec(json.getString(conf.tag_model), newKey);
                                    String serial = algo.enc2dec(json.getString(conf.tag_serial), newKey);
                                    String places = algo.enc2dec(json.getString(conf.tag_places), newKey) + " Places,";
                                    String luggages = algo.enc2dec(json.getString(conf.tag_luggages), newKey) + " Kg Luggages";
                                    Taxi tx = new Taxi(tokenOfDriver,picture,color,usernameOfDriver,dateN,pt,model,serial,places,luggages);
                                    listPreTaxi.add(tx);

                                    byte[] imageAsBytes = Base64.decode(picture.getBytes(), Base64.DEFAULT);
                                    Picture_iv.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
                                    Username_txt.setTextColor(Color.parseColor(color));
                                    Username_txt.setText(usernameOfDriver);
                                    Age_txt.setTextColor(Color.parseColor(color));
                                    Age_txt.setText(dateN);
                                    stars.getDrawable(2).setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_ATOP);
                                    Point_rb.setRating(pt);
                                    Color_iv.setBackgroundColor(Color.parseColor(color));
                                    Model_txt.setTextColor(Color.parseColor(color));
                                    Model_txt.setText(model);
                                    Serial_txt.setTextColor(Color.parseColor(color));
                                    Serial_txt.setText(serial);
                                    Places_txt.setTextColor(Color.parseColor(color));
                                    Places_txt.setText(places);
                                    Luggages_txt.setTextColor(Color.parseColor(color));
                                    Luggages_txt.setText(luggages);
                                }
                            }catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(getActivity(), R.string.serverunvalid, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                bookDialog.show();
                Cancel_btn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        bookDialog.dismiss();
                    }
                });
                Book_btn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        ioValid = true;
                        ioPreBook = false;
                        ioPostBook = false;
                        JSONObject jsonx = new JSONObject();
                        try {
                            jsonx.put(conf.tag_latitude, latitude);
                            jsonx.put(conf.tag_longitude, longitude);
                            jsonx.put(conf.tag_tokenDriver, tokenOfDriver);
                            jsonx.put(conf.tag_tokenClient, pref.getString(conf.tag_token, ""));
                            jsonx.put(conf.tag_fname, pref.getString(conf.tag_fname, "") + " " + pref.getString(conf.tag_lname, ""));
                            socket.emit(conf.io_preBook, jsonx);
                        } catch (JSONException e) {
                        }
                        bookDialog.dismiss();
                        googleMap.clear();
                    }
                });
                return true;
            }
        });
        return v;
    }

    private Emitter.Listener handleIncomingEndCourse = new Emitter.Listener(){
        public void call(final Object... args){
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (ioEndCourse) {
                        ioPreBook = false; ioValid = false; ioPostBook = false;
                        JSONObject data = (JSONObject) args[0];
                        Double pcourse, ptake, preturn;
                        String token;
                        try {
                            idBook = data.getString(conf.tag_id);
                            pcourse = data.getDouble(conf.tag_pcourse);
                            ptake = data.getDouble(conf.tag_ptake);
                            preturn = data.getDouble(conf.tag_preturn);
                            token = data.getString(conf.tag_token);
                            if (token.equals(tokenOfDriver)) {
                                googleMap.clear();
                                //Toast.makeText(getActivity(),"x:" + pcourse + " " + ptake,Toast.LENGTH_LONG).show();
                                validDialog = new Dialog(getActivity(), R.style.FullHeightDialog);
                                validDialog.setContentView(R.layout.booknow_dialog_valid);
                                validDialog.setCancelable(true);
                                TextView PriceCourse_txt, PriceTake_txt, PriceReturn_txt;
                                ListView Services_lv;
                                Button Valid_btn, Cancel_btn;
                                PriceCourse_txt = (TextView) validDialog.findViewById(R.id.priceCourse_txt);
                                PriceCourse_txt.setText("pcourse: " + pcourse);
                                PriceTake_txt = (TextView) validDialog.findViewById(R.id.priceTake_txt);
                                PriceTake_txt.setText("ptake: " + ptake);
                                PriceReturn_txt = (TextView) validDialog.findViewById(R.id.priceReturn_txt);
                                PriceReturn_txt.setText("preturn: " + preturn);
                                Services_lv = (ListView) validDialog.findViewById(R.id.services_lv);
                                Cancel_btn = (Button) validDialog.findViewById(R.id.cancel_btn);
                                Cancel_btn.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        googleMap.clear();
                                        ioPreBook = true;
                                        ioValid = false; ioPostBook = false;
                                        validDialog.dismiss();
                                    }
                                });
                                Valid_btn = (Button) validDialog.findViewById(R.id.valid_btn);
                                Valid_btn.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        validDialog.dismiss();
                                        int x = adapter.getNote();
                                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                                        params.add(new BasicNameValuePair(conf.tag_id, idBook));
                                        params.add(new BasicNameValuePair(conf.tag_tokenDriver, tokenOfDriver));
                                        params.add(new BasicNameValuePair(conf.tag_value, x + ""));
                                        JSONObject json = sr.getJSON(conf.url_addNote, params);
                                        if(json != null){
                                            try{
                                                String jsonstr = json.getString(conf.response);
                                                Toast.makeText(getActivity(), jsonstr, Toast.LENGTH_LONG).show();
                                                if(json.getBoolean(conf.res)){
                                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                                    ft.replace(R.id.container_body, new Home());
                                                    ft.commit();
                                                    ((Main) getActivity()).getSupportActionBar().setTitle(getString(R.string.home));
                                                }
                                            }catch(JSONException e){
                                                e.printStackTrace();
                                            }
                                        }else{
                                            Toast.makeText(getActivity(),R.string.serverunvalid,Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                                validDialog.show();
                                ArrayList<ServicesDB> servicesDBList = new ArrayList<>();
                                List<NameValuePair> params = new ArrayList<NameValuePair>();
                                JSONObject json = sr.getJSON(conf.url_getAllService, params);
                                if(json != null){
                                    try{
                                        if(json.getBoolean(conf.res)) {
                                            JSONArray loads = json.getJSONArray("data");
                                            for (int i = 0; i < loads.length(); i++) {
                                                JSONObject c = loads.getJSONObject(i);
                                                String id = c.getString(conf.tag_id);
                                                String name = c.getString(conf.tag_name);
                                                int value = c.getInt(conf.tag_value);
                                                ServicesDB rec = new ServicesDB(id, name, value);
                                                servicesDBList.add(rec);
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                adapter = new ServiceAdapterList(getActivity(), servicesDBList, BookNow.this);
                                Services_lv.setAdapter(adapter);
                            }
                        } catch (JSONException e) { }
                    }
                }
            });
        }
    };

    private Emitter.Listener handleIncomingDrawRoute = new Emitter.Listener(){
        public void call(final Object... args){
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (ioPostBook) {
                        ioPreBook = false; ioValid = false;
                        JSONObject data = (JSONObject) args[0];
                        Double origLat, origLon, desLat, desLon;
                        String token;
                        try {
                            origLat = data.getDouble(conf.tag_originLatitude);
                            origLon = data.getDouble(conf.tag_originLongitude);
                            desLat = data.getDouble(conf.tag_desLatitude);
                            desLon = data.getDouble(conf.tag_desLongitude);
                            token = data.getString(conf.tag_token);
                            if (token.equals(tokenOfDriver)) {
                                googleMap.clear();
                                ioEndCourse = true;
                                Valid_btn.setVisibility(View.VISIBLE);
                                MarkerOptions options = new MarkerOptions();
                                LatLng origin = new LatLng(origLat,origLon);
                                LatLng dest = new LatLng(desLat,desLon);
                                options.position(origin);
                                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title("Start");
                                googleMap.addMarker(options);
                                options.position(dest);
                                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title("End");
                                googleMap.addMarker(options);
                                String url = getDirectionsUrl(origin, dest);
                                DownloadTask downloadTask = new DownloadTask();
                                downloadTask.execute(url);
                            }
                        } catch (JSONException e) { }
                    }
                }
            });
        }
    };

    private Emitter.Listener handleIncomingPostBook = new Emitter.Listener(){
        public void call(final Object... args){
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (ioPostBook) {
                        ioPreBook = false; ioValid = false;
                        JSONObject data = (JSONObject) args[0];
                        Double lat, lon;
                        String token;
                        try {
                            lat = data.getDouble(conf.tag_latitude);
                            lon = data.getDouble(conf.tag_longitude);
                            token = data.getString(conf.tag_token);
                            if (token.equals(tokenOfDriver)) {
                                if (driverTaxi.isEmpty()) {
                                    MarkerOptions a = new MarkerOptions().position(new LatLng(lat, lon))
                                            .title(usernameOfDriver)
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                    Marker m = googleMap.addMarker(a);
                                    TaxiPosition t = new TaxiPosition(token, "", lat, lon, m);
                                    driverTaxi.add(t);
                                } else {
                                    for (int i = 0; i < driverTaxi.size(); i++) {
                                        if (token.equals(driverTaxi.get(i).getToken())) {
                                            driverTaxi.get(i).getMarker().setPosition(new LatLng(lat, lon));
                                            break;
                                        }
                                    }

                                }
                            }
                        } catch (JSONException e) { }
                    }
                }
            });
        }
    };

    private Emitter.Listener handleIncomingValidBook = new Emitter.Listener(){
        public void call(final Object... args){
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (ioValid) {
                        JSONObject data = (JSONObject) args[0];
                        Double lat, lon;
                        String token;
                        try {
                            lat = data.getDouble(conf.tag_latitude);
                            lon = data.getDouble(conf.tag_longitude);
                            token = data.getString(conf.tag_token);
                            if (token.equals(tokenOfDriver)) {
                                MarkerOptions options = new MarkerOptions();
                                LatLng origin = new LatLng(lat,lon);
                                LatLng dest = new LatLng(latitude,longitude);
                                options.position(dest);
                                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).title("Me");
                                googleMap.addMarker(options);
                                options.position(origin);
                                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title(usernameOfDriver);
                                googleMap.addMarker(options);
                                String url = getDirectionsUrl(origin, dest);
                                DownloadTask downloadTask = new DownloadTask();
                                downloadTask.execute(url);
                                ioPostBook = true;
                            }
                        } catch (JSONException e) { }
                    }
                }
            });
        }
    };

    private Emitter.Listener handleIncomingPreBook = new Emitter.Listener(){
        public void call(final Object... args){
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    if (ioPreBook) {
                        JSONObject data = (JSONObject) args[0];
                        Double lat, lon;
                        String token, socket;
                        Boolean working;
                        try {
                            lat = data.getDouble(conf.tag_latitude);
                            lon = data.getDouble(conf.tag_longitude);
                            token = data.getString(conf.tag_token);
                            socket = data.getString(conf.tag_socket);
                            working = data.getBoolean(conf.tag_working);
                            if (working) {
                                if (listTaxi.isEmpty()) {
                                    MarkerOptions a = new MarkerOptions().position(new LatLng(lat, lon))
                                            .title(token)
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                    Marker m = googleMap.addMarker(a);
                                    TaxiPosition t = new TaxiPosition(token, socket, lat, lon, m);
                                    listTaxi.add(t);
                                } else {
                                    boolean existTaxi = false;
                                    int position = 0;
                                    for (int i = 0; i < listTaxi.size(); i++) {
                                        if (socket.equals(listTaxi.get(i).getSocket())) {
                                            existTaxi = true;
                                            position = i;
                                            break;
                                        } else {
                                            existTaxi = false;
                                        }
                                    }
                                    if (existTaxi) {
                                        listTaxi.get(position).getMarker().setPosition(new LatLng(lat, lon));
                                        listTaxi.get(position).setLatitude(lat);
                                        listTaxi.get(position).setLongitude(lon);
                                    } else {
                                        MarkerOptions a = new MarkerOptions().position(new LatLng(lat, lon))
                                                .title(token)
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                        Marker m = googleMap.addMarker(a);
                                        TaxiPosition t = new TaxiPosition(token, socket, lat, lon, m);
                                        listTaxi.add(t);
                                    }
                                }
                            } else {
                                if (!listTaxi.isEmpty()) {
                                    for (int i = 0; i < listTaxi.size(); i++) {
                                        if (token.equals(listTaxi.get(i).getToken())) {
                                            listTaxi.get(i).getMarker().remove();
                                            listTaxi.remove(i);
                                            break;
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                        }
                    }
                }
            });
        }
    };

    private String getDirectionsUrl(LatLng origin,LatLng dest){
        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;
        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;
        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
        return url;
    }
    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb  = new StringBuffer();
            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        }catch(Exception e){
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {
        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = "";
            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
            }
            return data;
        }
        // Executes in UI thread, after the execution of doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }
    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionMap parser = new DirectionMap();
                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }
        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            String distance = "";
            String duration = "";
            if(result.size()<1){
                Toast.makeText(getActivity(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }
            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);
                    if(j==0){	// Get distance from the list
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");
                        continue;
                    }
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);
            }
            DistanceDuration_txt.setText("Distance:"+distance + ", Duration:"+duration);
            // Drawing polyline in the Google Map for the i-th route
            googleMap.addPolyline(lineOptions);
        }
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
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
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
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
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
            locationManager.removeUpdates(this);
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
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        cameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude));
        googleMap.moveCamera(cameraUpdate);
        changeLocation();
    }
    private void changeLocation() { }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}


    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopUsingGPS();
        socket.disconnect();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopUsingGPS();
        socket.disconnect();
        mMapView.onDestroy();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.container_body, new Home());
        ft.addToBackStack(null);
        ft.commit();
        ((Main) getActivity()).getSupportActionBar().setTitle(getString(R.string.home));
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
