/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MITM;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
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
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import static org.bouncycastle.asn1.x509.ObjectDigestInfo.publicKey;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.bc.BcX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import static org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import sun.misc.IOUtils;

/**
 *
 * @author Sai_Pradeep_K_M
 */
public class BouncyCastle {
    static { 
        Security.addProvider(new BouncyCastleProvider()); 
    } 
    
    private String CACommonName;
    private int keySize = 4096;
    private static final Date NOT_AFTER = new Date(System.currentTimeMillis() + 86400000L * 365 * 100); 
    private static final Date NOT_BEFORE = new Date(System.currentTimeMillis() - 86400000L * 365);
    private static String SIGNATURE_ALGORITHM = "SHA256WithRSAEncryption";
    /** step-1
     * Create key pair-publickey and privatekey from keypair generator
     * @param int keysize
     */
    public KeyPair generateKeyPair(int keysize) throws NoSuchAlgorithmException, NoSuchProviderException
    {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", BouncyCastleProvider.PROVIDER_NAME); 
        generator.initialize(keysize, new SecureRandom()); 
        return generator.generateKeyPair(); 
    }
    /*
    step-2
    Aim: Create CA Certificate using the public key and private key
    Approach: 
    1. Add x500 Name principal.
    2. This is very important. Add extension with basicConstraints parameter as true.
    3. Sign the certificate with private key.
    
    */
    public KeyStore clientCertificateStore(String domain) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, NoSuchProviderException, OperatorCreationException, InvalidKeyException, SignatureException
    {
        KeyStore CaKeystore = KeyStore.getInstance(KeyStore.getDefaultType());
        CaKeystore.load(new FileInputStream("CaCert//CatalystRoot.jks"), "Jocundity123#".toCharArray());
       X509Certificate CaCert = (X509Certificate) CaKeystore.getCertificate("carootcatalyst");
       PrivateKey Caprikey = (PrivateKey) CaKeystore.getKey("carootcatalystprivatekey", "Jocundity123#".toCharArray());
       PublicKey Capubkey = CaCert.getPublicKey();
       X500Name issuer = new X509CertificateHolder(CaCert.getEncoded()).getSubject();
       X500Name subject = new X500Name("CN=" + domain + ", O=proxyserver, L=Banglore, ST=Karnataka, C=IN"); 
        BigInteger serial = BigInteger.valueOf(new Random().nextInt()); 
        KeyPair StorePair = this.generateKeyPair(1024);
       List<GeneralName> subjectAlternativeNames = new ArrayList<GeneralName>();
       subjectAlternativeNames.add(new GeneralName(GeneralName.dNSName,domain)); 
       X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(issuer, serial, NOT_BEFORE, NOT_AFTER, subject, StorePair.getPublic()); 
        GeneralNames subjectAlternativeNamesExtension = new GeneralNames(subjectAlternativeNames.toArray(new GeneralName[subjectAlternativeNames.size()])); 
       builder.addExtension(Extension.subjectAlternativeName,false,subjectAlternativeNamesExtension);
         
        builder.addExtension(Extension.subjectKeyIdentifier, false, createSubjectKeyIdentifier(StorePair.getPublic())); 
        builder.addExtension(Extension.basicConstraints, false, new BasicConstraints(false));
        
        
         X509Certificate cert = signCertificate(builder, Caprikey); 
   //      System.out.println(cert);
 //System.out.println(cert.getSubjectAlternativeNames());
        cert.checkValidity(new Date()); 
        cert.verify(Capubkey); 
        KeyStore truststore = KeyStore.getInstance(KeyStore.getDefaultType());
        truststore.load(null, null);
        
        truststore.setKeyEntry(domain, StorePair.getPrivate(), "Jocundity123#".toCharArray(), new X509Certificate[]{cert,CaCert});
        truststore.setCertificateEntry("CaRootCatalyst", CaCert);
       // truststore.setCertificateEntry(domain, cert);
        return truststore;
        
    }
    public X509Certificate generateCACert(PublicKey pubkey, PrivateKey prikey) throws IOException, OperatorCreationException, CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException
    {
        X500Name issuerName = new X500Name("CN=CatalystCA, O=proxyserver, L=Banglore, ST=Karnataka, C=India");
        X500Name subjectName = issuerName;/* Since it is self-signed certificate */
        BigInteger serial = BigInteger.valueOf(new Random().nextInt()); 
         X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(issuerName, serial, NOT_BEFORE, NOT_AFTER, subjectName, pubkey); 
        builder.addExtension(Extension.subjectKeyIdentifier, false, createSubjectKeyIdentifier(pubkey)); 
        builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true)); 
 
        KeyUsage usage = new KeyUsage(KeyUsage.keyCertSign | KeyUsage.digitalSignature | KeyUsage.keyEncipherment | KeyUsage.dataEncipherment | KeyUsage.cRLSign); 
        builder.addExtension(Extension.keyUsage, false, usage); 
 
        ASN1EncodableVector purposes = new ASN1EncodableVector(); 
        purposes.add(KeyPurposeId.id_kp_serverAuth); 
        purposes.add(KeyPurposeId.id_kp_clientAuth); 
        purposes.add(KeyPurposeId.anyExtendedKeyUsage); 
       
        builder.addExtension(Extension.extendedKeyUsage, false, new DERSequence(purposes)); 
 
        X509Certificate cert = signCertificate(builder, prikey); 
        cert.checkValidity(new Date()); 
        cert.verify(pubkey); 
 
        return cert; 
    }
     private static X509Certificate signCertificate(X509v3CertificateBuilder certificateBuilder, PrivateKey signedWithPrivateKey) throws OperatorCreationException, CertificateException { 
        ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).setProvider(PROVIDER_NAME).build(signedWithPrivateKey); 
      //  return new JcaX509CertificateConverter().setProvider(PROVIDER_NAME).getCertificate(certificateBuilder.build(signer)); 
        return new JcaX509CertificateConverter().getCertificate(certificateBuilder.build(signer));
    } 
     private static SubjectKeyIdentifier createSubjectKeyIdentifier(Key key) throws IOException { 
        ASN1InputStream is = null; 
        try { 
            is = new ASN1InputStream(new ByteArrayInputStream(key.getEncoded())); 
            ASN1Sequence seq = (ASN1Sequence) is.readObject(); 
            SubjectPublicKeyInfo info = new SubjectPublicKeyInfo(seq); 
            return new BcX509ExtensionUtils().createSubjectKeyIdentifier(info); 
        } finally { 
            //IOUtils.closeQuietly(is); 
        } 
    } 
     public KeyStore StoreCACert(String Alias,String password) throws KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, IOException, OperatorCreationException, CertificateException, InvalidKeyException, SignatureException
     {
         KeyStore CaKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
         CaKeyStore.load(null, null);
         KeyPair StorePair = this.generateKeyPair(1024);
         KeyPair CaPair = this.generateKeyPair(2048);
         X509Certificate CaCert = this.generateCACert(CaPair.getPublic(), CaPair.getPrivate());
         CaKeyStore.setCertificateEntry("CaRootCatalyst", CaCert);
         CaKeyStore.setKeyEntry("CaRootCatalystCert", StorePair.getPrivate(), password.toCharArray(), new X509Certificate[]{CaCert});
         CaKeyStore.setKeyEntry("CaRootCatalystPrivateKey", CaPair.getPrivate(), password.toCharArray(), new X509Certificate[]{CaCert});
         FileOutputStream fos = new FileOutputStream("CaCert//CatalystRoot.jks");
         CaKeyStore.store(fos, password.toCharArray());
      
         return CaKeyStore;
     }
    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException, IOException, OperatorCreationException, CertificateException, InvalidKeyException, SignatureException, KeyStoreException, UnrecoverableKeyException, KeyManagementException {
        
        BouncyCastle impl = new BouncyCastle();
       KeyStore store = impl.clientCertificateStore("www.google.com");
     X509Certificate client = (X509Certificate) store.getCertificate("www.google.com");
     System.out.println(client);
      
  /* KeyStore truststore =  impl.clientCertificateStore("www.telerik.com");
      
       Enumeration list = truststore.aliases();
       while(list.hasMoreElements())
       {
           System.out.println(list.nextElement());
       }
       System.out.println(KeyManagerFactory.getDefaultAlgorithm());
       System.out.println(KeyStore.getDefaultType());
       KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm() );
       factory.init(truststore, "Jocundity123#".toCharArray());
       TrustManager tm = new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        SSLContext context = SSLContext.getInstance("TLS");
      context.init(factory.getKeyManagers(), new TrustManager[]{tm}, new SecureRandom());
      SSLSocket socket = (SSLSocket) context.getSocketFactory().createSocket("www.telerik.com", 443);
      socket.startHandshake();
      socket.close();
      System.out.println(socket.getSession().getLocalPrincipal());
      System.out.println(socket.getSession().getPeerPrincipal());
      System.out.println(socket.getSession().getCipherSuite());*/
   
       
    }
    
}
