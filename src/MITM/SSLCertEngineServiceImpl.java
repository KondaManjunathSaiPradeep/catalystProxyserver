/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MITM;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

/**
 *
 * @author Pradeep
 */
public class SSLCertEngineServiceImpl implements SSLCertEngineService {
     private final  AtomicLong serial; 
    private PrivateKey CAPrivateKey;
    private PublicKey CAPublicKey;
    private SSLContext contex;
    private SSLSocketFactory remotefactory;
    private KeyManagerFactory keyfactory;
    private X509Certificate CaCert;
    private SSLContext context;
   public SSLEngine GetEngineForDomain(String domain) throws KeyStoreException, IOException, CertificateException, UnrecoverableKeyException, KeyManagementException, NoSuchProviderException, OperatorCreationException, InvalidKeyException, SignatureException{
         try {
             KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
             BouncyCastle impl = new BouncyCastle();
             KeyStore store =impl.clientCertificateStore(domain);
             
             
             factory.init(store, "Jocundity123#".toCharArray());
             SSLContext context = SSLContext.getInstance("SSL");
             
             context.init(factory.getKeyManagers(),new TrustManager[]{ this.trustallTrustManager()}, null);
             return context.createSSLEngine();
         } catch (NoSuchAlgorithmException ex) {
             Logger.getLogger(SSLCertEngineServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
             return null;
         }
         
     }
    
    public SSLCertEngineServiceImpl() {
        Security.addProvider(new BouncyCastleProvider()); 
  final Random rnd = new Random(); 
  rnd.setSeed(System.currentTimeMillis()); 
  // prevent browser certificate caches, cause of doubled serial numbers 
  // using 48bit random number 
  long sl = ((long)rnd.nextInt()) << 32 | (rnd.nextInt() & 0xFFFFFFFFL); 
  // let reserve of 16 bit for increasing, serials have to be positive 
  sl = sl & 0x0000FFFFFFFFFFFFL; 
  this.serial = new AtomicLong(sl);
      this.contex=  this.InitializeSSLContext();
    }
    
    public static SSLCertEngineServiceImpl getInstance() {
        return SSLCertEngineServiceImplHolder.INSTANCE;
    }

    @Override
    public SSLContext InitializeSSLContext() {
      synchronized(this)
            {
        try {
            
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(new FileInputStream("CaCert//CatalystRoot.jks"), "Jocundity123#".toCharArray());
            this.keyfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keyfactory.init(keystore, "Jocundity123#".toCharArray());
      SSLContext context = SSLContext.getInstance("SSL");
      
      TrustManager tm = this.trustallTrustManager();
      context.init(keyfactory.getKeyManagers(), new TrustManager[]{tm}, null);
      this.CaCert = (X509Certificate) keystore.getCertificate("carootcatalyst");
       this.CAPrivateKey = (PrivateKey) keystore.getKey("carootcatalystprivatekey", "Jocundity123#".toCharArray());
       this.CAPublicKey = CaCert.getPublicKey();
     
     
      this.contex = context;
      return context;
        } catch (KeyStoreException ex) {
            Logger.getLogger(SSLCertEngineServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SSLCertEngineServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (IOException ex) {
            Logger.getLogger(SSLCertEngineServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(SSLCertEngineServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (CertificateException ex) {
            Logger.getLogger(SSLCertEngineServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (UnrecoverableKeyException ex) {
            Logger.getLogger(SSLCertEngineServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (KeyManagementException ex) {
            Logger.getLogger(SSLCertEngineServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        }
    }

  

    @Override
    public SSLSocket GetRemoteSSLSocket(String RemoteHost, String RemotePort) {
        SSLSocket socket = null;
        try {
            
            socket = (SSLSocket) this.contex.getSocketFactory().createSocket(RemoteHost, Integer.parseInt(RemotePort));
        } catch (IOException ex) {
            Logger.getLogger(SSLCertEngineServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
               
        }
        return socket;
    }

     @Override
    public SSLSocket ConvertClientToSSLSocket(Socket socket,String domain) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, UnrecoverableKeyException, NoSuchProviderException, OperatorCreationException, InvalidKeyException, SignatureException, KeyManagementException {
         try {
            
             KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
             BouncyCastle impl = new BouncyCastle();
             KeyStore store =impl.clientCertificateStore(domain);
            
            
             factory.init(store, "Jocundity123#".toCharArray());
             SSLContext context = SSLContext.getInstance("SSL");
             context.init(factory.getKeyManagers(),new TrustManager[]{ this.trustallTrustManager()}, null);
             SSLSocket ssl = (SSLSocket) context.getSocketFactory().createSocket(socket, socket.getInetAddress().getHostName(),socket.getPort(), true);
             
             System.out.println(socket.getInetAddress().getHostName());
             System.out.println(socket.getPort());
             ssl.setUseClientMode(false);
            
             return ssl;
             
             
         } catch (IOException ex) {
             Logger.getLogger(SSLCertEngineServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
         }
       return null;
    }
public SSLContext getsslcontext()
{
   return this.context;
}
    @Override
    public SSLEngine getSSLEngine() {
        
        return this.contex.createSSLEngine();
    }

    @Override
    public TrustManager trustallTrustManager() {
        TrustManager tm = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        return tm;
    }
      private KeyPair createKeyPair() throws NoSuchAlgorithmException { 
  final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA"); 
  final SecureRandom random  = SecureRandom.getInstance("SHA1PRNG"); 
  random.setSeed(Long.toString(System.currentTimeMillis()).getBytes()); 
  keyGen.initialize(2048, random); 
  final KeyPair keypair = keyGen.generateKeyPair(); 
  return keypair; 
 }

    @Override
    public SSLSocket ConvertClientToSSLSocket(Socket socket) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public SSLEngine getSSLEngine(String host, int i) {
        return this.contex.createSSLEngine(host,i);//To change body of generated methods, choose Tools | Templates.
    }
    private static class SSLCertEngineServiceImplHolder {

        private static final SSLCertEngineServiceImpl INSTANCE = new SSLCertEngineServiceImpl();
    }
  public static void main(String[] args) throws IOException
  {
    SSLCertEngineServiceImpl service =  SSLCertEngineServiceImpl.getInstance();
    
     
  }
}
