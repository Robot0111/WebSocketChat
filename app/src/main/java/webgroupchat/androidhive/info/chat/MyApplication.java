package webgroupchat.androidhive.info.chat;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;



import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import webgroupchat.androidhive.info.chat.Manaer.PrintfManager;
import webgroupchat.androidhive.info.chat.Utils.StaticVar;

public class MyApplication extends Application {

    ExecutorService cachedThreadPool = null;

    public ExecutorService getCachedThreadPool() {
        return cachedThreadPool;
    }


    private Handler handler = new Handler();

    public Handler getHandler() {
        return handler;
    }

    static MyApplication instance = null;

    public static MyApplication getInstance(){
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
//        CrashHandler crashHandler = CrashHandler.getInstance();
//        crashHandler.init(getApplicationContext());
        cachedThreadPool = Executors.newCachedThreadPool();
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
                int left = PrintfManager.getCenterLeft(72, bitmap);
                byte[] bytes = PrintfManager.bitmap2PrinterBytes(bitmap, left);
                StaticVar.bitmap_80 = bytes;
                left = PrintfManager.getCenterLeft(48,bitmap);
                bytes = PrintfManager.bitmap2PrinterBytes(bitmap, left);
                StaticVar.bitmap_58 = bytes;
            }
        });
    }
}
