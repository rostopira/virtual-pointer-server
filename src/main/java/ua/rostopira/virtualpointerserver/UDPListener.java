package ua.rostopira.virtualpointerserver;

import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Listens for commands from client and does, what they say
 * Maybe should move message parse and commands execution to another file?
 */
public class UDPListener extends AsyncTask<Void, String, Void> {
    private int x, y,
                h, w; //screen size

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        h = S.get().screenSize.y;
        w = S.get().screenSize.x;
        x = w / 2;
        y = h / 2;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (!isCancelled()) {
                socket.receive(packet);
                String msg = new String(buffer, 0, packet.getLength());
                if (msg.charAt(0)=='B') {
                    //Detected client broadcast. Let him know, about running server here
                    byte[] answer = "VPS here!".getBytes();
                    socket.send(new DatagramPacket(
                            answer,
                            answer.length,
                            packet.getAddress(),
                            S.port)
                    );
                } else
                    publishProgress(msg.split(" "));
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
            case 'M': //move
                x += (int) Math.round(w*Math.sin(Double.parseDouble(message[1])));
                y += (int) Math.round(h*Math.sin(Double.parseDouble(message[2])));
                S.get().pointerService.overlayView.Update(x,y);
                return;
            case 'T': //tap
                SUInput("tap " + xy());
                return;
            case 'L': //longpress
                SUInput(String.format("swipe %s %s %d", xy(), xy(), S.get().longPress));
                return;
            case 'C': //center
                x = w / 2;
                y = h / 2;
                return;
            case 'K': //custom key
                SUInput("keyevent " + message[1]);
                return;
            default: //wtf?
                Log.e("Message parser", "Message missed: " + message[0]);
        }
    }

    /**
     * Just to make code above more simple.
     * Formats x and y to string with space as seperator
     * Also, checks, if values in screen bounds.
     */
    private String xy() {
        int X = (x < 0) ? 0 : (x > w) ? w : x;
        int Y = (y < 0) ? 0 : (y > h) ? h : y;
        return String.format("%d %d", X, Y);
    }

    /**
     * The simplest way to inject input events using root.
     * No gestures support. But it's for TV! Who cares?
     */
    private void SUInput(String s) {
        try {
            Runtime.getRuntime().exec( new String[] {"su", "-c", "input " + s } );
        } catch (IOException e) {
            Log.e("Message parser","I/O Exception");
            Log.e("Message:", s);
        }
    }
}