/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MITM;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Pradeep
 */
public class KeyToolSSLContext {
    public String CARootPath = "Certgen//CARoot//Catalyst.jks";
    public String TrustRootPath = "Certgen//TrustRoot//";
    public String Password = "lasttime";
    public String CARootKeyStorePath;
    public File CARootKeyStoreFile = new File("Certgen//CARoot//Catalyst.jks");
    public File TrustStoreFile = new File("Certgen//TrustRoot//");
    
    
    public String GetFilePathForKeystore(String Alias)
    {
       this.keytoolargs("keytool", "-genkey", "-alias", Alias, "-keysize",
                "4096", "-validity", "36500", "-keyalg", "RSA", "-dname",
                "CN=Catalyst, OU=Catalyst, O=Catalyst, L=Banglore, S=Karnataka", "-keypass", Password, "-storepass",
                Password, "-keystore", CARootKeyStoreFile.getAbsolutePath());
       return CARootKeyStoreFile.getAbsolutePath();
    }
    public String GetFilePathForTrustStore(String Domain)
    {
        this.keytoolargs("keytool", "-genkey", "-alias", Domain, "-keysize",
                "4096", "-validity", "36500", "-keyalg", "RSA", "-dname",
                "CN="+Domain+", OU=Catalyst, O=Catalyst, L=Banglore, S=Karnataka", "-keypass", Password, "-storepass",
                Password, "-keystore", TrustStoreFile.getAbsolutePath()+"//"+Domain);
        String path = TrustStoreFile.getAbsolutePath()+"//"+Domain;
        return path;
    }
    
    
    public void  keytoolargs(String... args)
    {  
        ProcessBuilder pb = new ProcessBuilder(args);
        try{
            Process p = pb.start();
             final InputStream is = p.getInputStream();
            final String data = IOUtils.toString(is);
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, UnrecoverableKeyException
    {
        KeyToolSSLContext context = new KeyToolSSLContext();
        String keypath = context.GetFilePathForKeystore("Catalyst");
        String Trustpath = context.GetFilePathForTrustStore("www.telerik.com");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        KeyStore ks =KeyStore.getInstance( KeyStore.getDefaultType());
        ks.load(new FileInputStream(keypath), "lasttime".toCharArray());
        
        //System.out.println(ks.getCertificate("Catalyst"));
        
        KeyStore ks2 = KeyStore.getInstance(KeyStore.getDefaultType());
        ks2.load(new FileInputStream(Trustpath),"lasttime".toCharArray());
        final Certificate[] chain = new Certificate[2];
       // kmf.init(ks2, "lasttime".toCharArray());
        chain[0]= ks.getCertificate("Catalyst");
        chain[1]= ks2.getCertificate("www.telerik.com");
        ks.setKeyEntry("Catalyst", ks.getKey("Catalyst", "lasttime".toCharArray()), "lasttime".toCharArray(), chain);
        kmf.init(ks, "lasttime".toCharArray());
        
        for(Certificate cert:ks.getCertificateChain("Catalyst"))
        {
            System.out.println(cert);
        }
        SSLContext sslcontext = SSLContext.getInstance("TLS");
        
    }
}
