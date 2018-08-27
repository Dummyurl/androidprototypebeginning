package amiin.bazouk.application.com.localisationdemo;

import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import static android.content.Context.WIFI_SERVICE;

public class ConnectionDialog extends Dialog {
    private BuyActivity buyActivity;

    public ConnectionDialog(@NonNull Context context, BuyActivity buyActivity) {
        super(context);
        this.buyActivity = buyActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.identification_connection_dialog);
        setCanceledOnTouchOutside(false);

        findViewById(R.id.connect_to_wifi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread connectionThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String ssid = ((TextView)findViewById(R.id.wifi_name)).getText().toString();
                        String key = ((TextView)findViewById(R.id.wifi_password)).getText().toString();
                        WifiConfiguration wifiConfig = new WifiConfiguration();
                        wifiConfig.SSID = String.format("\"%s\"", ssid);
                        wifiConfig.preSharedKey = String.format("\"%s\"", key);
                        WifiManager wifiManager = (WifiManager) buyActivity.getApplicationContext().getSystemService(WIFI_SERVICE);
                        wifiManager.setWifiEnabled(true);
                        int netId = wifiManager.addNetwork(wifiConfig);
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(netId, true);
                        wifiManager.reconnect();
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ConnectivityManager connectivityManager = (ConnectivityManager) buyActivity.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
                            buyActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    buyActivity.connect();
                                    dismiss();
                                }
                            });
                        }
                        else {
                            buyActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((TextView)findViewById(R.id.wrong_identification)).setText(R.string.wrong_identification);
                                }
                            });
                        }
                    }
                });
                connectionThread.start();
            }
        });

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
