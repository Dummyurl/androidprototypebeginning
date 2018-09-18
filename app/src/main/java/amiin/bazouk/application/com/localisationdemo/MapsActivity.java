package amiin.bazouk.application.com.localisationdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String PRICE_SELL_INTENT_VALUE = "price_sell_intent_value";
    public static final String VOLUME_SELL_INTENT_VALUE = "volume__sell_intent_value";
    public static final String ISON_SELL_INTENT_VALUE = "ison_sell_intent_value";
    public static final String USER_LONGITUDE_INTENT = "user_longitutde_intent";
    public static final String USER_LATITUDE_INTENT = "user_latitude_intent";
    private static final int PERMISSION_REQUEST_CODE = 10;
    private static final int SELL_REQUEST_CODE = 1;
    private static final int BUY_REQUEST_CODE = 2;
    public static List<MarkerSold> sellMarkers;
    private static User user;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private CircleOptions circleOptions;
    private Circle circle;
    private Boolean beginningOfTheMap = true;
    private Location location = null;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private static double calculateCircleRadiusMeterForMapCircle(final int _targetRadiusDip, final double _circleCenterLatitude,
                                                                 final float _currentMapZoom) {
        final double arbitraryValueForDip = 100000D;
        final double oneDipDistance = Math.abs(Math.cos(Math.toRadians(_circleCenterLatitude))) * arbitraryValueForDip / Math.pow(2, _currentMapZoom);
        return oneDipDistance * (double) _targetRadiusDip;
    }

    public static User getUser() {
        return user;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        editor.apply();

        user = new User("F");
        sellMarkers = new ArrayList<>();
        getSellMarkers();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mMap.clear();
                MapsActivity.this.location = location;
                double latitude = location.getLatitude();
                LatLng latLng = new LatLng(latitude, location.getLongitude());
                if (circle != null) {
                    circle.remove();
                }

                float zoomLevel;
                if (beginningOfTheMap) {
                    zoomLevel = 16.0f;
                } else {
                    zoomLevel = mMap.getCameraPosition().zoom;
                }
                circleOptions = new CircleOptions();
                circleOptions.center(latLng);
                circleOptions.radius(calculateCircleRadiusMeterForMapCircle(12, latitude, zoomLevel));
                circleOptions.fillColor(Color.BLUE);
                circleOptions.strokeColor(Color.RED);
                circleOptions.strokeWidth(4);

                circle = mMap.addCircle(circleOptions);

                for (MarkerSold sellMarker : sellMarkers) {
                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(sellMarker.getLatLng().latitude, sellMarker.getLatLng().longitude)).snippet("User: " + sellMarker.getOwnerMarker() + "\n" + "Price: " + sellMarker.getPrice() + "\n" + "Volume: " + sellMarker.getVolume());
                    Marker marker = mMap.addMarker(markerOptions);
                    marker.setTag(location);
                    if (user.getMarkerBought() != null && user.getMarkerBought().equals(sellMarker)) {
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    }
                    sellMarker.setMarker(marker);
                }

                if (beginningOfTheMap) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
                }
                beginningOfTheMap = false;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        };
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getSellMarkers() {
        MarkerSold markerSold1 = new MarkerSold(new User("A"), null, new LatLng(32.077794, 34.781362), 3, 3);
        sellMarkers.add(markerSold1);

        MarkerSold markerSold2 = new MarkerSold(new User("B"), null, new LatLng(32.075612, 34.781652), 8, 4);
        sellMarkers.add(markerSold2);

        MarkerSold markerSold3 = new MarkerSold(new User("C"), null, new LatLng(32.077503, 34.782296), 7, 5);
        sellMarkers.add(markerSold3);

        MarkerSold markerSold4 = new MarkerSold(new User("D"), null, new LatLng(51.532293, -0.106230), 7, 5);
        sellMarkers.add(markerSold4);

        MarkerSold markerSold5 = new MarkerSold(new User("E"), null, new LatLng(51.499895, -0.134211), 7, 5);
        sellMarkers.add(markerSold5);

        //Take from a database all the markers according to a distance from the current location(I dont want all the markers in the world!)
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(MapsActivity.this);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView snippet = new TextView(MapsActivity.this);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(snippet);

                return info;
            }
        });

        findViewById(R.id.sell).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, SellActivity.class);
                startActivityForResult(intent, SELL_REQUEST_CODE);
            }
        });

        findViewById(R.id.buy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, BuyActivity.class);
                intent.putExtra(USER_LONGITUDE_INTENT, location.getLongitude());
                intent.putExtra(USER_LATITUDE_INTENT, location.getLatitude());
                startActivityForResult(intent, BUY_REQUEST_CODE);
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, PERMISSION_REQUEST_CODE);
        }
        else {
            getLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELL_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(ISON_SELL_INTENT_VALUE, false)) {
                LatLng newLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                MarkerSold sellMarker = new MarkerSold(user, null, newLatLng, data.getDoubleExtra(PRICE_SELL_INTENT_VALUE, -1), data.getDoubleExtra(VOLUME_SELL_INTENT_VALUE, -1));
                MarkerOptions markerOptions = new MarkerOptions().position(newLatLng).snippet("User: " + sellMarker.getOwnerMarker().getUsername() + "\n" + "Price: " + sellMarker.getPrice() + "\n" + "Volume: " + sellMarker.getVolume());
                Marker marker = mMap.addMarker(markerOptions);
                marker.setTag(location);
                sellMarker.setMarker(marker);
                user.setMarkerSold(sellMarker);
                sellMarkers.add(sellMarker);
            } else {
                removeMarker();
            }
        }
    }

    private void removeMarker() {
        for (MarkerSold sellMarker : sellMarkers) {
            if (sellMarker.getOwnerMarker().equals(user)) {
                sellMarker.getMarker().remove();
                sellMarkers.remove(sellMarker);
                return;
            }
        }
    }
}
