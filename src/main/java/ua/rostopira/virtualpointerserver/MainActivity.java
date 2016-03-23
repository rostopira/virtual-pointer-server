package ua.rostopira.virtualpointerserver;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
//import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

public class MainActivity extends Activity {
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        if (prefs.getBoolean("onBoot",false)) {
            CheckBox cb = (CheckBox) findViewById(R.id.checkBox);
            cb.setChecked(true);
        }

        //Marshmallow permission system support
        if ( (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) )
            requestOverlayPermission();
        else
            startService(new Intent(this, PointerService.class));
    }

    public void cbClick(View v) {
        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        CheckBox cb = (CheckBox) findViewById(R.id.checkBox);
        prefs.edit().putBoolean("onBoot",cb.isChecked()).apply();
    }

    /**
     * All code below for Marshmallow ONLY
     */

    private final static int REQUEST_OVERLAY_PERMISSION = 7657; //Random value in lower 16 bits

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
            else
                Toast.makeText(this, "Really? Permission denied? Fuck yourself then.", Toast.LENGTH_LONG).show();
        }
    }

}
