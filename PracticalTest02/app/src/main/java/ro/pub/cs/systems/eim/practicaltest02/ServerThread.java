package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerThread extends Thread {

    private int port = 0;
    private ServerSocket serverSocket = null;
    private long time;
    HashMap<InetAddress, Long> vals;


    public ServerThread(int port) {
        this.port = port;
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
        time = -1;
        vals = new HashMap<InetAddress, Long>();
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setServerSocker(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }


    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Log.i(Constants.TAG, "[SERVER] Waiting for a connection...");
                Socket socket = serverSocket.accept();
                Log.i(Constants.TAG, "[SERVER] A connection request was received from " + socket.getInetAddress() + ":" + socket.getLocalPort());

                if (!vals.containsKey(socket.getInetAddress())) {
                    vals.put(socket.getInetAddress(), System.currentTimeMillis());
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet("http://www.timeapi.org/utc/now");
                    ResponseHandler<String> responseHandler = new BasicResponseHandler();

                    String content = "";
                    try {
                        content = httpClient.execute(httpGet, responseHandler);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    PrintWriter writer = Utilities.getWriter(socket);
                    writer.println(content);

                } else {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - vals.get(socket.getInetAddress()) >= 5000) {//Am setat timpul mai mic decat 1min pentru a se putea observa mai rapid functionalitatea.

                        HttpClient httpClient = new DefaultHttpClient();
                        HttpGet httpGet = new HttpGet("http://www.timeapi.org/utc/now");
                        ResponseHandler<String> responseHandler = new BasicResponseHandler();

                        String content = "";
                        try {
                            content = httpClient.execute(httpGet, responseHandler);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        PrintWriter writer = Utilities.getWriter(socket);
                        writer.println(content);
                        vals.put(socket.getInetAddress(), System.currentTimeMillis());

                    } else {
                        PrintWriter writer = Utilities.getWriter(socket);
                        writer.println("NOT NOW!");
                    }
                }

                socket.close();
            }
        } catch (ClientProtocolException clientProtocolException) {
            Log.e(Constants.TAG, "An exception has occurred: " + clientProtocolException.getMessage());
            if (Constants.DEBUG) {
                clientProtocolException.printStackTrace();
            }
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }

    public void stopThread() {
        if (serverSocket != null) {
            interrupt();
            try {
                serverSocket.close();
            } catch (IOException ioException) {
                Log.e(Constants.TAG, "An exception has occurred: " + ioException.getMessage());
                if (Constants.DEBUG) {
                    ioException.printStackTrace();
                }
            }
        }
    }

}