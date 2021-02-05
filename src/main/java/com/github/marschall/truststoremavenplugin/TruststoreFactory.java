package com.github.marschall.truststoremavenplugin;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

final class TruststoreFactory {

  private final KeyStore keyStore;
  private final CertificateFactory certificateFactory;
  private final Log log;

  TruststoreFactory(Log log) throws MojoExecutionException, MojoFailureException {
    this.log = log;
    this.keyStore = this.newEmptyKeystore();
    this.certificateFactory = this.newCertificateFactory();
  }

  void addCertificatesIn(File directory) throws MojoExecutionException, MojoFailureException {
    boolean empty = true;
    for (File certificateFile : directory.listFiles()) {
      if (certificateFile.isDirectory()) {
        this.log.warn("skipping directory: " + certificateFile);
        continue;
      }
      // we don't currently filter by extension like .crt .pem
      this.addCertificate(certificateFile);
      empty = false;
    }
    if (empty) {
      this.log.warn("keystore is empty");
    }
  }

  private void addCertificate(File certificateFile) throws MojoExecutionException, MojoFailureException {
    Certificate certificate = this.loadCertificateFromFile(certificateFile);
    this.validateCertificate(certificate, certificateFile);

    String alias = getAlias(certificateFile);
    if (this.log.isDebugEnabled()) {
      this.log.debug("adding certificate: " + certificateFile + " with alias: " + alias);
    }
    try {
      this.keyStore.setCertificateEntry(alias, certificate);
    } catch (KeyStoreException e) {
      throw new MojoFailureException("could not add certificate: " + certificateFile, e);
    }
  }

  private Certificate loadCertificateFromFile(File certificateFile) throws MojoExecutionException {
    try (FileInputStream fileInputStream = new FileInputStream(certificateFile);
         BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
      return this.certificateFactory.generateCertificate(bufferedInputStream);
    } catch (IOException | CertificateException e) {
      throw new MojoExecutionException("could not load certificate from file: " + certificateFile, e);
    }
  }

  private void validateCertificate(Certificate certificate, File certificateFile) throws MojoFailureException {
    if (certificate instanceof X509Certificate) {
      X509Certificate x509Certificate = (X509Certificate) certificate;
      try {
        x509Certificate.checkValidity();
      } catch (CertificateExpiredException e) {
        String message = "Expired certificate " + certificateFile.getName();
        this.log.error(message);
        throw new MojoFailureException(message, e);
      } catch (CertificateNotYetValidException e) {
        // we allow this to support seamless certificate renewal
        this.log.info("Not yet valid certificate " + certificateFile.getName());
      }
    } else {
      this.log.warn("unknown certificate type: " + certificate.getClass());
    }

  }

  File saveKeystore(File outputDirectory, String finalName, String password) throws MojoExecutionException, MojoFailureException {
    if (!outputDirectory.exists()) {
      if (!outputDirectory.mkdirs()) {
        throw new MojoExecutionException("could not create folder: " + outputDirectory);
      }
    }

    File keyStoreFile = new File(outputDirectory, finalName + ".p12");
    if (keyStoreFile.exists()) {
      if (!keyStoreFile.delete()) {
        throw new MojoExecutionException("could not delete existing file: " + keyStoreFile);
      }
    }

    if (this.log.isDebugEnabled()) {
      this.log.debug("saving keystore to: " + keyStoreFile);
    }
    try (FileOutputStream fileOutputstream = new FileOutputStream(keyStoreFile);
         BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputstream)) {
      this.keyStore.store(bufferedOutputStream, password != null ? password.toCharArray() : null);
    } catch (IOException | GeneralSecurityException e) {
      throw new MojoFailureException("could not save keystore: " + keyStoreFile, e);
    }
    return keyStoreFile;
  }

  private CertificateFactory newCertificateFactory() throws MojoFailureException {
    try {
      return CertificateFactory.getInstance("X.509");
    } catch (CertificateException e) {
      throw new MojoFailureException("no X509 factory found", e);
    }
  }

  private KeyStore newEmptyKeystore() throws MojoExecutionException {
    KeyStore keyStore;
    try {
      keyStore = KeyStore.getInstance("PKCS12");
    } catch (KeyStoreException e) {
      throw new MojoExecutionException("PKCS12 not supported", e);
    }
    try {
      keyStore.load(null, null);
    } catch (GeneralSecurityException | IOException e) {
      throw new MojoExecutionException("could not initialize keystore", e);
    }
    return keyStore;
  }

  private static String getAlias(File certificateFile) {
    String fileName = certificateFile.getName();
    int lastDotIndex = fileName.lastIndexOf('.');
    if (lastDotIndex == -1) {
      return fileName;
    }
    return fileName.substring(0, lastDotIndex);
  }

}
