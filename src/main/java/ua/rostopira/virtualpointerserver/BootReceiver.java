package ua.rostopira.virtualpointerserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ( Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) &&
             context.getSharedPreferences("settings", Context.MODE_PRIVATE).getBoolean("onBoot",false) )
                 context.startService(new Intent(context, PointerService.class));
    }
}
