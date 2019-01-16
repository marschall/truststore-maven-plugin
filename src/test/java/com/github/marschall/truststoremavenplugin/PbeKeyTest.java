package com.github.marschall.truststoremavenplugin;

import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.junit.Test;

public class PbeKeyTest {
  
  private static final String KEY_ALIAS = "password-alias";
  private static final String KEYSTORE_PASSWORD = "changeit";

  @Test
  public void loadAndStoreKey() throws GeneralSecurityException, IOException {
    String password = "swordfish";
    PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBE");
    SecretKey key = keyFactory.generateSecret(keySpec);
    
    Path keystoreFile = Files.createTempFile("keystore", "p12");
    try {
      KeyStore keyStore = newKeyStore();
      keyStore.setKeyEntry(KEY_ALIAS, key, null, null);
      saveKeystore(keyStore, keystoreFile);
      
      keyStore = loadKeystore(keystoreFile);
      key = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
      
      keySpec = (PBEKeySpec) keyFactory.getKeySpec(key, PBEKeySpec.class);
      assertEquals(password, new String(keySpec.getPassword()));
      
    } finally {
      Files.delete(keystoreFile);
    }
  }
  
  private static KeyStore newKeyStore() throws IOException, GeneralSecurityException {
    KeyStore keyStore = KeyStore.getInstance("PKCS12");
    keyStore.load(null, null);
    return keyStore;
  }

  private static KeyStore loadKeystore(Path keyStoreFile) throws IOException, GeneralSecurityException {
    KeyStore keyStore = KeyStore.getInstance("PKCS12");
    try (InputStream fileInputStream = Files.newInputStream(keyStoreFile);
         BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
      keyStore.load(bufferedInputStream, KEYSTORE_PASSWORD.toCharArray());
    }

    return keyStore;
  }
  
  private static void saveKeystore(KeyStore keyStore, Path keyStoreFile) throws IOException, GeneralSecurityException {
    try (OutputStream outputStream = Files.newOutputStream(keyStoreFile);
         BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {
      keyStore.store(bufferedOutputStream, KEYSTORE_PASSWORD.toCharArray());
    }
  }

}
