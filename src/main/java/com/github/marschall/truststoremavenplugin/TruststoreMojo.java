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
import java.security.cert.CertificateFactory;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(
        name = "pkcs12",
        threadSafe = true)
public class TruststoreMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project.basedir}/src/main/certificates")
  private File sourceDirectory;

  @Parameter(defaultValue = "${project.build.finalName}")
  private File finalName;

  @Parameter(defaultValue = "changeit", property = "truststore.password")
  private String password;

  @Override
  public void execute()
          throws MojoExecutionException, MojoFailureException {
    this.validate();
    KeyStore keyStore = this.newEmptyKeystore();

    this.getLog().info("adding certificates in directory: " + this.sourceDirectory);

    CertificateFactory certificateFactory = this.newCertificateFactory();

    boolean empty = true;
    for (File certificateFile : this.sourceDirectory.listFiles()) {
      if (certificateFile.isDirectory()) {
        this.getLog().warn("skipping directory: " + certificateFile);
        continue;
      }
      this.addCertificate(keyStore, certificateFactory, certificateFile);
      empty = false;
    }
    if (empty) {
      this.getLog().warn("keystore is empty");
    }

    this.saveKeystore(keyStore);
  }

  private void saveKeystore(KeyStore keyStore) throws MojoFailureException {
    this.getLog().debug("saving keystore to: " + this.finalName);
    try (FileOutputStream fileOutputstream = new FileOutputStream(this.finalName);
         BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputstream)) {
      keyStore.store(bufferedOutputStream, this.password.toCharArray());
    } catch (IOException | GeneralSecurityException e) {
      throw new MojoFailureException("could not save keystore: " + this.finalName, e);
    }
  }

  private void addCertificate(KeyStore keyStore, CertificateFactory certificateFactory, File certificateFile)
          throws MojoExecutionException, MojoFailureException {
    Certificate certificate = this.loadCertificateFromFile(certificateFactory, certificateFile);

    String alias = this.getAlias(certificateFile);
    this.getLog().debug("adding certificate: " + certificateFile + " with alias: " + alias);
    try {
      keyStore.setCertificateEntry(alias, certificate);
    } catch (KeyStoreException e) {
      throw new MojoFailureException("could not add certificate: " + certificateFile, e);
    }
  }

  private String getAlias(File certificateFile) {
    String fileName = certificateFile.getName();
    int lastDotIndex = fileName.lastIndexOf('.');
    if (lastDotIndex == -1) {
      return fileName;
    }
    return fileName.substring(0, lastDotIndex);
  }

  private Certificate loadCertificateFromFile(CertificateFactory certificateFactory, File certificateFile)
          throws MojoExecutionException {
    try (FileInputStream fileInputStream = new FileInputStream(certificateFile);
         BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
      return certificateFactory.generateCertificate(bufferedInputStream);
    } catch (IOException | CertificateException e) {
      throw new MojoExecutionException("could not load certificate from file: " + certificateFile, e);
    }
  }

  private CertificateFactory newCertificateFactory()
          throws MojoFailureException {
    try {
      return CertificateFactory.getInstance("X.509");
    } catch (CertificateException e) {
      throw new MojoFailureException("no X509 factory found", e);
    }
  }

  private KeyStore newEmptyKeystore()
          throws MojoExecutionException {
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

  private void validate() throws MojoFailureException {
    if (!this.sourceDirectory.exists()) {
      throw new MojoFailureException("certificate directory: " + this.sourceDirectory + " does not exist");
    }
    if (!this.sourceDirectory.isDirectory()) {
      throw new MojoFailureException("certificate directory: " + this.sourceDirectory + " is not a directory");
    }
  }

}
