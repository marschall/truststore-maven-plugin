package com.github.marschall.truststoremavenplugin;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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
        OutputStream outputStream = null;
        unarchive(alias, keyStore.getCertificate(alias), outputStream);
      }
    }
  }

  private void unarchive(String alias, Certificate certificate, OutputStream outputStream) throws IOException, CertificateEncodingException {
    // writer.println(X509Factory.BEGIN_CERT);
    outputStream.write("-----BEGIN CERTIFICATE-----\n".getBytes(StandardCharsets.US_ASCII));
    Encoder encoder = Base64.getEncoder();
    outputStream.write(encoder.encode(certificate.getEncoded()));
    outputStream.write("-----END CERTIFICATE-----".getBytes(StandardCharsets.US_ASCII));
    //writer.println(X509Factory.END_CERT);
  }

}
