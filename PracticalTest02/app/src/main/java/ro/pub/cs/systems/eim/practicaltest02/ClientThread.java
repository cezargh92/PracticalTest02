package ro.pub.cs.systems.eim.practicaltest02;

import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import ro.pub.cs.systems.eim.practicaltest02.Constants;
import ro.pub.cs.systems.eim.practicaltest02.Utilities;

public class ClientThread extends Thread {

    private String address;
    private int port;
    private String city;
    private String informationType;
    private TextView timeTextView;
    private double time;

    private Socket socket;

    public ClientThread(
            String address,
            int port,
            TextView timeTv) {
        this.address = address;
        this.port = port;
        this.timeTextView = timeTv;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(address, port);
            if (socket == null) {
                Log.e(Constants.TAG, "[CLIENT THREAD] Could not create socket!");
            }

            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader != null && printWriter != null) {
                /*printWriter.println(city);
                printWriter.flush();
                printWriter.println(informationType);
                printWriter.flush();*/
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    final String serverTime = line;
                    timeTextView.post(new Runnable() {
                        @Override
                        public void run() {
                            timeTextView.setText(serverTime + "\n");
                        }
                    });
                }
            } else {
                Log.e(Constants.TAG, "[CLIENT THREAD] BufferedReader / PrintWriter are null!");
            }
            socket.close();
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[CLIENT THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
    }

}
