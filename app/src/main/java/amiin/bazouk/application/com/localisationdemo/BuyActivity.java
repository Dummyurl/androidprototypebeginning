package amiin.bazouk.application.com.localisationdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import amiin.bazouk.application.com.localisationdemo.Forms.BuyForm;

public class BuyActivity extends AppCompatActivity {

    private static final int BUY_FORM_REQUEST_CODE =4;
    private final String SERIE_OF_EXPENSES_POINTS = "serie_of_earnings_points";
    public static final String PRICE_MIN_BUY_INTENT_VALUE = "price_min_buy_intent_value";
    public static final String PRICE_MAX_BUY_INTENT_VALUE = "price_max_buy_intent_value";
    private static final String PRICE_BUY_EDITOR_VALUE = "price_editor_value";
    private final String VOLUME_BUY_EDITOR_VALUE = "volume_buy_editor_value";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private double currentLatitude;
    private double currentLongitude;
    private double valueSpent;
    private List<Double> serieOfExpensesPoints;
    private LineGraphSeries<DataPointInterface> series;
    private GraphView graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buy_activity);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        editor.apply();

        serieOfExpensesPoints = new ArrayList<>();
        serieOfExpensesPoints.add((double) 0);

        serieOfExpensesPoints = new ArrayList<>();
        String savedString = preferences.getString(SERIE_OF_EXPENSES_POINTS, "0");
        StringTokenizer st = new StringTokenizer(savedString, ",");
        for (int i = 0; i < st.countTokens(); i++) {
            serieOfExpensesPoints.add(Double.parseDouble(st.nextToken()));
        }
        graph = findViewById(R.id.graph_view_sell);
        graph.getGridLabelRenderer().setGridStyle( GridLabelRenderer.GridStyle.NONE );
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);// remove horizontal x labels and line
        graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        graph.setBackgroundColor(Color.BLACK);
        series = new LineGraphSeries<>();

        currentLatitude = getIntent().getDoubleExtra(MapsActivity.USER_LATITUDE_INTENT, 0);
        currentLongitude = getIntent().getDoubleExtra(MapsActivity.USER_LONGITUDE_INTENT, 0);

        valueSpent = Double.longBitsToDouble(preferences.getLong(PRICE_BUY_EDITOR_VALUE, 0));

        Button connectionButton = findViewById(R.id.connection);
        if(MapsActivity.getUser().isBuyOn())
        {
            connectionButton.setBackgroundColor(getResources().getColor(R.color.black));
            connectionButton.setText(R.string.connected);
        }
        else {
            connectionButton.setBackgroundColor(getResources().getColor(R.color.silver));
            connectionButton.setText(R.string.connection);
        }

        graphExpensesUpdates();
        updateExpense();
        
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
                User buyer = MapsActivity.getUser();
                if(buyer.isBuyOn())
                {
                    disconnect();
                }
                else {
                    buyer.setWebSocketClient("ws://echo.websocket.org");
                    ConnectionDialog connectionDialog = new ConnectionDialog(BuyActivity.this,BuyActivity.this);
                    connectionDialog.show();
                }
            }
        });
    }

    public void connect() {
        User buyer = MapsActivity.getUser();
        Button buttonConnection = findViewById(R.id.connection);
        buttonConnection.setBackgroundColor(getResources().getColor(R.color.black));
        buttonConnection.setText(R.string.connected);
        buyer.setExpenses(valueSpent);
        buyer.getMarkerSold().getOwnerMarker().addEarnings(valueSpent);
        buyer.setBuyOn(true);
        editor.commit();
    }

    private void graphExpensesUpdates() {
        Thread threadUpdateGraphExpenses = new Thread(new Runnable() {
            @Override
            public void run() {
                Handler handlerUpdateGraphExpenses = new Handler(getMainLooper());
                while(true) {
                    handlerUpdateGraphExpenses.post(new Runnable() {
                        @Override
                        public void run() {
                            series = new LineGraphSeries<>();
                            graph.removeAllSeries();
                            double x, y;
                            x = 0;
                            for (int i = 0; i < serieOfExpensesPoints.size(); i++) {
                                x = x + 0.01;
                                y = serieOfExpensesPoints.get(i);
                                series.appendData(new DataPoint(x, y), true, 100);
                                graph.addSeries(series);
                            }
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
        threadUpdateGraphExpenses.start();
    }

    private void disconnect() {
        User buyer = MapsActivity.getUser();
        buyer.getMarkerBought().getOwnerMarker().soustractEarnings(valueSpent);
        buyer.setExpenses(0);
        findViewById(R.id.connection).setBackgroundColor(getResources().getColor(R.color.silver));
        ((Button)findViewById(R.id.connection)).setText(R.string.connection);
        buyer.closeWebSocketClient();
        buyer.setBuyOn(false);
        editor.commit();
    }

    private void updateExpense() {
        Thread threadUpdateExpenses = new Thread(new Runnable() {
            @Override
            public void run() {
                Handler handlerUpdateExpenses = new Handler(getMainLooper());
                while(true)
                {
                    handlerUpdateExpenses.post(new Runnable() {
                        @Override
                        public void run() {
                            double newEarning =  MapsActivity.getUser().getExpenses();
                            ((TextView)findViewById(R.id.expenses)).setText(String.valueOf(newEarning)+" $");
                            ((TextView)findViewById(R.id.volume)).setText("0");
                            if(serieOfExpensesPoints.size()==100) {
                                serieOfExpensesPoints.remove(0);
                            }
                            serieOfExpensesPoints.add(newEarning);
                            StringBuilder str = new StringBuilder();
                            for (int i = 0; i < serieOfExpensesPoints.size(); i++) {
                                str.append(serieOfExpensesPoints.get(i)).append(",");
                            }
                            editor.putString(SERIE_OF_EXPENSES_POINTS, str.toString());
                            editor.commit();
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BUY_FORM_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(BuyActivity.this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(BuyActivity.this);
            }
            double minPrice = Double.longBitsToDouble(data.getLongExtra(PRICE_MIN_BUY_INTENT_VALUE, -1));
            double maxPrice = Double.longBitsToDouble(data.getLongExtra(PRICE_MAX_BUY_INTENT_VALUE, -1));
            List<MarkerSold> sellMarkers = MapsActivity.sellMarkers;
            double minimumDistance;
            int k =0;
            while(k<sellMarkers.size() && (sellMarkers.get(k).getPrice()< minPrice || sellMarkers.get(k).getPrice()> maxPrice)) {
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
                valueSpent = markerBought.getPrice();
                builder.setTitle("Wifi found")
                        .setMessage("Wifi found for " + valueSpent)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setIcon(android.R.drawable.ic_dialog_alert).show();
                editor.putLong(PRICE_BUY_EDITOR_VALUE, Double.doubleToRawLongBits(valueSpent));
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
