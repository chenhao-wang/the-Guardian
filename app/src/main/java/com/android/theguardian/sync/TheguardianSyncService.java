package com.android.theguardian.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class TheguardianSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static TheguardianSyncAdapter sTheguardianSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("TheguardianSyncService", "onCreate - TheguardianSyncService");
        synchronized (sSyncAdapterLock) {
            if (sTheguardianSyncAdapter == null) {
                sTheguardianSyncAdapter = new TheguardianSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sTheguardianSyncAdapter.getSyncAdapterBinder();
    }
}
