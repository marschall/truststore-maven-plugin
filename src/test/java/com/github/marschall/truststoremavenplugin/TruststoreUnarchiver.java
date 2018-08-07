package com.github.marschall.truststoremavenplugin;

import java.io.IOException;
import java.io.Writer;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Enumeration;

public class TruststoreUnarchiver {
  
  private void unarchive(KeyStore keyStore) throws GeneralSecurityException, IOException {
    Enumeration<String> aliases = keyStore.aliases();
    while (aliases.hasMoreElements()) {
      String alias = aliases.nextElement();
      if (keyStore.isCertificateEntry(alias)) {
        Writer writer = null;
        unarchive(alias, keyStore.getCertificate(alias), writer);
      }
    }
  }

  private void unarchive(String alias, Certificate certificate, Writer writer) throws IOException, CertificateEncodingException {
    // writer.println(X509Factory.BEGIN_CERT);
    writer.write("-----BEGIN CERTIFICATE-----\n");
    Encoder encoder = Base64.getEncoder();
    writer.write(encoder.encodeToString(certificate.getEncoded()));
    writer.write("-----END CERTIFICATE-----");
    //writer.println(X509Factory.END_CERT);
  }

}
