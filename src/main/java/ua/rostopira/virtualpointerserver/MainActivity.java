package ua.rostopira.virtualpointerserver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    }

    public void cbClick(View v) {
        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        CheckBox cb = (CheckBox) findViewById(R.id.checkBox);
        prefs.edit().putBoolean("onBoot",cb.isChecked()).apply();
    }

    public void bClick(View v) {
        if (Singleton.getInstance().pointerService==null)
            startService(new Intent(this, PointerService.class));
        else stopService(new Intent(this, PointerService.class));
    }

}
