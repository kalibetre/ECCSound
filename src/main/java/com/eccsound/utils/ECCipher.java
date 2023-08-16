package com.eccsound.utils;

import com.eccsound.model.ProcessedFile;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.IESParameterSpec;

import javax.crypto.Cipher;
import java.io.File;
import java.io.PrintWriter;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Scanner;

public class ECCipher {
    private KeyPairGenerator keyPairGenerator;
    private ECPublicKey publicKey;
    private ECPrivateKey privateKey;
    private ECPublicKey receiverPublicKey;
    private IESParameterSpec iesParameterSpec;
    private Cipher cipher;

    public ECCipher() {
        Security.addProvider(new BouncyCastleProvider());

        try {
            keyPairGenerator = KeyPairGenerator.getInstance("EC", "BC");
            iesParameterSpec = new IESParameterSpec(null, null, 128);
            cipher = Cipher.getInstance("ECIES");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public byte[] encrypt(byte[] data) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, receiverPublicKey, iesParameterSpec);
            return cipher.doFinal(data);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ProcessedFile encryptFile(String filePath) {
        try {
            File file = new File(filePath);
            byte[] data = FileHelper.readBytesFromFile(file);
            byte[] encryptedData = encrypt(data);
            return new ProcessedFile(FileHelper.getFileExtension(filePath), encryptedData);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public byte[] decrypt(byte[] data) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey, iesParameterSpec);
            return cipher.doFinal(data);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ProcessedFile decryptFile(String filePath) {
        try {
            File file = new File(filePath);
            byte[] data = FileHelper.readBytesFromFile(file);
            return new ProcessedFile(FileHelper.getFileExtension(filePath), decrypt(data));
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public String getPublicKey() {
        if (publicKey == null) {
            return null;
        }
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
        return Base64.getEncoder().encodeToString(x509EncodedKeySpec.getEncoded());
    }

    public String getPrivateKey() {
        if (privateKey == null) {
            return null;
        }
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(
                privateKey.getEncoded());
        return Base64.getEncoder().encodeToString(
                pkcs8KeySpec.getEncoded());
    }

    public String getReceiverPublicKey() {
        if (receiverPublicKey == null) {
            return null;
        }
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(receiverPublicKey.getEncoded());
        return Base64.getEncoder().encodeToString(x509EncodedKeySpec.getEncoded());
    }

    public boolean setReceiverPublicKey(String receiverPublicKey) {
        this.receiverPublicKey = publicKeyFromString(receiverPublicKey);
        return this.receiverPublicKey != null;
    }

    public boolean generateKeys() {
        try {
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            publicKey = (ECPublicKey) keyPair.getPublic();
            privateKey = (ECPrivateKey) keyPair.getPrivate();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean saveKeyPairToFile(String filePath) {
        try {
            String privateKeyStr = getPrivateKey();
            String publicKeyStr = getPublicKey();
            File keyFile = new File(filePath);
            PrintWriter writer = new PrintWriter(keyFile);
            writer.println(privateKeyStr);
            writer.println(publicKeyStr);
            writer.close();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean loadKeyPairFromFile(String filePath) {
        try {
            File keyFile = new File(filePath);
            Scanner scanner = new Scanner(keyFile);
            String privateKeyStr = scanner.nextLine();
            String publicKeyStr = scanner.nextLine();
            scanner.close();
            privateKey = privateKeyFromString(privateKeyStr);
            publicKey = publicKeyFromString(publicKeyStr);
            return privateKey != null && publicKey != null;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public ECPublicKey publicKeyFromString(String keyString) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyString);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
            return (ECPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public ECPrivateKey privateKeyFromString(String keyString) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyString);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC", "BC");
            return (ECPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void clearKeys() {
        publicKey = null;
        privateKey = null;
    }
}
