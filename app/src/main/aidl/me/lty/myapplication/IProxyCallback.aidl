// IProxyCallback.aidl
package me.lty.myapplication;

// Declare any non-default types here with import statements

interface IProxyCallback {
    oneway void getProxyPort(IBinder callback);
}
