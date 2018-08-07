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
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Generates a PKCS12 truststore from a collection of certificates located in a folder.
 */
@Mojo(
  name = "pkcs12",
  threadSafe = true,
  defaultPhase = LifecyclePhase.PACKAGE
)
public class TruststoreMojo extends AbstractMojo {

  /**
   * The directory in which the certificates to add to the truststore are located.
   */
  @Parameter(defaultValue = "${project.basedir}/src/main/certificates")
  private File sourceDirectory;

  /**
   * Directory containing the generated JAR.
   */
  @Parameter(defaultValue = "${project.build.directory}", readonly = true)
  private File outputDirectory;

  /**
   * Name of the generated JAR.
   */
  @Parameter(defaultValue = "${project.build.finalName}", readonly = true)
  private String finalName;

  /**
   * The password to generate the truststore integrity check.
   */
  @Parameter(defaultValue = "changeit", property = "truststore.password")
  private String password;

  @Component
  private MavenProject project;

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

    File keyStoreFile = this.saveKeystore(keyStore);
    this.project.getArtifact().setFile(keyStoreFile);
  }

  private File saveKeystore(KeyStore keyStore) throws MojoFailureException, MojoExecutionException {
    if (!this.outputDirectory.exists()) {
      if (!this.outputDirectory.mkdirs()) {
        throw new MojoExecutionException("could not create folder: " + this.outputDirectory);
      }
    }

    File keyStoreFile = new File(this.outputDirectory, this.finalName + ".p12");
    if (keyStoreFile.exists()) {
      if (!keyStoreFile.delete()) {
        throw new MojoExecutionException("could not delete existing file: " + keyStoreFile);
      }
    }

    this.getLog().debug("saving keystore to: " + keyStoreFile);
    try (FileOutputStream fileOutputstream = new FileOutputStream(keyStoreFile);
         BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputstream)) {
      keyStore.store(bufferedOutputStream, this.password.toCharArray());
    } catch (IOException | GeneralSecurityException e) {
      throw new MojoFailureException("could not save keystore: " + keyStoreFile, e);
    }
    return keyStoreFile;
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
