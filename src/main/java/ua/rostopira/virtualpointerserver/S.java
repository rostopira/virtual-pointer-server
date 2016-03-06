package ua.rostopira.virtualpointerserver;

import android.app.Application;
import android.graphics.Point;

public class S extends Application { // Singleton
    private static S instance;
    public String longPress = " 500";
    public Point screenSize;
    public PointerService pointerService;

    public S() {
        super();
        instance = this;
    }

    // Double-checked singleton fetching
    public static S get() {
        if(instance == null) {
            synchronized(S.class) {
                if(instance == null) new S();
            }
        }
        return instance;
    }
    
    @Override
    public void onCreate()
    {
        super.onCreate();
    }
}

