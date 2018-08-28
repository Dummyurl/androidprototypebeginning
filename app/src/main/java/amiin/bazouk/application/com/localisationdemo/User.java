package amiin.bazouk.application.com.localisationdemo;

import android.support.annotation.NonNull;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.net.URISyntaxException;

public class User implements Comparable<User>{
    private String username;
    private MarkerSold markerBought;
    private MarkerSold markerSold;
    private double earnings;
    private double expenses;
    private boolean isBuyOn = false;
    private WebSocketClient webSocketClient;

    User(String username)
    {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public double getEarnings()
    {
        return earnings;
    }

    public double getExpenses()
    {
        return expenses;
    }

    public MarkerSold getMarkerBought() {
        return markerBought;
    }

    public MarkerSold getMarkerSold() {
        return markerSold;
    }

    public boolean isBuyOn() {
        return isBuyOn;
    }

    public void setMarkerBought(MarkerSold markerBought)
    {
        this.markerBought = markerBought;
    }

    public void setMarkerSold(MarkerSold markerSold)
    {
        this.markerSold = markerSold;
    }

    public void setExpenses(double expenses)
    {
        this.expenses= expenses;
    }

    public void addEarnings(double valueSpent) {
        earnings+=valueSpent;
    }

    public void soustractEarnings(double earnings)
    {
        this.earnings -= earnings;
    }

    public void soustractExpenses(double expenses)
    {
        this.expenses -= expenses;
    }

    public void setBuyOn(boolean isBuyOn) {
        this.isBuyOn = isBuyOn;
    }

    //Web Socket Client part
    public void setWebSocketClient(final String serverAdress) {
        Thread clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                URI uri;
                try {
                    uri = new URI(serverAdress);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    return;
                }

                webSocketClient = new WebSocketClient(uri) {
                    @Override
                    public void onOpen(ServerHandshake serverHandshake) {
                        Log.i("Websocket", "Opened");
                        webSocketClient.send("Hello");
                    }

                    @Override
                    public void onMessage(String s) {
                        System.out.println("Message sent: "+s);
                        webSocketClient.send(s);
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

    public void closeWebSocketClient()
    {
        Thread closeClientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                webSocketClient.close();
            }
        });
        closeClientThread.start();
    }
    //Web Socket Client part*

    @Override
    public int compareTo(@NonNull User user) {
        if(username.equals(user.username))
        {
            return 0;
        }
        return 1;
    }
}

