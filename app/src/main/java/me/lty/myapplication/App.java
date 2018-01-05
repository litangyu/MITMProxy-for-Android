package me.lty.myapplication;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

/**
 * Describe
 * <p>
 * Created on: 2017/12/27 下午8:05
 * Email: lty81372860@sina.com
 * <p>
 * Copyright (c) 2017 lty. All rights reserved.
 * Revision：
 *
 * @author lty
 * @version v1.0
 */
public class App extends MultiDexApplication{
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
