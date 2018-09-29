package com.bsit.linhai605;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.bsit.linhai605.model.DaoMaster;
import com.bsit.linhai605.model.DaoSession;

public class BaseApplication extends Application {
    private static DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();

        //配置数据库
        setupDatabase();
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
