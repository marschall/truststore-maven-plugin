package com.github.marschall.truststoremavenplugin;

import static com.github.marschall.truststoremavenplugin.Pkcs12Assertions.assertOutput;

import java.io.File;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenExecution;
import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenRuntime.MavenRuntimeBuilder;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;

@RunWith(MavenJUnitTestRunner.class)
@MavenVersions("3.8.4")
public class Pkcs12MojoTest {

  @Rule
  public final TestResources resources = new TestResources();

  private final MavenRuntime mavenRuntime;

  public Pkcs12MojoTest(MavenRuntimeBuilder builder) throws Exception {
    this.mavenRuntime = builder
            .withCliOptions("--batch-mode")
            .build();
  }

  @Test
  public void testBasic() throws Exception {
    File basedir = this.resources.getBasedir("project-to-test");
    MavenExecution execution = this.mavenRuntime.forProject(basedir);

    MavenExecutionResult result = execution.execute("clean", "package");
    result.assertErrorFreeLog();

    File targetFolder = new File(basedir, "target");

    assertOutput(targetFolder, Arrays.asList("DigiCertHighAssuranceEVRootCA", "isrg-root-x1"));
  }

  @Test
  public void testExpiredCertificate() throws Exception {
    File basedir = this.resources.getBasedir("expired-certificate");
    MavenExecution execution = this.mavenRuntime.forProject(basedir);

    MavenExecutionResult result = execution.execute("clean", "package");
    result.assertLogText("Expired certificate badssl-com.pem");
    result.assertLogText("BUILD FAILURE");
  }

}
