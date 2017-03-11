package com.mateyinc.marko.matey.activity.maps;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.inall.MyApplication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.mateyinc.marko.matey.R.id.map;
import static com.mateyinc.marko.matey.activity.NewPostActivity.EXTRA_MAP_POSITIONS;
import static com.mateyinc.marko.matey.inall.MyApplication.PERMISSIONS_REQUEST_ACCESS_LOCATION;

public class MapsActivity extends MotherActivity implements GoogleMap.InfoWindowAdapter, OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

    private static final String TAG = MapsActivity.class.getSimpleName();
    /**
     * Bundle key for storing data in instance state
     */
    private static final String KEY_LOCATIONS = "locations_data";


    private SearchView svSearchInput;

    private GoogleMap mMap;
    //    private List<Marker> mLocations = new ArrayList<>();
    private LocationManager mLocationManager;
    //    private RecyclerView rvLocationlist;
    private Button btnOk;
    private List<Marker> mMarkerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        init();
        setClickListeners();

//        if (savedInstanceState != null)
//            mAdapter.setSerializedData(savedInstanceState.getString(KEY_LOCATIONS));
    }

    private void init() {
        super.setChildSupportActionBar();

        mMarkerList = new ArrayList<>();
        mLocationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        if (displayGpsStatus() && checkGPSPermission()) {
            mLocationManager.requestLocationUpdates(LocationManager
                    .GPS_PROVIDER, 5000, 10, new MyLocationListener());
        } else {
            Toast.makeText(MapsActivity.this, getString(R.string.maps_gpsOff_notif), Toast.LENGTH_LONG).show();
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        svSearchInput = (SearchView) findViewById(R.id.svSearchInput);
        btnOk = (Button) findViewById(R.id.btnOK);
//        rvLocationlist = (RecyclerView) findViewById(R.id.rvLocationList);
//
//        if (mAdapter == null)
//            mAdapter = new LocationsAdapter(this, mMap);
//
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        rvLocationlist.setLayoutManager(layoutManager);
//        rvLocationlist.setAdapter(mAdapter);
    }

    private void setClickListeners() {

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();

                String list = MarkerJSON.serialiseMarkers(mMarkerList);

                if (!list.isEmpty())
                    i.putExtra(EXTRA_MAP_POSITIONS, list);
                setResult(RESULT_OK, i);
                finish();
            }
        });

        svSearchInput.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String address = svSearchInput.getQuery().toString();

                try {
                    List<Address> addresses = getListOfAddress(address);
                    if (addresses.size() > 0) {
                        Double lat = (double) (addresses.get(0).getLatitude());
                        Double lon = (double) (addresses.get(0).getLongitude());

                        Log.d("lat-long", "" + lat + "......." + lon);
                        LatLng user = new LatLng(lat, lon);
        /*used marker for show the location */
//                        Marker hamburg = mMap.addMarker(new MarkerOptions()
//                                .position(user)
//                                .title(adderess));

                        // Move the camera instantly to hamburg with a zoom of 15.
//                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user, 15));

                        // Zoom in, animating the camera.
//                        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

                        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(
                                user, 15);
                        mMap.animateCamera(location);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    private List<Address> getListOfAddress(String address) throws IOException {
          /* get latitude and longitude from the adderress */
        Geocoder geoCoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        return geoCoder.getFromLocationName(address, 5);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        mMap.setInfoWindowAdapter(this);

        if (checkGPSPermission()) {
            mMap.setMyLocationEnabled(true);
        } else
            return;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        showInputDialog(latLng);
    }

    /**
     * Helper method for showing input dialog for entering new location name.
     */
    private void showInputDialog(final LatLng latLng) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Title");

        final View view = LayoutInflater.from(this).inflate(R.layout.maps_input_dialog, null);

        builder.setView(view);
        builder.setTitle(R.string.maps_new_location_name);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get location name
                String name = ((EditText) view.findViewById(R.id.etInputField)).getText().toString();
                // Add a marker
                LatLng newLocation = latLng;
                MarkerOptions mp = new MarkerOptions().position(newLocation).
                        title(name);
//                mAdapter.addData(mp);
                Marker m = mMap.addMarker(mp);
                mMarkerList.add(m);

                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // TODO - finish showing marker info into bottom view
        return false;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return prepareInfoView(marker);
    }

    private View prepareInfoView(final Marker marker) {
        //prepare InfoView programmatically
        LinearLayout infoView = new LinearLayout(MapsActivity.this);
        LinearLayout.LayoutParams infoViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        infoView.setOrientation(LinearLayout.HORIZONTAL);
        infoView.setLayoutParams(infoViewParams);
        infoView.setClickable(false);

        ImageView infoImageView = new ImageView(MapsActivity.this);
        //Drawable drawable = getResources().getDrawable(R.mipmap.ic_launcher);
        Drawable drawable = getResources().getDrawable(android.R.drawable.ic_dialog_map);
        infoImageView.setImageDrawable(drawable);
        infoView.addView(infoImageView);

        TextView text = new TextView(MapsActivity.this);
        LinearLayout.LayoutParams subInfoViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        text.setLayoutParams(subInfoViewParams);
        text.setText("Title: " + marker.getTitle());
        infoView.addView(text);

        return infoView;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        outState.putString(KEY_LOCATIONS, mAdapter.getSerializedData());
        super.onSaveInstanceState(outState);
    }

    @Override
    @SuppressWarnings({"MissingPermission"})
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == MyApplication.PERMISSIONS_REQUEST_ACCESS_LOCATION) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                        return;
                    } else {
                        Log.e(TAG, "Permission denied.");
                    }
                }
            }
        }
    }

    /**
     * Method to check if GPS is on or off
     *
     * @return true if it's on
     */
    private Boolean displayGpsStatus() {
        ContentResolver contentResolver = getBaseContext()
                .getContentResolver();
        boolean gpsStatus = Settings.Secure
                .isLocationProviderEnabled(contentResolver,
                        LocationManager.GPS_PROVIDER);
        if (gpsStatus) {
            return true;

        } else {
            return false;
        }
    }

    /**
     * Method to check if user has granted location permission, if not prompt to grant.
     *
     * @return true if permission is granted.
     */
    private synchronized boolean checkGPSPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_LOCATION);
            return false;
        }
        return true;
    }


    /*---------- Listener class to get coordinates ------------- */
    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            String longitude = "Longitude: " + loc.getLongitude();
            String latitude = "Latitude: " + loc.getLatitude();
        }

        @Override
        public void onProviderDisabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStatusChanged(String provider,
                                    int status, Bundle extras) {
            // TODO Auto-generated method stub
        }
    }
}
