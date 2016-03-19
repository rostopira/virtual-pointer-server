package ua.rostopira.virtualpointerserver;

import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Listens for commands from client (doInBackground) and does, what they say (onProgressUpdate)
 */
public class UDPListener extends AsyncTask<Void, String, Void> {
    private int x, y;

    @Override
    public void onPreExecute() {
        x = S.get().screenSize.x / 2;
        y = S.get().screenSize.y / 2;
    }

    @Override
    public Void doInBackground(Void... voids) {
        try {
            DatagramSocket socket = new DatagramSocket(S.port, InetAddress.getByName("0.0.0.0"));
            socket.setBroadcast(true);
            byte[] buffer = new byte[32]; //Bigger - just overkill. Biggest message - M with 2 float
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (!isCancelled()) {
                socket.receive(packet);
                String msg = new String(buffer, 0, packet.getLength());
                if (msg.charAt(0)=='B')
                    //Detected client broadcast. Answer
                    new DatagramSocket().send(
                        new DatagramPacket(
                            "VPS here!".getBytes(),
                            "VPS here!".length(),
                            packet.getAddress(), //Address of broadcasting client
                            S.port
                        )
                    );
                else
                    publishProgress(msg.split(" ")); //message parser
                packet.setLength(buffer.length);
            }
            socket.close();
        } catch (Exception e) {
            Log.e("UDPListener", "Inet error");
        }
        return null;
    }

    /**
     * Simple message parser. No encryption, or anything else
     * Just a letter and numbers. KISS
     */
    @Override
    protected void onProgressUpdate(String... message) {
        Log.d("Message parser", "Got " + message[0]);
        switch (message[0].charAt(0)) {
            case 'M': //move cursor
                x += (int) Math.round(S.get().screenSize.x*Math.sin(Double.parseDouble(message[1])));
                y += (int) Math.round(S.get().screenSize.y*Math.sin(Double.parseDouble(message[2])));
                S.get().overlayView.Update(x,y);
                return;
            case 'T': //tap
                SUInput("tap " + xy());
                return;
            case 'L': //longpress
                SUInput(String.format("swipe %s %s %d", xy(), xy(), S.get().longPress));
                return;
            case 'C': //center
                onPreExecute();
                return;
            case 'K': //any key
                SUInput("keyevent " + message[1]);
                return;
            default: //wtf?
                Log.e("Message parser", "Message missed: " + message[0]);
        }
    }

    /**
     * Formats x and y to string with space as seperator
     * Also, checks, if values in screen bounds.
     */
    private String xy() {
        int X = (x < 0) ? 0 : (x > S.get().screenSize.x) ? S.get().screenSize.x : x;
        int Y = (y < 0) ? 0 : (y > S.get().screenSize.y) ? S.get().screenSize.y : y;
        return String.format("%d %d", X, Y);
    }

    /**
     * The simplest way to inject input events using root.
     * No gestures support. But it's for TV! Who cares?
     * TODO: decrease executing delay
     */
    private void SUInput(String s) {
        try {
            Runtime.getRuntime().exec( new String[] {"su", "-c", "input " + s } );
        } catch (IOException e) {
            Log.e("SUInput","I/O Exception on " + s);
        }
    }
}