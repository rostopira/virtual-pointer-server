package ua.rostopira.virtualpointerserver;

import android.os.AsyncTask;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPListener extends AsyncTask<Integer, String, Void> {

    boolean running;
    long lastTimeStamp = 0;
    int x,y,h,w;
    String xy = "";

    void shellInput(String c) {
        try {
            Runtime.getRuntime().exec("input "+c);
        } catch (Exception e) {}
    }

    int range (double x, int r) {
        if (x<0)
            return 0;
        else if (x>r)
            return r;
        else
            return (int) Math.round(x);
    }

    void calculatexy(String[] s) {
        x = range( (w/2) + (2 * Math.tan(Double.parseDouble(s[1]))) / w, w);
        y = range( (h/2) - (2 * Math.tan(Double.parseDouble(s[2]))) / h, h);
        xy = " "+Integer.toString(x)+" "+Integer.toString(y);
    }

    @Override
    protected void onProgressUpdate(String... message) {
        if (message[0].compareTo("M")==0) { //Move cursor
            calculatexy(message);
            long ts = Long.parseLong(message[3]);
            if (ts>lastTimeStamp) {
                lastTimeStamp = ts;
                Singleton.getInstance().pointerService.Update(x,y);
            }
        }
        if (message[0].compareTo("T")==0) //Tap
            shellInput("tap" + xy);
        if (message[0].compareTo("L")==0) //Long press
            shellInput("swipe" + xy + xy + " " + Singleton.getInstance().longPress);
        if (message[0].compareTo("B")==0) //Back press
            shellInput("keypress 4");
        if (message[0].compareTo("H")==0) //Home press
            shellInput("keypress 3");
        if (message[0].compareTo("RA")==0) //Recent apps press
            shellInput("keypress 187");
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        running = true;
        h = Singleton.getInstance().screenH;
        w = Singleton.getInstance().screenW;
    }

    @Override
    protected void onCancelled() {
        running = false;
        super.onCancelled();
    }

    @Override
    protected Void doInBackground(Integer... parameter) {
        //suShell = new SUShell();
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
        } catch (Exception e) {}
        return null;
    }
}