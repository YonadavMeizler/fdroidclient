package org.fdroid.fdroid.net;
import android.content.Context;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class SelfSignSslFactory {

    private final String fileName = "ca.crt";
    private SSLContext sslContext = null;

    public SelfSignSslFactory(@NonNull Context context){
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            InputStream is = context.getResources().getAssets().open(fileName);
            InputStream caInput = new BufferedInputStream(is);
            Certificate ca;
            try{
                ca = certificateFactory.generateCertificate(caInput);
            }
            finally {
                caInput.close();
            }
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            sslContext = sslContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null );
        }
        catch(CertificateException | KeyStoreException | IOException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    public SSLSocketFactory getSocketFactory(){
        if (sslContext == null){
            return null;
        }
        else {
            return sslContext.getSocketFactory();
        }

    }

}
