package ua.rostopira.virtualpointerserver;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

public class MainActivity extends AppCompatActivity {

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        if (prefs.getBoolean("onBoot",false)) {
            CheckBox cb = (CheckBox) findViewById(R.id.checkBox);
            cb.setChecked(true);
        }

        //Get input service. If fail - make app system
        S.get().injectionManager = new InjectionManager(this);
        if (!S.get().injectionManager.gotService) {
            makeAppSystem();
            return;
        }

        //Marshmallow permission system support
        if ( (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ) {
            requestOverlayPermission();
            return;
        }

        startService(new Intent(this, PointerService.class));
    }

    /**
     * This method moves this app to system using root permissions and then reboots.
     * Root required only on first start of the program or if new version installed.
     */
    public void makeAppSystem() {
        Log.e("makeAppSystem", "making app system");
        //TODO
    }

    public static int REQUEST_OVERLAY_PERMISSION = 7657;

    @TargetApi(23)
    public void requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION);
        } else
            startService(new Intent(this, PointerService.class));
    }

    @Override
    @TargetApi(23)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this))
                startService(new Intent(this, PointerService.class));
            //TODO: SYSTEM_ALERT_WINDOW permission denied
        }
    }

    public void cbClick(View v) {
        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        CheckBox cb = (CheckBox) findViewById(R.id.checkBox);
        prefs.edit().putBoolean("onBoot",cb.isChecked()).apply();
    }

}
