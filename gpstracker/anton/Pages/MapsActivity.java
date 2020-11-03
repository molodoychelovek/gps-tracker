package com.gpstracker.anton.Pages;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.gpstracker.anton.CacheRoom;
import com.gpstracker.anton.MyProperties;
import com.gpstracker.anton.OnClearFromRecentService;
import com.gpstracker.anton.R;
import com.gpstracker.anton.Server.BitmapEditor;
import com.gpstracker.anton.Server.DataServer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        GoogleMap.OnMarkerClickListener {
    private HashMap<String, String> prop;
    private GoogleMap googleMap;
    private boolean gps = false;
    private boolean find_me = true;
    private String id_room = "0";
    private HashMap<String, Marker> markers = new HashMap<>(); // USER - MARK

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        gps = true;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        startService(new Intent(this, OnClearFromRecentService.class));
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }

    Marker redMarker;
    int touch = 0;

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                if(touch == 0) {
                    redMarker = googleMap.addMarker(new MarkerOptions().position(point));
                    touch++;
                } else {
                    visibleRedMarker = true;
                    animateMarker(redMarker, point, false);
                }
            }
        });


        prop = new MyProperties().getProp(this);

        HashMap<String, String> user = new DataServer().getUser(prop.get("email"));
        id_room = user.get("room");
        reloadMap();

    }


    boolean visibleRedMarker = true;
    @Override
    public boolean onMarkerClick(final Marker marker) {

        if (marker.equals(redMarker))
        {
            if(visibleRedMarker) {
                System.out.println("CLICK  MARKER");
                animateMarker(marker, marker.getPosition(), true);
                visibleRedMarker = false;
            }
        }
        return false;
    }

    Runnable refresh = null;
    private void reloadMap(){
        final Handler handler = new Handler();

        refresh = new Runnable() {
            public void run() {
                if(gps) {
                    new ReloadMaps().execute("");
                    System.out.println("reload");
                }
                if(find_me)
                    handler.postDelayed(refresh, 1000);
                else
                    handler.postDelayed(refresh, 4500);
            }
        };
        handler.post(refresh);
    }


    @Override
    public void onLocationChanged(Location location) {
        if(gps) {
            try {
                final double latitude = location.getLatitude();
                final double longitude = location.getLongitude();

                new DataServer().updateUserPos(prop.get("email"), String.valueOf(latitude), String.valueOf(longitude));
                System.out.println("lat: " + latitude + " long: " + longitude);
                //Thread.sleep(4000);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = googleMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 700;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }



    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public void back_butt(View view) {
        gps = false;
        HashMap<String, String> user = new DataServer().getUser(prop.get("email"));
        HashMap<String, String> roomusers = new DataServer().getRoom(user.get("room"));

        new DataServer().updateUserRoom(prop.get("email"), user.get("room"));
        new DataServer().updateRoom(prop.get("email"), roomusers.get("users"), user.get("room"), true);

        Intent intent = new Intent(this, Rooms.class);
        startActivity(intent);
    }

    private boolean first = false;

    public void cam_click(View view) {
        HashMap<String, String> user = new DataServer().getUser(prop.get("email"));

        double latitude = Double.parseDouble(user.get("latitude"));
        double longitude = Double.parseDouble(user.get("longitude"));

        if(latitude > 0 && longitude > 0)
            find_me = false;

        final LatLng myPoint = new LatLng(latitude, longitude);

        CameraPosition camPos = new CameraPosition.Builder()
                .target(myPoint)
                .zoom(18)
                .build();
        CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
        System.out.println("CAM+");
        googleMap.animateCamera(camUpd3);
    }

    private class ReloadMaps extends AsyncTask<String, Integer, Integer> {
        @Override
        protected Integer doInBackground(final String... parameter) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final CacheRoom cacheRoom = new CacheRoom();

                    final HashMap<String, String> room = new DataServer().getRoom(id_room);
                    ArrayList<String> list = new ArrayList<>();
                    try {
                        list = new ArrayList<String>(Arrays.asList(room.get("users").split(" ")));
                    } catch (Exception e) { }

                    for (final String email : list) {
                        HashMap<String, String> user = new DataServer().getUser(email);

                        final String img = user.get("img");

                        System.out.println("Email: " + email);

                        double latitude = Double.parseDouble(user.get("latitude"));
                        double longitude = Double.parseDouble(user.get("longitude"));


                        final LatLng myPoint = new LatLng(latitude, longitude);

                        String newImage = img.replace("http://142.93.139.45/gpstracker/api/v1/users/images/", "");

                        String path = Environment.getExternalStorageDirectory()
                                + File.separator + "GPSTracker/" + newImage;
                        BitmapEditor bitmapEditor = new BitmapEditor();

                        if(cacheRoom.existsImg(img, room.get("id"))) {
                            Bitmap bitmap = BitmapFactory.decodeFile(path);
                            bitmap = bitmapEditor.getResizedBitmap(bitmap, 100, 100);
                            bitmap = bitmapEditor.getCroppedBitmap(bitmap);

                            if(!markers.containsKey(user.get("email"))){
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(myPoint)
                                        .title(user.get("email"))
                                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                        .flat(true);

                                Marker marker = googleMap.addMarker(markerOptions);
                                markers.put(user.get("email"), marker);
                            }

                            animateMarker(markers.get(user.get("email")), myPoint, false);
                        }
                        else{
                            cacheRoom.getCache(room.get("id"));
                            Bitmap bitmap = BitmapFactory.decodeFile(path);
                            bitmap = bitmapEditor.getResizedBitmap(bitmap, 100, 100);
                            bitmap = bitmapEditor.getCroppedBitmap(bitmap);

                            if(!markers.containsKey(user.get("email"))){
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(myPoint)
                                        .title(user.get("email"))
                                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                        .flat(true);

                                Marker marker = googleMap.addMarker(markerOptions);
                                markers.put(user.get("email"), marker);
                            }

                            animateMarker(markers.get(user.get("email")), myPoint, false);
                        }
                        System.out.println("USER [M/A]: " + user.get("email"));
                        if(first && user.get(email).equals(prop.get("email"))) {
                            CameraPosition camPos = new CameraPosition.Builder()
                                    .target(myPoint)
                                    .zoom(18)
                                    .build();
                            CameraUpdate camUpd3 = CameraUpdateFactory.newCameraPosition(camPos);
                            System.out.println("CAM+");
                            googleMap.animateCamera(camUpd3);
                            first = false;
                        }
                    }
                }
            });
            return 0;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
        }
    }
}
