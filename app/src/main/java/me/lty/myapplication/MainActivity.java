package me.lty.myapplication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.security.KeyChain;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.CertificateEncodingException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocketFactory;

import me.lty.myapplication.proxy.crt.CertKeyStore;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private CertKeyStore mCertKeyStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button start = findViewById(R.id.btn_start);
        Button stop = findViewById(R.id.btn_stop);
        Button bind = findViewById(R.id.btn_bind);
        Button unbind = findViewById(R.id.btn_unbind);
        Button test = findViewById(R.id.btn_test);
        Button socket = findViewById(R.id.btn_test_ssl_socket);
        Button serverSocket = findViewById(R.id.btn_test_ssl_server_socket);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        bind.setOnClickListener(this);
        unbind.setOnClickListener(this);
        test.setOnClickListener(this);
        socket.setOnClickListener(this);
        serverSocket.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        intent = new Intent(this, BackService.class);

        switch (v.getId()) {
            case R.id.btn_test:
                //new Thread(new Runnable() {
                //    @Override
                //    public void run() {
                //        try {
                //            URL url = new URL("https://www.baidu.com");
                //            HttpsURLConnection urlConnection = (HttpsURLConnection) url
                //                    .openConnection();
                //            InputStream in = urlConnection.getInputStream();
                //            byte[] bytes = new byte[1024];
                //            int read = in.read(bytes, 0, bytes.length);
                //            Log.d("TLS", Arrays.toString(bytes));
                //        } catch (Exception e) {
                //            e.printStackTrace();
                //        }
                //    }
                //}).start();
                break;
            case R.id.btn_test_ssl_socket:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            testSSLSocket();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            case R.id.btn_test_ssl_server_socket:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            testSSLServerSocket();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            case R.id.btn_start:
                startService(intent);
                break;
            case R.id.btn_stop:
                stopService(intent);
                break;
            case R.id.btn_bind:
                bindService(intent, mProxyConnection,
                            Context.BIND_AUTO_CREATE | Context.BIND_NOT_FOREGROUND
                );
                break;
            case R.id.btn_unbind:
                if (mBound) {
                    unbindService(mProxyConnection);
                    mBound = false;
                }
                break;
            case R.id.btn_status:
                byte[] certAsDer = null;
                try {
                    certAsDer = this.mCertKeyStore.getCertAsDer();
                } catch (CertificateEncodingException e) {
                    e.printStackTrace();
                }
                if (certAsDer == null) {
                    Log.i(TAG, " certAsDer = this.mCertKeyStore.getCertAsDer(); is null");
                    break;
                }
                Intent caIntent = KeyChain.createInstallIntent();
                caIntent.putExtra("name", "Packet Capture CA Certificate");
                caIntent.putExtra("CERT", certAsDer);
                try {
                    startActivityForResult(caIntent, 0);
                } catch (Exception e) {
                    Toast.makeText(
                            this,
                            "Certificate installer is missing on this device",
                            Toast.LENGTH_SHORT
                    ).show();
                }
                break;
            default:
                break;
        }
    }

    private void testSSLServerSocket() throws Exception {
        init();
        start();
    }

    private static final int SERVER_PORT = 50030;
    private static final String SERVER_KEY_PASSWORD = "password";
    private static final String SERVER_AGREEMENT = "TLS";//使用协议
    private static final String SERVER_KEY_MANAGER = "X509";//密钥管理器
    private static final String SERVER_KEY_KEYSTORE = "BKS";//密库，这里用的是Java自带密库
    private SSLServerSocket serverSocket;

    //由于该程序不是演示Socket监听，所以简单采用单线程形式，并且仅仅接受客户端的消息，并且返回客户端指定消息
    public void start() {
        if (serverSocket == null) {
            Log.d(TAG,"ERROR");
            return;
        }
        while (true) {
            try {
                Log.d(TAG,"Server Side......");
                Socket s = serverSocket.accept();
                InputStream input = s.getInputStream();
                OutputStream output = s.getOutputStream();

                BufferedInputStream bis = new BufferedInputStream(input);
                BufferedOutputStream bos = new BufferedOutputStream(output);

                byte[] buffer = new byte[20];
                bis.read(buffer);
                Log.d(TAG,new String(buffer));

                bos.write("This is Server".getBytes());
                bos.flush();

                s.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void init() {
        try {
            //取得SSLContext
            SSLContext ctx = SSLContext.getInstance(SERVER_AGREEMENT);
            //取得SunX509私钥管理器
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(SERVER_KEY_MANAGER);
            //取得JKS密库实例
            KeyStore ks = KeyStore.getInstance(SERVER_KEY_KEYSTORE);
            //加载服务端私钥
            ks.load(getAssets().open("server.bks"), SERVER_KEY_PASSWORD.toCharArray());
            //初始化
            kmf.init(ks, SERVER_KEY_PASSWORD.toCharArray());
            //初始化SSLContext
            ctx.init(kmf.getKeyManagers(),null, null);
            //通过SSLContext取得ServerSocketFactory，创建ServerSocket
            serverSocket = (SSLServerSocket) ctx.getServerSocketFactory().createServerSocket(SERVER_PORT);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void testSSLSocket() throws Exception {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, null, null);

        // 监听和接收客户端连接
        SSLSocketFactory factory = context.getSocketFactory();
        Socket socket = factory.createSocket("127.0.0.1", 9999);
        ObjectOutputStream stream = new ObjectOutputStream(socket.getOutputStream());
        stream.writeObject("Client Hello");
        stream.flush();
        stream.close();
    }

    private boolean mBound;

    private ServiceConnection mProxyConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName component) {
            mBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName component, IBinder binder) {
            IProxyCallback callbackService = IProxyCallback.Stub.asInterface(binder);
            if (callbackService != null) {
                try {
                    callbackService.getProxyPort(new IProxyPortListener.Stub() {

                        @Override
                        public void setProxyPort(final int port) throws RemoteException {
                            if (port != -1) {
                                Log.d(TAG, "Local proxy is bound on " + port);
                            } else {
                                Log.e(TAG, "Received invalid port from Local Proxy,"
                                        + " PAC will not be operational");
                            }
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            mBound = true;
        }
    };
}
