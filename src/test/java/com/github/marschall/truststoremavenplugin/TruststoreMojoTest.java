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
@MavenVersions("3.6.3")
public class TruststoreMojoTest {

  @Rule
  public final TestResources resources = new TestResources();

  private final MavenRuntime mavenRuntime;

  public TruststoreMojoTest(MavenRuntimeBuilder builder) throws Exception {
    this.mavenRuntime = builder.build();
  }

  @Test
  public void testBasic() throws Exception {
    File basedir = this.resources.getBasedir("project-to-test");
    MavenExecution execution = this.mavenRuntime.forProject(basedir);

    MavenExecutionResult result = execution.execute("clean", "package");
    result.assertErrorFreeLog();

    File targetFolder = new File(basedir, "target");

    this.assertOutput(targetFolder);
  }

  private void assertOutput(File targetFolder) throws IOException, GeneralSecurityException {
    boolean found = false;
    File[] targetFiles = targetFolder.listFiles();
    assertNotNull("target files", targetFiles);
    for (File targetFile : targetFiles) {
      if (targetFile.isFile() &&  targetFile.getName().endsWith(".p12")) {
        this.validateKeystore(targetFile);
        found = true;
      }
    }
    assertTrue("no keystore present", found);
  }

  private void validateKeystore(File keyStoreFile) throws IOException, GeneralSecurityException {

    KeyStore keyStore = KeyStore.getInstance("PKCS12");
    try (FileInputStream fileInputStream = new FileInputStream(keyStoreFile);
         BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
      keyStore.load(bufferedInputStream, "changeit".toCharArray());
    }

    assertEquals("keystore size", 1, keyStore.size());
    String alias = "DigiCertHighAssuranceEVRootCA";
    assertTrue(keyStore.getEntry(alias, null) instanceof TrustedCertificateEntry);
    assertNotNull(keyStore.getCertificate(alias));
  }

}
