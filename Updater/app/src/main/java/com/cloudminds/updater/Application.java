
package com.cloudminds.updater;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Logger.setDebugLogging(getResources().getBoolean(R.bool.debug_output));
    }
}
