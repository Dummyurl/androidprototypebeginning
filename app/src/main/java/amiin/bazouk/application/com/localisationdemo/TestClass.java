package amiin.bazouk.application.com.localisationdemo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestClass extends AppCompatActivity {

    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private static final String TAG = "TEST ON HOTSPOT";
    private static final int PERMISSION_ACCESS_WIFI_REQUEST_CODE = 1000;
    private static final int PERMISSION_REQUEST_CODE = 10002;
    private WifiManager.LocalOnlyHotspotReservation reservation;
    private WifiManager wifiManager;
    private WifiManager mWifiManager;
    private List<ScanResult> wifiList;
    private SimpleAdapter adapterListWifis;
    private List<HashMap<String, String>> listMapOfEachWifi;
    private OkHttpClient client;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_class_activity);

        client = new OkHttpClient();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        ListView listViewWifis = findViewById(R.id.wifi_list_view);
        listMapOfEachWifi = new ArrayList<>();
        adapterListWifis = new SimpleAdapter(this, listMapOfEachWifi, R.layout.item_wifi,
            new String[]{"wifi_ssid","wifi_encryption"}, new int[]{R.id.wifi_ssid,R.id.wifi_encryption}) {
                @Override
                public View getView(final int position, final View convertView, ViewGroup parent) {
                    final View convertViewToReturn = super.getView(position, convertView, parent);
                    convertViewToReturn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((EditText)TestClass.this.findViewById(R.id.wifi_name)).setText(((TextView)((LinearLayout)v).getChildAt(0)).getText().toString());
                            ((EditText)TestClass.this.findViewById(R.id.encryption)).setText(((TextView)((LinearLayout)v).getChildAt(1)).getText().toString());
                        }
                    });
                    return convertViewToReturn;
                }
        };

        (listViewWifis).setAdapter(adapterListWifis);

        setUpTheViews();
    }

    private void setUpTheViews() {
        findViewById(R.id.connect_to_wifi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeKeyBoard();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(TestClass.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(TestClass.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, PERMISSION_REQUEST_CODE);
                    }
                    else{
                        connectToWifi();
                    }
                }
                else {
                    connectToWifi();
                }
            }
        });

        findViewById(R.id.go_to_main_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestClass.this,MapsActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.open_hotspot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                openWifiHotspotState();
            }
        });

        findViewById(R.id.close_hotspot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                closeWifiHotspotState();
            }
        });

        findViewById(R.id.list_wifis).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            PERMISSION_ACCESS_WIFI_REQUEST_CODE);
                    requestPermissions(new String[]{Manifest.permission.ACCESS_WIFI_STATE}, PERMISSION_ACCESS_WIFI_REQUEST_CODE);
                }
                else{
                    getWifiList();
                }
            }
        });

        findViewById(R.id.client_java).setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {
                Thread clientThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        URI uri;
                        try {
                            uri = new URI("http://18.223.249.82:3000");
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                            return;
                        }

                        final WebSocketClient webSocketClient = new WebSocketClient(uri) {

                            @Override
                            public void onOpen(ServerHandshake serverHandshake) {
                                Log.i("Websocket", "Opened");
                                send("Hello");
                            }

                            @Override
                            public void onMessage(String s) {
                                System.out.println("Message sent: " + s);
                                send(s);
                            }

                            @Override
                            public void onClose(int i, String s, boolean b) {
                                Log.i("Websocket", "Closed " + s);
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.i("Websocket", "Error " + e.getMessage());
                            }
                        };
                        webSocketClient.connect();
                    }
                });
                clientThread.start();
            }
        });

        findViewById(R.id.client_android).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread clientThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Request request = new Request.Builder().url("http://18.223.249.82:3000").build();
                        WebSocket ws = client.newWebSocket(request, new EchoWebSocketListener());
                        client.dispatcher().executorService().shutdown();

                    }
                });
                clientThread.start();
            }
        });

        findViewById(R.id.client_socket_io).setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {
                Thread clientThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Socket mSocket = null;
                        try {
                            mSocket = IO.socket("http://18.223.249.82:3000");
                        } catch (URISyntaxException e) {
                            System.out.print(e.getMessage());
                            int a = 0;
                            int b = 0;
                        }
                        mSocket.connect();
                        mSocket.on("new message", onNewMessage);
                        mSocket.emit("salut Sam c est Adrien","salut Sam c est Adrien");
                    }
                });
                clientThread.start();
            }
        });
    }

    private void closeKeyBoard() {
        View view = this.getCurrentFocus();
        if(view!=null)
        {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm!=null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void getWifiList() {
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                if (intent.getAction()!= null && intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    wifiList = mWifiManager.getScanResults();
                    listMapOfEachWifi.clear();
                    for (ScanResult scanResult : wifiList) {
                        HashMap<String, String> mapOfTheNewWifi = new HashMap<>();
                        mapOfTheNewWifi.put("wifi_ssid", scanResult.SSID);
                        mapOfTheNewWifi.put("wifi_encryption", "\t" + scanResult.capabilities);
                        listMapOfEachWifi.add(mapOfTheNewWifi);
                    }
                    adapterListWifis.notifyDataSetChanged();
                }
            }
        };
        registerReceiver(mWifiScanReceiver,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mWifiManager.startScan();
    }

    private void connectToWifi() {
        Thread connectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                if(wifiManager!=null) {
                    wifiManager.setWifiEnabled(true);
                    String ssid = ((EditText) findViewById(R.id.wifi_name)).getText().toString();
                    String key = ((EditText) findViewById(R.id.wifi_password)).getText().toString();
                    WifiConfiguration conf = new WifiConfiguration();
                    conf.SSID = String.format("\"%s\"", ssid);
                    conf.status = WifiConfiguration.Status.ENABLED;
                    conf.priority = 40;
                    if (((EditText) findViewById(R.id.encryption)).getText().toString().equals("WEP")) {
                        Log.v("rht", "Configuring WEP");
                        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                        conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                        conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                        conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

                        if (key.matches("^[0-9a-fA-F]+$")) {
                            conf.wepKeys[0] = key;
                        } else {
                            conf.wepKeys[0] = "\"".concat(key).concat("\"");
                        }

                        conf.wepTxKeyIndex = 0;

                    } else if (((EditText) findViewById(R.id.encryption)).getText().toString().contains("WPA")) {
                        Log.v("rht", "Configuring WPA");

                        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                        conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

                        conf.preSharedKey = "\"" + key + "\"";

                    } else {
                        Log.v("rht", "Configuring OPEN network");
                        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                        conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                        conf.allowedAuthAlgorithms.clear();
                        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                        conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                    }

                    int netId = wifiManager.addNetwork(conf);
                    if (netId == -1) {
                        netId = getExistingNetworkId(conf.SSID);
                    }

                    wifiManager.disconnect();
                    wifiManager.enableNetwork(netId, true);
                    wifiManager.reconnect();

                    final ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    if(connectivityManager!=null) {
                        final AlertDialog.Builder builder;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder = new AlertDialog.Builder(TestClass.this, android.R.style.Theme_Material_Dialog_Alert);
                        } else {
                            builder = new AlertDialog.Builder(TestClass.this);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final CountDownTimer countDownTimer = new CountDownTimer(10000, 1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                                        if (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            builder.setTitle("Connected to Wifi")
                                                                    .setMessage("Connected to the wifi")
                                                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                        }
                                                                    }).setIcon(android.R.drawable.ic_dialog_alert).show();
                                                        }
                                                    });
                                                }
                                            });
                                            cancel();
                                        }
                                    }

                                    @Override
                                    public void onFinish() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        builder.setTitle("Not connected to Wifi")
                                                                .setMessage("Connection impossible to the Wifi. Please, check if the network is already saved and remove it.")
                                                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                    }
                                                                }).setIcon(android.R.drawable.ic_dialog_alert).show();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                };
                                countDownTimer.start();
                            }
                        });
                    }
                }
            }
        });
        connectionThread.start();
    }

    private int getExistingNetworkId(String SSID) {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration existingConfig : configuredNetworks) {
                if (existingConfig.SSID.equals(SSID)) {
                    return existingConfig.networkId;
                }
            }
        }
        return -1;
    }

    private void closeWifiHotspotState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            reservation.close();
        }
        else{
            wifiManager.setWifiEnabled(true);
        }
    }

    private void openWifiHotspotState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            wifiManager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

                @Override
                public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                    super.onStarted(reservation);
                    System.out.println("HERE I AM");
                    Log.d(TAG, "Wifi Hotspot is on now");
                    TestClass.this.reservation = reservation;
                }

                @Override
                public void onStopped() {
                    super.onStopped();
                    Log.d(TAG, "onStopped: ");
                }

                @Override
                public void onFailed(int reason) {
                    super.onFailed(reason);
                    Log.d(TAG, "onFailed: ");
                }
            }, new Handler());
        }
        else{
            try {
                Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
                method.invoke(wifiManager, null, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_WIFI_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getWifiList();
                }
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    connectToWifi();
                }
        }
    }

    public class EchoWebSocketListener extends  WebSocketListener{
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            webSocket.send("Hello, it's SSaurel !");
            webSocket.send("What's up ?");
            webSocket.send(ByteString.decodeHex("deadbeef"));
            webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
        }
        @Override
        public void onMessage(WebSocket webSocket, String text) {
            System.out.println("Receiving : " + text);
        }
        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            System.out.println("Receiving bytes : " + bytes.hex());
        }
        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            System.out.println("Closing : " + code + " / " + reason);
        }
        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            System.out.println("Error : " + t.getMessage());
        }
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        return;
                    }
                    ((TextView)findViewById(R.id.response_server)).setText(username+" "+message);
                }
            });
        }
    };
}
