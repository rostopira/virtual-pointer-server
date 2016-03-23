package ua.rostopira.virtualpointerserver;

import android.os.AsyncTask;
import android.util.Log;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Listens for commands from client, (doInBackground) and pass them to Input Injector (onProgressUpdate)
 */
public class UDPListener extends AsyncTask<Void, String, Void> {
    private MessageParser injector;

    @Override
    public void onPreExecute() {
        injector = new MessageParser();
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


    @Override
    protected void onProgressUpdate(String... message) {
        injector.exec(message);
    }
}