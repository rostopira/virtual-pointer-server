package ua.rostopira.virtualpointerserver;

import android.app.Application;
import android.graphics.Point;

/* @project
 *
 * License to access, copy or distribute this file.
 * This file or any portions of it, is Copyright (C) 2012, Radu Motisan ,  http://www.pocketmagic.net . All rights reserved.
 * @author Radu Motisan, radu.motisan@gmail.com
 *
 * This file is protected by copyright law and international treaties. Unauthorized access, reproduction
 * or distribution of this file or any portions of it may result in severe civil and criminal penalties.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * @purpose
 * Cursor Overlay Sample
 * (C) 2012 Radu Motisan , all rights reserved.
 */

public class S extends Application {
    private static S m_Instance;
    public String longPress = "500";
    public Point screenSize;
    public PointerService pointerService;
    public InjectionManager injectionManager;

    public S() {
        super();
        m_Instance = this;
    }
    // Double-checked singleton fetching
    public static S get() {
        if(m_Instance == null) {
            synchronized(S.class) {
                if(m_Instance == null) new S();
            }
        }
        return m_Instance;
    }
    @Override
    public void onCreate()
    {
        super.onCreate();
    }
}

