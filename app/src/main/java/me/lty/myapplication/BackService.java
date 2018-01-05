package me.lty.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import me.lty.myapplication.proxy.exception.HttpProxyExceptionHandle;
import me.lty.myapplication.proxy.intercept.HttpProxyIntercept;
import me.lty.myapplication.proxy.intercept.HttpProxyInterceptInitializer;
import me.lty.myapplication.proxy.intercept.HttpProxyInterceptPipeline;
import me.lty.myapplication.proxy.server.HttpProxyServer;

/**
 * Describe
 * <p>
 * Created on: 2017/12/25 上午11:52
 * Email: lty81372860@sina.com
 * <p>
 * Copyright (c) 2017 lty. All rights reserved.
 * Revision：
 *
 * @author lty
 * @version v1.0
 */
public class BackService extends Service{

    private ServerThread thread;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        thread = new ServerThread();
        thread.start();
        String defaultType = KeyStore.getDefaultType();
        Log.e("back service",defaultType);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        thread.interrupt();
    }

    class ServerThread extends Thread{
        @Override
        public void run() {
            HttpProxyServer proxyServer = new HttpProxyServer(getApplicationContext())
                    .proxyInterceptInitializer(new HttpProxyInterceptInitializer() {
                        @Override
                        public void init(HttpProxyInterceptPipeline pipeline) {
                            pipeline.addLast(new HttpProxyIntercept() {
                                @Override
                                public void beforeRequest(Channel clientChannel, HttpRequest
                                        httpRequest, HttpProxyInterceptPipeline pipeline) throws
                                        Exception {
                                    //替换UA，伪装成手机浏览器
                                    httpRequest.headers().set(
                                            HttpHeaderNames.USER_AGENT,
                                            "Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) " +
                                                    "AppleWebKit/601.1.46 (KHTML, like Gecko) " +
                                                    "Version/9.0 Mobile/13B143 Safari/601.1"
                                    );
                                    //转到下一个拦截器处理
                                    pipeline.beforeRequest(clientChannel, httpRequest);
                                }

                                @Override
                                public void afterResponse(Channel clientChannel, Channel proxyChannel,
                                                          HttpContent httpContent,
                                                          HttpProxyInterceptPipeline pipeline) throws
                                        Exception {

                                    //拦截响应，添加一个响应头
                                    pipeline.getHttpRequest().headers().add("intercept", "test");
                                    //转到下一个拦截器处理
                                    pipeline.afterResponse(clientChannel, proxyChannel, httpContent);
                                }
                            });
                        }
                    })
                    .httpProxyExceptionHandle(new HttpProxyExceptionHandle() {

                        @Override
                        public void beforeCatch(Channel clientChannel, Throwable cause) {
                            System.out.println("111111111111111");
                            super.beforeCatch(clientChannel, cause);
                        }

                        @Override
                        public void afterCatch(Channel clientChannel, Channel proxyChannel, Throwable
                                cause) {
                            System.out.println("22222222222222");
                            super.afterCatch(clientChannel, proxyChannel, cause);
                        }
                    });
            proxyServer.start(9999);
        }
    }
}
