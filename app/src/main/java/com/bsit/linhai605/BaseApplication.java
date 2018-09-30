package com.bsit.linhai605;

import android.annotation.SuppressLint;
import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.widget.Toast;

import com.bsit.linhai605.model.DaoMaster;
import com.bsit.linhai605.model.DaoSession;

public class BaseApplication extends Application {
    private static DaoSession daoSession;

    private static final Handler sHandler = new Handler();
    private static Toast sToast; // 单例Toast,避免重复创建，显示时间过长

    @SuppressLint("ShowToast")
    @Override
    public void onCreate() {
        super.onCreate();
        sToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        //配置数据库
        setupDatabase();
    }

    public static void toast(String txt, int duration) {
        sToast.setText(txt);
        sToast.setDuration(duration);
        sToast.show();
    }

    public static void runUi(Runnable runnable) {
        sHandler.post(runnable);
    }


    /**
     * 配置数据库
     */
    private void setupDatabase() {
        //创建数据库shop.db
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "linhai605.db", null);
        //获取可写数据库
        SQLiteDatabase db = helper.getWritableDatabase();
        //获取数据库对象
        DaoMaster daoMaster = new DaoMaster(db);
        //获取dao对象管理者
        daoSession = daoMaster.newSession();
    }

    public static DaoSession getDaoInstant() {
        return daoSession;
    }

}
