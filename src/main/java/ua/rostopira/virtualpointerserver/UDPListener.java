package ua.rostopira.virtualpointerserver;

import android.os.AsyncTask;
import android.util.Log;
import android.view.KeyEvent;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPListener extends AsyncTask<Integer, String, Void> {
    boolean running, pressed;
    long lastTimeStamp = 0;
    int x,y,h,w;

    void calculatexy(String[] s) {
        x = (int) Math.round((w/2) * (1 + Math.sin(Double.parseDouble(s[1]))));
        y = (int) Math.round((h/2) * (1 + Math.sin(Double.parseDouble(s[2]))));
    }

    @Override
    protected void onProgressUpdate(String... message) {
        if (message[0].compareTo("M")==0) { //Move cursor
            calculatexy(message);
            String temp = message[3].replace("\n",""); //TODO: clean this mess with "\n"
            long ts = Long.parseLong(temp);
            if (ts>lastTimeStamp) {
                lastTimeStamp = ts;
                S.get().pointerService.Update(x,y);
            }
            if (pressed)
                S.get().injectionManager.injectMove(x,y);
            return;
        }

        Log.d("Message parser", "Got " + message[0]);
        switch (message[0].charAt(0)) {
            case 'T': //press
                S.get().injectionManager.injectTouchEventDown(x,y);
                pressed = true;
                return;
            case 'B': //back
                S.get().injectionManager.injectKeyPress(KeyEvent.KEYCODE_BACK);
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