package ua.rostopira.virtualpointerserver;

import android.os.AsyncTask;
import android.util.Log;
import android.view.KeyEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPListener extends AsyncTask<Integer, String, Void> {
    boolean running, pressed;
    int x,y,h,w;

    void calculatexy(String[] s) {
        x += (int) Math.round(w*Math.sin(Double.parseDouble(s[1])));
        x = (x < 0) ? 0 : (x > w) ? w : x;
        y += (int) Math.round(h*Math.sin(Double.parseDouble(s[2])));
        y = (y < 0) ? 0 : (y > h) ? h : y;
    }

    @Override
    protected void onProgressUpdate(String... message) {
        Log.d("Message parser", "Got " + message[0]);
        switch (message[0].charAt(0)) {
            case 'M':
                calculatexy(message);
                S.get().pointerService.overlayView.Update(x,y);
                if (pressed)
                    S.get().injectionManager.injectMove(x,y);
                return;
            case 'T': //press
                S.get().injectionManager.injectTouchEventDown(x,y);
                pressed = true;
                return;
            case 'B': //back
                try { Runtime.getRuntime().exec("input keyevent 4"); }
                catch (IOException e) { Log.e("Message parser","I/O Exception"); }
                return;
            case 'H': //home
                S.get().injectionManager.injectKeyPress(KeyEvent.KEYCODE_HOME);
                return;
            case 'A': //app switch
                S.get().injectionManager.injectKeyPress(KeyEvent.KEYCODE_APP_SWITCH);
                return;
            case 'R': //release
                S.get().injectionManager.injectTouchEventRelease(x,y);
                pressed = false;
                return;
            case 'C':
                x = w / 2;
                y = h / 2;
                return;
            default: //wtf?
                Log.e("Message parser", "Message missed");
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        running = true;
        pressed = false;
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