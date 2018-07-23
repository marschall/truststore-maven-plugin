package com.github.marschall.truststoremavenplugin;

import static io.takari.maven.testing.TestResources.assertFilesPresent;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.takari.maven.testing.TestMavenRuntime;
import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenRuntime.MavenRuntimeBuilder;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;

@RunWith(MavenJUnitTestRunner.class)
@MavenVersions("3.5.4")
public class TruststoreMojoTest {

  @Rule
  public final TestResources resources = new TestResources();

  @Rule
  public final TestMavenRuntime maven = new TestMavenRuntime();

//  public final MavenRuntime maven;

  public TruststoreMojoTest(MavenRuntimeBuilder builder) throws Exception {
//    this.maven = builder.build();
    // ignore
  }

  @Test
  public void test() throws Exception {
    File basedir = this.resources.getBasedir("project-to-test");
    this.maven.executeMojo(basedir, "pkcs12");
    assertFilesPresent(basedir, "target/output.txt");
  }

}
