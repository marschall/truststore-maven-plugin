package com.github.marschall.truststoremavenplugin;

import static org.apache.maven.plugins.annotations.LifecyclePhase.GENERATE_RESOURCES;

import java.io.File;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Generates a PKCS12 truststore from a collection of certificates located in a folder.
 * Runs during a normal Maven build.
 */
@Mojo(
  name = "pkcs12-inplace",
  threadSafe = true,
  defaultPhase = GENERATE_RESOURCES
)
public class Pkcs12InplaceMojo extends AbstractMojo {

  /**
   * The directory in which the certificates to add to the truststore are located.
   */
  @Parameter(defaultValue = "${project.basedir}/src/main/certificates")
  private File sourceDirectory;

  /**
   * Directory containing the generated truststore.
   */
  @Parameter(defaultValue = "${project.build.directory}/generated-truststores", readonly = true)
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

  @Component
  private MojoExecution execution;

  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    this.validate();

    TruststoreFactory truststoreFactory = new TruststoreFactory(this.getLog());
    truststoreFactory.addCertificatesIn(this.sourceDirectory);
    File truststoreFile = truststoreFactory.saveKeystore(this.outputDirectory, this.finalName, this.password);


    Resource resource = new Resource();
    resource.setDirectory(this.outputDirectory.getAbsolutePath());
    resource.setTargetPath(truststoreFile.getName());

    String phase = this.execution.getLifecyclePhase();
    if ("generate-resources".equals(phase)) {
      this.project.addResource(resource);
    } else if ("generate-test-sources".equals(phase)) {
      this.project.addTestResource(resource);
    } else {
      this.getLog().warn("unsupported phase: " + phase + " not attaching resource");
    }
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
