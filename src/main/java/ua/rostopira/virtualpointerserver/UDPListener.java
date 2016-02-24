package ua.rostopira.virtualpointerserver;

import android.os.AsyncTask;
import android.util.Log;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPListener extends AsyncTask<Integer, String, Void> {
    boolean running;
    long lastTimeStamp = 0;
    int x,y,h,w;
    String xy = "";
    Runtime runtime;

    void shell(String c) {
        try {
            runtime.exec(c+"\n");
        } catch (Exception e) {
            Log.e("UDPListener", "Shell input failed");
        }
    }

    int range (double x, int r) {
        if (x<0)
            return 0;
        if (x>r)
            return r;
        return (int) Math.round(x);
    }

    void calculatexy(String[] s) {
        x = range( (w/2) * (1 + Math.sin(Double.parseDouble(s[1]))), w);
        y = range( (h/2) * (1 + Math.sin(Double.parseDouble(s[2]))), h);
        xy = " "+Integer.toString(x)+" "+Integer.toString(y);
    }

    @Override
    protected void onProgressUpdate(String... message) {
        if (message[0].compareTo("M")==0) { //Move cursor
            calculatexy(message);
            String temp = message[3].replace("\n","");
            long ts = Long.parseLong(temp);
            if (ts>lastTimeStamp) {
                lastTimeStamp = ts;
                Singleton.getInstance().pointerService.Update(x,y);
            }
            return;
        }

        Log.d("UDPListener", "Got " + message[0]);
        if (message[0].compareTo("T")==0) //Tap
            shell("input tap" + xy);
        if (message[0].compareTo("L")==0) //Long press
            shell("input swipe" + xy + xy + " " + Singleton.getInstance().longPress);
        if (message[0].compareTo("B")==0) //Back press
            shell("input keyevent 4");
        if (message[0].compareTo("H")==0) //Home press
            shell("input keyevent 3");
        if (message[0].compareTo("RA")==0) //Recent apps press
            shell("input keyevent 187");
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        running = true;
        h = Singleton.getInstance().screenSize.y;
        w = Singleton.getInstance().screenSize.x;
        runtime = Runtime.getRuntime();
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