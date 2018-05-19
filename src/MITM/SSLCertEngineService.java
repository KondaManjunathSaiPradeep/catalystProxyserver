/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MITM;

import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import org.bouncycastle.operator.OperatorCreationException;

/**
 *
 * @author Pradeep
 */
public interface SSLCertEngineService {
    
    
    public SSLContext InitializeSSLContext();
    
    public SSLSocket GetRemoteSSLSocket(String RemoteHost,String RemotePort);
    public SSLSocket ConvertClientToSSLSocket(Socket socket);
     public SSLSocket ConvertClientToSSLSocket(Socket socket,String domain)throws NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, NoSuchProviderException, OperatorCreationException, InvalidKeyException, SignatureException, KeyManagementException ;
    public SSLEngine getSSLEngine();
    public TrustManager trustallTrustManager();
    
    
}
