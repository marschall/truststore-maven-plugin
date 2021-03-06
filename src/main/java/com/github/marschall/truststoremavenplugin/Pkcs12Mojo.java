package com.github.marschall.truststoremavenplugin;

import static org.apache.maven.plugins.annotations.LifecyclePhase.PACKAGE;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Generates a PKCS12 truststore from a collection of certificates located in a folder.
 * Supports the "pkcs12" packaging of a project.
 */
@Mojo(
  name = "pkcs12",
  threadSafe = true,
  defaultPhase = PACKAGE
)
public class Pkcs12Mojo extends AbstractMojo {

  /**
   * The directory in which the certificates to add to the truststore are located.
   */
  @Parameter(defaultValue = "${project.basedir}/src/main/certificates")
  private File sourceDirectory;

  /**
   * Directory containing the generated truststore.
   */
  @Parameter(defaultValue = "${project.build.directory}", readonly = true)
  private File outputDirectory;

  /**
   * Name of the generated truststore without a file extension.
   */
  @Parameter(defaultValue = "${project.build.finalName}")
  private String finalName;

  /**
   * The password to generate the truststore integrity check.
   */
  @Parameter(defaultValue = "changeit", property = "truststore.password")
  private String password;

  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    this.validate();

    TruststoreFactory truststoreFactory = new TruststoreFactory(this.getLog());
    truststoreFactory.addCertificatesIn(this.sourceDirectory);
    File truststoreFile = truststoreFactory.saveKeystore(this.outputDirectory, this.finalName, this.password);

    this.project.getArtifact().setFile(truststoreFile);
    this.project.addCompileSourceRoot(this.sourceDirectory.getAbsolutePath());
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
