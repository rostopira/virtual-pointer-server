package ua.rostopira.virtualpointerserver;

import android.util.Log;
import java.io.DataOutputStream;
import java.io.IOException;

public class SU {
    private Process su;
    private DataOutputStream shell;

    public SU() {
        try{
            su = Runtime.getRuntime().exec("su");
            shell = new DataOutputStream(su.getOutputStream());
        } catch (IOException e) {
            su = null;
            Log.e("SU", "Failed");
        }
    }

    public void DO(String s) {
        if (isPermitted())
            try {
                shell.writeBytes(s+"\n");
                shell.flush();
            } catch (IOException e) {
                Log.e("SU.DO", "IOException");
            }
        else
            Log.e("SU.DO", "Denied");
    }

    public boolean isPermitted() {
        return su != null;
    }
}
