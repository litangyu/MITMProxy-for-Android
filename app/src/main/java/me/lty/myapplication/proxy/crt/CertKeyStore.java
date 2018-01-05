package me.lty.myapplication.proxy.crt;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

/**
 * Describe
 * <p>
 * Created on: 2017/12/27 下午12:16
 * Email: lty81372860@sina.com
 * <p>
 * Copyright (c) 2017 lty. All rights reserved.
 * Revision：
 *
 * @author lty
 * @version v1.0
 */
public class CertKeyStore {

    private final X509Certificate cert;
    private final KeyStore keyStore;
    private final PrivateKey privateKey;
    private final char[] storePass;

    public CertKeyStore(KeyStore keyStore, char[] storePass, X509Certificate cert, PrivateKey
            privateKey) {
        //判空校验
        this.keyStore = keyStore;
        this.storePass = storePass;
        this.cert = cert;
        this.privateKey = privateKey;
    }

    public final X509Certificate getCert() {
        return this.cert;
    }

    public final KeyStore getKeyStore() {
        return this.keyStore;
    }

    public final PrivateKey getPrivKey() {
        return this.privateKey;
    }

    public final char[] getStorePass() {
        return this.storePass;
    }

    public final byte[] getCertAsDer() throws CertificateEncodingException {
        return this.cert.getEncoded();
    }

    @Override
    public boolean equals(Object obj) {
        boolean equals;
        if (this == obj) {
            equals = true;
        }

        equals = obj instanceof CertKeyStore;
        if (!equals) {
            return false;
        }

        CertKeyStore objCertKeyStore = (CertKeyStore) obj;

        equals = this.keyStore.equals(objCertKeyStore.keyStore);
        if (!equals) {
            return false;
        }

        equals = this.storePass.equals(objCertKeyStore.storePass);
        if (!equals) {
            return false;
        }

        equals = this.cert.equals(objCertKeyStore.cert);
        if (!equals) {
            return false;
        }

        equals = this.privateKey.equals(objCertKeyStore.privateKey);
        if (!equals) {
            return false;
        }
        return equals;
    }

    @Override
    public int hashCode() {
        int i = 0;
        KeyStore keyStore = this.keyStore;
        int hashCode = (keyStore != null ? keyStore.hashCode() : 0) * 31;
        char[] cArr = this.storePass;
        hashCode = ((cArr != null ? Arrays.hashCode(cArr) : 0) + hashCode) * 31;
        X509Certificate x509Certificate = this.cert;
        int hashCode2 = ((x509Certificate != null ? x509Certificate.hashCode() : 0) + hashCode) *
                31;
        PrivateKey privateKey = this.privateKey;
        if (privateKey != null) {
            i = privateKey.hashCode();
        }
        return hashCode2 + i;
    }

    @Override
    public String toString() {
        return "CertKeyStore(keyStore=" + this.keyStore + ", storePass=" + this.storePass + ", " +
                "cert=" + this.cert + ", privKey=" + this.privateKey + ")";
    }
}
