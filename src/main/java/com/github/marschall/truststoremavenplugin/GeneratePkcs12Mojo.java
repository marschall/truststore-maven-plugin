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
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Generates a PKCS12 truststore from a collection of certificates located in a folder.
 * Runs during a normal Maven build.
 */
@Mojo(
  name = "generate-pkcs12",
  threadSafe = true,
  defaultPhase = GENERATE_RESOURCES
)
public class GeneratePkcs12Mojo extends AbstractMojo {

  /**
   * The directory in which the certificates to add to the truststore are located.
   */
  @Parameter(defaultValue = "${project.basedir}/src/main/certificates")
  private File sourceDirectory;

  /**
   * Directory containing the generated truststore.
   */
  @Parameter(defaultValue = "${project.build.directory}/generated-truststores")
  private File outputDirectory;

  /**
   * Name of the generated truststore without a file extension.
   */
  @Parameter(defaultValue = "${project.build.finalName}")
  private String finalName;

  /**
   * The password to generate the truststore integrity check.
   * <p>
   * The password is optional and can be left out. This is supported
   * out of the box on JDK 18+ and requires the following system
   * properties on earlier versions
   * <pre><code>-Dkeystore.pkcs12.certProtectionAlgorithm=NONE -Dkeystore.pkcs12.macAlgorithm=NONE</code></pre>
   */
  @Parameter(property = "truststore.password")
  private String password;

  @Parameter( defaultValue = "${mojoExecution}", readonly = true )
  private MojoExecution execution;

  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;

  @Component
  private BuildContext buildContext;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    this.validate();

    if (!this.buildContext.hasDelta(this.outputDirectory)) {
      return;
    }

    TruststoreFactory truststoreFactory = new TruststoreFactory(this.getLog(), this.buildContext);
    truststoreFactory.addCertificatesIn(this.sourceDirectory);
    truststoreFactory.saveKeystore(this.outputDirectory, this.finalName, this.password);

    Resource resource = new Resource();
    resource.setDirectory(this.outputDirectory.getAbsolutePath());
    // TODO should TargetPath be configurable?

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
      this.buildContext.addMessage(this.sourceDirectory, 0, 0, "certificate directory: " + this.sourceDirectory + " does not exist", BuildContext.SEVERITY_ERROR, null);
    }
    if (!this.sourceDirectory.isDirectory()) {
      this.buildContext.addMessage(this.sourceDirectory, 0, 0, "certificate directory: " + this.sourceDirectory + " is not a directory", BuildContext.SEVERITY_ERROR, null);
    }
  }

}
