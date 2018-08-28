package amiin.bazouk.application.com.localisationdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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

import amiin.bazouk.application.com.localisationdemo.Forms.SellForm;

public class SellActivity extends AppCompatActivity {

    private final String SERIE_OF_EARNINGS_POINTS = "serie_of_earnings_points";
    private final int SELL_FORM_REQUEST_CODE = 3;
    private Boolean isOn;
    private double price,volume;
    private SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    private final String PRICE_SELL_EDITOR_VALUE ="price_sell_editor_value";
    private final String VOLUME_SELL_EDITOR_VALUE = "volume_sell_editor_value";
    private final String NETWORK_NAME_SELL_EDITOR_VALUE ="network_name_sell_editor_value";
    private final String NETWORK_PASSWORD_SELL_EDITOR_VALUE = "network_password_sell_editor_value";
    public static final String NETWORK_NAME_INTENT_VALUE = "network_name_intent_value";
    public static final String NETWORK_PASSWORD_INTENT_VALUE = "network_password_intent_value";
    private final String IS_ON_SELL_EDITOR_VALUE = "is_on_sell_editor_value";
    private List<Double> serieOfEarningPoints;
    private LineGraphSeries<DataPointInterface> series;
    private GraphView graph;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sell_activity);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = preferences.edit();
        editor.apply();

        serieOfEarningPoints = new ArrayList<>();
        serieOfEarningPoints.add((double) 0);

        serieOfEarningPoints = new ArrayList<>();
        String savedString = preferences.getString(SERIE_OF_EARNINGS_POINTS, "0");
        StringTokenizer st = new StringTokenizer(savedString, ",");
        for (int i = 0; i < st.countTokens(); i++) {
            serieOfEarningPoints.add(Double.parseDouble(st.nextToken()));
        }
        graph = findViewById(R.id.graph_view_sell);
        graph.getGridLabelRenderer().setGridStyle( GridLabelRenderer.GridStyle.NONE );
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);// remove horizontal x labels and line
        graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        graph.setBackgroundColor(Color.BLACK);
        series = new LineGraphSeries<>();

        setUpParameters(Double.longBitsToDouble(preferences.getLong(PRICE_SELL_EDITOR_VALUE,0)),Double.longBitsToDouble(preferences.getLong(VOLUME_SELL_EDITOR_VALUE,0)), preferences.getBoolean(IS_ON_SELL_EDITOR_VALUE,false));

        graphEarningsUpdates();
        earningUpdates();

        findViewById(R.id.selling_activation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User seller = MapsActivity.getUser();
                if(isOn)
                {
                    isOn=false;
                    v.setBackgroundColor(getResources().getColor(R.color.silver));
                    MarkerSold markerSold = MapsActivity.getUser().getMarkerSold();
                    if(markerSold!=null) {
                        List<User> buyerMarkers = markerSold.getBuyersMarker();
                        for (User buyerMarker : buyerMarkers)
                        {
                            buyerMarker.soustractExpenses(price);
                            buyerMarker.setMarkerBought(null);
                            buyerMarker.closeWebSocketClient();
                            buyerMarker.setBuyOn(false);
                        }
                        markerSold.getBuyersMarker().clear();
                        markerSold.getMarker().remove();
                    }
                    seller.setExpenses(0);
                    ((Button)v).setText(R.string.selling_is_off);
                }
                else {
                    isOn=true;
                    v.setBackgroundColor(getResources().getColor(R.color.black));
                    ((Button)v).setText(R.string.selling_is_on);
                }
                editor.putBoolean(IS_ON_SELL_EDITOR_VALUE,isOn);
                editor.commit();
            }
        });

        findViewById(R.id.parameters).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SellActivity.this, SellForm.class);
                startActivityForResult(intent,SELL_FORM_REQUEST_CODE);
            }
        });
    }

    private void graphEarningsUpdates() {
        Thread threadUpdateGraphEarnings = new Thread(new Runnable() {
            @Override
            public void run() {
                Handler handlerUpdateGraphEarnings = new Handler(getMainLooper());
                while(true) {
                    handlerUpdateGraphEarnings.post(new Runnable() {
                        @Override
                        public void run() {
                            series = new LineGraphSeries<>();
                            graph.removeAllSeries();
                            double x, y;
                            x = 0;
                            for (int i = 0; i < serieOfEarningPoints.size(); i++) {
                                x = x + 0.01;
                                y = serieOfEarningPoints.get(i);
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
        threadUpdateGraphEarnings.start();
    }

    private void earningUpdates() {
        Thread threadUpdateEarnings = new Thread(new Runnable() {
            @Override
            public void run() {
                Handler handlerUpdateEarnings = new Handler(getMainLooper());
                while(true)
                {
                    handlerUpdateEarnings.post(new Runnable() {
                        @Override
                        public void run() {
                            double newEarning = MapsActivity.getUser().getEarnings();
                            ((TextView)findViewById(R.id.earnings)).setText(String.valueOf(newEarning)+" $");
                            ((TextView)findViewById(R.id.volume)).setText("0");
                            if(serieOfEarningPoints.size()==100) {
                                serieOfEarningPoints.remove(0);
                            }
                            serieOfEarningPoints.add(newEarning);
                            StringBuilder str = new StringBuilder();
                            for (int i = 0; i < serieOfEarningPoints.size(); i++) {
                                str.append(serieOfEarningPoints.get(i)).append(",");
                            }
                            editor.putString(SERIE_OF_EARNINGS_POINTS, str.toString());
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
        threadUpdateEarnings.start();
    }

    private void setUpParameters(double price, double volume, Boolean isOn)
    {
        this.price = price;
        this.volume = volume;
        this.isOn = isOn;
        Button sellingActivationButton = findViewById(R.id.selling_activation);
        if(isOn)
        {
            sellingActivationButton.setBackgroundColor(getResources().getColor(R.color.black));
            sellingActivationButton.setText(R.string.selling_is_on);
        }
        else {
            sellingActivationButton.setBackgroundColor(getResources().getColor(R.color.silver));
            sellingActivationButton.setText(R.string.selling_is_off);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELL_FORM_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            double price = data.getDoubleExtra(MapsActivity.PRICE_SELL_INTENT_VALUE,0);
            double volume = data.getDoubleExtra(MapsActivity.VOLUME_SELL_INTENT_VALUE,0);
            editor.putLong(PRICE_SELL_EDITOR_VALUE, Double.doubleToRawLongBits(price));
            editor.putLong(VOLUME_SELL_EDITOR_VALUE, Double.doubleToRawLongBits(volume));
            editor.putString(NETWORK_NAME_SELL_EDITOR_VALUE, data.getStringExtra(NETWORK_NAME_INTENT_VALUE));
            editor.putString(NETWORK_PASSWORD_SELL_EDITOR_VALUE, data.getStringExtra(NETWORK_PASSWORD_INTENT_VALUE));
            editor.apply();
            setUpParameters(price,volume,isOn);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_seller, menu);
        setTitle("Seller View");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back:
                Intent result = new Intent();
                setResult(RESULT_OK, result);
                result.putExtra(MapsActivity.PRICE_SELL_INTENT_VALUE, price);
                result.putExtra(MapsActivity.VOLUME_SELL_INTENT_VALUE, volume);
                result.putExtra(MapsActivity.ISON_SELL_INTENT_VALUE,isOn);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
