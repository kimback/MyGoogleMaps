package com.bluesweater.mygooglemaps;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    //위치 매니저
    private LocationManager locationManager;

    private TextView status;


    //리스너 정의
    private LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            status.setText("WIFI - 위도 : " + location.getLatitude() + "\n경도:"
                    + location.getLongitude() + "\n고도:"
                    + location.getAltitude());

            Log.i("LOCTIONTAG","onLocationChanged");
            updateMapsLocation(location.getLatitude(), location.getLongitude());

            //Uri uri = Uri.parse(String.format("geo:%f,%f?z=23", location.getLatitude(),
            //location.getLongitude()));
            //startActivity(new Intent(Intent.ACTION_VIEW, uri));


        }

        public void onStatusChanged(String provider, int status,
                                    Bundle extras) {

            Log.i("LOCTIONTAG","onStatusChanged");

        }

        public void onProviderEnabled(String provider) {
            Log.i("LOCTIONTAG","onProviderEnabled");
        }

        public void onProviderDisabled(String provider) {
            Log.i("LOCTIONTAG","onProviderDisabled");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        status = (TextView) findViewById(R.id.locStatus);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);

        mapFragment.getMapAsync(this);


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

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }


        //위치 업데이트 요청
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000,
                0, locationListener);


    }


    private void updateMapsLocation(double x, double y){
        // Add a marker in Sydney and move the camera
        LatLng nowLocation = new LatLng(x, y);
        mMap.addMarker(new MarkerOptions().position(nowLocation).title("Marker in Now Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(nowLocation, 17));

    }


    @Override
    public void onBackPressed() {
        if(locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
        finish();
    }


}
