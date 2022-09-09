package com.github.marschall.truststoremavenplugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStore.TrustedCertificateEntry;
import java.util.List;

final class Pkcs12Assertions {

  private Pkcs12Assertions() {
    throw new AssertionError("not instantiable");
  }

  static void assertOutputNoPassword(File targetFolder, List<String> aliases) throws IOException, GeneralSecurityException {
    assertOutput(targetFolder, aliases, null);
  }

  static void assertOutput(File targetFolder, List<String> aliases) throws IOException, GeneralSecurityException {
    assertOutput(targetFolder, aliases, "changeit".toCharArray());
  }

  static void assertOutput(File targetFolder, List<String> aliases, char[] password) throws IOException, GeneralSecurityException {
    boolean found = false;
    File[] targetFiles = targetFolder.listFiles();
    assertNotNull("target files", targetFiles);
    for (File targetFile : targetFiles) {
      if (targetFile.isFile() &&  targetFile.getName().endsWith(".p12")) {
        validateKeystore(targetFile, aliases, password);
        found = true;
      }
    }
    assertTrue("no keystore present", found);
  }

  private static void validateKeystore(File keyStoreFile, List<String> aliases, char[] password) throws IOException, GeneralSecurityException {

    KeyStore keyStore = KeyStore.getInstance("PKCS12");
    try (FileInputStream fileInputStream = new FileInputStream(keyStoreFile);
         BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
      keyStore.load(bufferedInputStream, password);
    }

    assertEquals("keystore size", aliases.size(), keyStore.size());
    for (String alias : aliases) {
      assertTrue(keyStore.getEntry(alias, null) instanceof TrustedCertificateEntry);
      assertNotNull(keyStore.getCertificate(alias));
    }
  }

}

