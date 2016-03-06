package ua.rostopira.virtualpointerserver;

import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPListener extends AsyncTask<Integer, String, Void> {
    private int x,y,h,w;
    private boolean running;
    private String xy;

    private void calculatexy(String[] s) {
        x += (int) Math.round(w*Math.sin(Double.parseDouble(s[1])));
        x = (x < 0) ? 0 : (x > w) ? w : x;
        y += (int) Math.round(h*Math.sin(Double.parseDouble(s[2])));
        y = (y < 0) ? 0 : (y > h) ? h : y;
        xy = ' ' + String.valueOf(x) + ' ' + String.valueOf(y);
    }

    private void SUInput(String s) {
        try {
            Runtime.getRuntime().exec( new String[] {"su", "-c", s } );
        } catch (IOException e) {
            Log.e("Message parser","I/O Exception");
            Log.e("Message:", s);
        }
    }

    @Override
    protected void onProgressUpdate(String... message) {
        Log.d("Message parser", "Got " + message[0]);
        switch (message[0].charAt(0)) {
            case 'M':
                calculatexy(message);
                S.get().pointerService.overlayView.Update(x,y);
                return;
            case 'T': //press
                SUInput("input tap" + xy);
                return;
            case 'L': //longpress
                SUInput("input swipe" + xy + xy + S.get().longPress);
                return;
            case 'C': //center
                x = w / 2;
                y = h / 2;
                return;
            case 'K': //custom key
                SUInput("input keyevent " + message[1]);
                return;
            default: //wtf?
                Log.e("Message parser", "Message missed: " + message[0]);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        running = true;
        h = S.get().screenSize.y;
        w = S.get().screenSize.x;
        x = w / 2;
        y = h / 2;
    }

    protected void stop() {
        running = false;
    }

    @Override
    protected Void doInBackground(Integer... parameter) {
        try {
            DatagramSocket dsocket = new DatagramSocket(parameter[0].intValue());
            byte[] buffer = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (running) {
                dsocket.receive(packet);
                String msg = new String(buffer, 0, packet.getLength());
                publishProgress(msg.split(" "));
                packet.setLength(buffer.length);
            }
        } catch (Exception e) {
            Log.e("UDPListener", "Inet error");
        }
        return null;
    }
}