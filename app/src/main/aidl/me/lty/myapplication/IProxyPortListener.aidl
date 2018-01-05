// IProxyPortListener.aidl
package me.lty.myapplication;

// Declare any non-default types here with import statements

interface IProxyPortListener {
    oneway void setProxyPort(int port);
}
