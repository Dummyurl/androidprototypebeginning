package amiin.bazouk.application.com.localisationdemo;

import android.support.annotation.NonNull;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

public class User implements Comparable<User>{

    private static final int PORT_SERVER_SOCKET = 8080;
    private String username;
    private MarkerSold markerBought;
    private MarkerSold markerSold;
    private double earnings;
    private double expenses;
    private Socket tcpClient;
    private ServerSocket tcpServerSocket;
    private WebSocketClient webSocketClient;
    private String message;

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

    public String getMessage() {
        return message;
    }

    public void setMarkerBought(MarkerSold markerBought)
    {
        this.markerBought = markerBought;
    }

    public void setMarkerSold(MarkerSold markerSold)
    {
        this.markerSold = markerSold;
    }

    public void setEarnings(double earnings)
    {
        this.earnings = earnings;
    }

    public void setExpenses(double expenses)
    {
        this.expenses= expenses;
    }

    public void soustractEarnings(double earnings)
    {
        this.earnings -= earnings;
    }

    public void soustractExpenses(double expenses)
    {
        this.expenses -= expenses;
    }

    //TCP part

    public ServerSocket getTCPServerSocket()
    {
        return tcpServerSocket;
    }

    public void setTCPClient(final ServerSocket tcpServerSocket)
    {
        Thread clientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    tcpClient = new Socket();
                    tcpClient.connect(tcpServerSocket.getLocalSocketAddress());
                    InputStream is = tcpClient.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    message = br.readLine();
                    System.out.println("Message received from the server : " +message);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i("received response", e.getMessage());
                }
            }
        });
        clientThread.start();
    }

    public void setTCPServer(final String ssid, final String password)
    {
        try {
            tcpServerSocket = new ServerSocket(PORT_SERVER_SOCKET);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread threadServerListening = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!tcpServerSocket.isClosed()) {
                    try {
                        Socket socket = tcpServerSocket.accept();
                        String returnMessage = ssid+","+password;
                        //Sending the response back to the client.
                        OutputStream os = socket.getOutputStream();
                        OutputStreamWriter osw = new OutputStreamWriter(os);
                        BufferedWriter bw = new BufferedWriter(osw);
                        bw.write(returnMessage);
                        System.out.println("Message sent to the client is "+returnMessage);
                        bw.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        threadServerListening.start();
    }

    public void closeTCPServer()
    {
        Thread closeServerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    tcpServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        closeServerThread.start();
    }

    public void closeTCPClient()
    {
        Thread closeClientThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    tcpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        closeClientThread.start();
    }
    //TCP part*

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
                        webSocketClient.send("Opening");
                    }

                    @Override
                    public void onMessage(String s) {
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
                //System.out.println("NOT CONNECTED AT ALL: "+webSocketClient.getConnection().isConnecting());
                webSocketClient.connect();
                //webSocketClient.send("HELLO");
                //System.out.println("CONNECTED AT ALL: "+webSocketClient.getConnection().isConnecting());
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

