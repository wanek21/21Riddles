package martian.riddles.controllers;

import android.app.Application;
import android.content.Context;

public class GetContextClass extends Application { // класс исключительно для получения context

    private static Context context;

    public void onCreate() {
        super.onCreate();
        GetContextClass.context = getApplicationContext();
    }
    public static Context getContext() {
        return GetContextClass.context;
    }
}
