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
import android.net.TrafficStats;
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
import android.text.InputType;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class TestClass extends AppCompatActivity {

    private static final String TAG = "TEST ON HOTSPOT";
    private static final int PERMISSION_ACCESS_WIFI_REQUEST_CODE = 1000;
    private static final int PERMISSION_REQUEST_CODE = 10002;
    private WifiManager.LocalOnlyHotspotReservation reservation;
    private WifiManager wifiManager;
    private WifiManager mWifiManager;
    private List<ScanResult> wifiList;
    private SimpleAdapter adapterListWifis;
    private List<HashMap<String, String>> listMapOfEachWifi;
    private Handler mHandler = new Handler();
    private long mStartRX = 0;
    private long mStartTX = 0;
    private Runnable mRunnable;
    private OkHttpClient client;
    private WebSocketServer server;
    private WebSocket webSocket;
    private boolean isHotspotOn = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_class_activity);

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

        mRunnable = new Runnable() {
            public void run() {
                TextView RX = findViewById(R.id.rx);
                TextView TX = findViewById(R.id.tx);
                long rxBytes = TrafficStats.getTotalRxBytes() - mStartRX;
                RX.setText(Long.toString(rxBytes));
                long txBytes = TrafficStats.getTotalTxBytes() - mStartTX;
                TX.setText(Long.toString(txBytes));
                mHandler.postDelayed(mRunnable, 1000);
            }
        };

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
                isHotspotOn = false;
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

        findViewById(R.id.connect_socket).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               setWebSocketClient();
            }
        });

        findViewById(R.id.disconnect_socket).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        findViewById(R.id.send_client).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TestClass.this);
                builder.setTitle("Title");
                final EditText input = new EditText(TestClass.this);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        webSocket.send(input.getText().toString());
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
        });

        findViewById(R.id.send_server).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TestClass.this);
                builder.setTitle("Title");
                final EditText input = new EditText(TestClass.this);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (org.java_websocket.WebSocket webSocketofClients : server.connections()) {
                            webSocketofClients.send(input.getText().toString());
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                /*Iterator<org.java_websocket.WebSocket> it = server.connections().iterator();
                while(it.hasNext()){
                    it.next().send("HELLO CLIENT");
                }*/
            }
        });

        findViewById(R.id.show_volume_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStartRX = TrafficStats.getTotalRxBytes();
                mStartTX = TrafficStats.getTotalTxBytes();
                if (mStartRX == TrafficStats.UNSUPPORTED || mStartTX == TrafficStats.UNSUPPORTED) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(TestClass.this);
                    alert.setTitle("Uh Oh!");
                    alert.setMessage("Your device does not support traffic stat monitoring.");
                    alert.show();
                }
                else {
                    mHandler.postDelayed(mRunnable, 1000);
                }
            }
        });

        findViewById(R.id.start_server).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(server==null){
                    final AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(TestClass.this, android.R.style.Theme_Material_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(TestClass.this);
                    }
                    Thread serverThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            /*wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    openWifiHotspotState();
                                }
                            });*/

                            if(!isHotspotOn()){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        builder.setTitle("Turn on your hotspot")
                                                .setMessage("Turn on your hotspot")
                                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                    }
                                                }).setIcon(android.R.drawable.ic_dialog_alert).show();
                                    }
                                });
                                return;
                            }

                            if(!ipDevice()){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        builder.setTitle("Change your hotspot address")
                                                .setMessage("Change your hotspot address to the default address")
                                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                    }
                                                }).setIcon(android.R.drawable.ic_dialog_alert).show();
                                    }
                                });
                                return;
                            }
                            String ipAddress = "192.168.43.1";
                            InetSocketAddress inetSockAddress = new InetSocketAddress(ipAddress,38301);
                            server = new WebSocketServer(inetSockAddress){

                                @Override
                                public void onOpen(org.java_websocket.WebSocket conn, ClientHandshake handshake) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            builder.setTitle("New client connected")
                                                    .setMessage("New client connected")
                                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                        }
                                                    }).setIcon(android.R.drawable.ic_dialog_alert).show();
                                        }
                                    });
                                }

                                @Override
                                public void onClose(org.java_websocket.WebSocket conn, int code, String reason, boolean remote) {

                                }

                                @Override
                                public void onMessage(org.java_websocket.WebSocket conn, final String message) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            builder.setTitle("Message received from client to server")
                                                    .setMessage(message)
                                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                        }
                                                    }).setIcon(android.R.drawable.ic_dialog_alert).show();
                                        }
                                    });
                                }

                                @Override
                                public void onError(org.java_websocket.WebSocket conn, Exception ex) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            builder.setTitle("Connection failed")
                                                    .setMessage("Connection of client failed")
                                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                        }
                                                    }).setIcon(android.R.drawable.ic_dialog_alert).show();
                                        }
                                    });
                                }
                            };
                            server.start();
                        }
                    });
                    serverThread.start();
                }
            }
        });

        /*findViewById(R.id.get_ip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView)findViewById(R.id.ip_text)).setText(ipDevice());
            }
        });*/
    }

    private boolean isHotspotOn() {
        return new WifiApManager().isWifiApEnabled();
        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!new WifiApManager().isWifiApEnabled()){

                    return;
                }
            }
        });*/
    }

    private boolean ipDevice() {
        try {
            for(Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();en.hasMoreElements();){
                NetworkInterface intf = en.nextElement();
                for(Enumeration<InetAddress> enumIpAdress = intf.getInetAddresses();enumIpAdress.hasMoreElements();){
                    InetAddress inetAddress = enumIpAdress.nextElement();
                    if(inetAddress.getHostAddress().equals("192.168.43.1")){
                        return true;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void setWebSocketClient() {
        final AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(TestClass.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(TestClass.this);
        }
        Thread clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                client = new OkHttpClient();
                WebSocketListener webSocketListener = new WebSocketListener() {
                    private static final int NORMAL_CLOSURE_STATUS = 1000;
                    @Override
                    public void onOpen(WebSocket webSocket, Response response) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                builder.setTitle("Connected to server")
                                        .setMessage("Connected to the server")
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        }).setIcon(android.R.drawable.ic_dialog_alert).show();
                            }
                        });
                    }
                    @Override
                    public void onMessage(WebSocket webSocket, final String text) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                builder.setTitle("Message received from server to client")
                                        .setMessage(text)
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        }).setIcon(android.R.drawable.ic_dialog_alert).show();
                            }
                        });
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                builder.setTitle("Connection failed")
                                        .setMessage("Connection to server failed")
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        }).setIcon(android.R.drawable.ic_dialog_alert).show();
                            }
                        });
                    }
                };
                Request request = new Request.Builder().url("ws://192.168.43.1:38301").build();
                webSocket = client.newWebSocket(request, webSocketListener);
                client.dispatcher().executorService().shutdown();
            }
        });
        clientThread.start();
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
                                            builder.setTitle("Connected to Wifi")
                                                .setMessage("Connected to the wifi")
                                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                    }
                                                }).setIcon(android.R.drawable.ic_dialog_alert).show();
                                            cancel();
                                        }
                                    }

                                    @Override
                                    public void onFinish() {
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
                    isHotspotOn = true;
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
                isHotspotOn = true;
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

    enum WIFI_AP_STATE {
        WIFI_AP_STATE_DISABLING,
        WIFI_AP_STATE_DISABLED,
        WIFI_AP_STATE_ENABLING,
        WIFI_AP_STATE_ENABLED,
        WIFI_AP_STATE_FAILED
    }

    public class WifiApManager {
        private final WifiManager mWifiManager;

        public WifiApManager() {
            mWifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        }

        /*the following method is for getting the wifi hotspot state*/

        WIFI_AP_STATE getWifiApState() {
            try {
                Method method = mWifiManager.getClass().getMethod("getWifiApState");

                int tmp = ((Integer) method.invoke(mWifiManager));

                // Fix for Android 4
                if (tmp > 10) {
                    tmp = tmp - 10;
                }

                return WIFI_AP_STATE.class.getEnumConstants()[tmp];
            } catch (Exception e) {
                Log.e(this.getClass().toString(), "", e);
                return WIFI_AP_STATE.WIFI_AP_STATE_FAILED;
            }
        }

        /**
         * Return whether Wi-Fi Hotspot is enabled or disabled.
         *
         * @return {@code true} if Wi-Fi AP is enabled
         * @see #getWifiApState()
         */
        public boolean isWifiApEnabled() {
            return getWifiApState() == WIFI_AP_STATE.WIFI_AP_STATE_ENABLED;
        }
    }

}
