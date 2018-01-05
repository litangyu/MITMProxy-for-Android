package me.lty.myapplication.proxy.crt;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * Describe
 * <p>
 * Created on: 2017/12/27 下午7:02
 * Email: lty81372860@sina.com
 * <p>
 * Copyright (c) 2017 lty. All rights reserved.
 * Revision：
 *
 * @author lty
 * @version v1.0
 */
public class CACertGenerator {

    private static final CACertGenerator ourInstance = new CACertGenerator();

    static CACertGenerator getInstance() {
        return ourInstance;
    }

    private CACertGenerator() {

    }

    public final String makeStoreFileName(Context context) {
        return new File(context.getFilesDir(), "castore").getAbsolutePath();
    }

    public final void save(Context context, PrivateKey privKey, X509Certificate cert) {
        String storeFileName = makeStoreFileName(context);

        char[] password = "password".toCharArray();
        char[] keypass = "keypass".toCharArray();
        save(storeFileName, password, "alias", keypass, privKey, cert);
    }

    private KeyStore save(String storeFileName, char[] storePass, String alias, char[]
            keyPass, PrivateKey privKey, X509Certificate cert) {

        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, storePass);
            keyStore.setKeyEntry(alias, privKey, keyPass, new Certificate[]{cert});
            FileOutputStream fout = new FileOutputStream(storeFileName);
            keyStore.store(fout, storePass);
            fout.close();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }

        return keyStore;
    }

    public final boolean isCertInstalled(CertKeyStore fsCert) {
        if (fsCert == null) {
            return false;
        }
        KeyStore ks;
        try {
            ks = KeyStore.getInstance("AndroidCAStore");
            ks.load(null, null);
            Enumeration e = ks.aliases();

            while (e.hasMoreElements()) {
                String alias = (String) e.nextElement();
                java.security.cert.Certificate installedCert = ks.getCertificate(alias);
                if (installedCert != null && (installedCert instanceof X509Certificate)) {
                    Log.i("SSL", alias);
                    Log.i("SSL", ((X509Certificate) installedCert).getSubjectDN().getName());
                    Log.i(
                            "SSL",
                            ((X509Certificate) installedCert).getSubjectX500Principal().getName()
                    );
                    if (Arrays.equals(
                            ((X509Certificate) installedCert).getSignature(),
                            fsCert.getCert().getSignature()
                    )) {
                        Log.i("SSL", "signature match");
                        return true;
                    }
                }
            }
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        }

        Log.i("SSL", "no matching signagure");
        return false;
    }

    private CertKeyStore loadFsCert(String storeFileName) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(storeFileName);
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            if (keyStore == null) {
                return null;
            }
            char[] toCharArray = "password".toCharArray();
            keyStore.load(fileInputStream, toCharArray);
            toCharArray = "keypass".toCharArray();
            PrivateKey privKey = (PrivateKey) keyStore.getKey("alias", toCharArray);
            X509Certificate cert = null;
            try {
                cert = (X509Certificate) keyStore.getCertificate("alias");
                fileInputStream.close();
            } catch (IOException e5) {
                e5.printStackTrace();
            }
            if (privKey == null || cert == null) {
                return null;
            }
            char[] toCharArray2 = "password".toCharArray();
            return new CertKeyStore(keyStore, toCharArray2, cert, privKey);
        } catch (Exception e6) {
            e6.printStackTrace();
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
