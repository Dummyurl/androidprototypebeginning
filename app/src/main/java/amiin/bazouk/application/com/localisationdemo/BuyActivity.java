package amiin.bazouk.application.com.localisationdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import amiin.bazouk.application.com.localisationdemo.Forms.BuyForm;

public class BuyActivity extends AppCompatActivity {

    private static final int BUY_FORM_REQUEST_CODE =4;
    public static final String PRICE_MIN_BUY_INTENT_VALUE = "price_min_buy_intent_value";
    public static final String PRICE_MAX_BUY_INTENT_VALUE = "price_max_buy_intent_value";
    private final String IS_ON_BUY_EDITOR_VALUE = "is_on_buy_editor_value";
    private static final String PRICE_BUY_EDITOR_VALUE = "price_editor_value";
    private final String VOLUME_BUY_EDITOR_VALUE = "volume_buy_editor_value";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private double minPrice;
    private double maxPrice;
    private double currentLatitude;
    private double currentLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buy_activity);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        editor.apply();

        currentLatitude = getIntent().getDoubleExtra(MapsActivity.USER_LATITUDE_INTENT, 0);
        currentLongitude = getIntent().getDoubleExtra(MapsActivity.USER_LONGITUDE_INTENT, 0);
        
        findViewById(R.id.parameters).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BuyActivity.this, BuyForm.class);
                startActivityForResult(intent, BUY_FORM_REQUEST_CODE);
            }
        });

        findViewById(R.id.connection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isOn = preferences.getBoolean(IS_ON_BUY_EDITOR_VALUE,false);
                if(isOn)
                {
                    isOn = false;
                    v.setBackgroundColor(getResources().getColor(R.color.silver));
                    ((Button)v).setText("CONNECTION");
                }
                else {
                    isOn = true;
                    v.setBackgroundColor(getResources().getColor(R.color.black));
                    ((Button)v).setText("CONNECTED");
                    Thread threadUpdateExpenses = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Handler handlerUpdateExpenses = new Handler(getMainLooper());
                            while(preferences.getBoolean(IS_ON_BUY_EDITOR_VALUE,false) && MapsActivity.getUser().getMarkerBought()!=null)
                            {
                                handlerUpdateExpenses.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        double valueSpent = Double.longBitsToDouble(preferences.getLong(PRICE_BUY_EDITOR_VALUE, 0));
                                        MapsActivity.getUser().addExpenses(valueSpent);
                                        MapsActivity.getUser().getMarkerBought().getOwnerMarker().addEarnings(valueSpent);
                                        ((TextView)findViewById(R.id.expenses)).setText(String.valueOf( MapsActivity.getUser().getExpenses())+" $");
                                        ((TextView)findViewById(R.id.volume)).setText("0");
                                    }
                                });
                                try {
                                    Thread.sleep(10000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    threadUpdateExpenses.start();
                }
                editor.putBoolean(IS_ON_BUY_EDITOR_VALUE,isOn);
                editor.commit();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BUY_FORM_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(BuyActivity.this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(BuyActivity.this);
            }
            minPrice = Double.longBitsToDouble(data.getLongExtra(PRICE_MIN_BUY_INTENT_VALUE,-1));
            maxPrice = Double.longBitsToDouble(data.getLongExtra(PRICE_MAX_BUY_INTENT_VALUE,-1));
            List<MarkerSold> sellMarkers = MapsActivity.sellMarkers;
            double minimumDistance;
            int k =0;
            while(k<sellMarkers.size() && (sellMarkers.get(k).getPrice()<minPrice || sellMarkers.get(k).getPrice()>maxPrice)) {
                k++;
            }
            if(k==sellMarkers.size())
            {
                builder.setTitle("Wifi available not found")
                        .setMessage("We could not find a wifi available next to you for your price range")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setIcon(android.R.drawable.ic_dialog_alert).show();
            }
            else {
                minimumDistance = Math.sqrt(Math.pow(getIntent().getDoubleExtra("latitude", 0) - sellMarkers.get(k).getLatLng().latitude, 2) + Math.pow(getIntent().getDoubleExtra("longitude", 0) - sellMarkers.get(k).getLatLng().longitude, 2));
                int numberOfTheMarkerSoldChosen = k;
                
                for (int i = k + 1; i < sellMarkers.size(); i++) {
                    double distanceToMarker = Math.sqrt(Math.pow(currentLatitude - sellMarkers.get(i).getLatLng().latitude, 2) + Math.pow(currentLongitude - sellMarkers.get(i).getLatLng().longitude, 2));
                    if (sellMarkers.get(i).getPrice() >= minPrice && sellMarkers.get(i).getPrice() <= maxPrice && minimumDistance > distanceToMarker) {
                        minimumDistance = distanceToMarker;
                        numberOfTheMarkerSoldChosen = i;
                    }
                }
                MarkerSold markerBought = sellMarkers.get(numberOfTheMarkerSoldChosen);
                User buyer = MapsActivity.getUser();
                buyer.setMarkerBought(markerBought);
                markerBought.addBuyerMarker(buyer);
                double price = markerBought.getPrice();
                builder.setTitle("Wifi found")
                        .setMessage("Wifi found for " + sellMarkers.get(numberOfTheMarkerSoldChosen).getPrice())
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setIcon(android.R.drawable.ic_dialog_alert).show();
                editor.putLong(PRICE_BUY_EDITOR_VALUE, Double.doubleToRawLongBits(price));
                editor.putLong(VOLUME_BUY_EDITOR_VALUE, 0);
                editor.commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_seller, menu);
        setTitle("Buyer View");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back:
                Intent result = new Intent();
                setResult(RESULT_OK, result);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
