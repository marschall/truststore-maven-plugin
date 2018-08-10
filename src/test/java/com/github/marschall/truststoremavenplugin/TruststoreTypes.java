package com.github.marschall.truststoremavenplugin;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Security;
import java.util.Arrays;
import java.util.Enumeration;

public class TruststoreTypes {

  public static void main(String[] args) throws GeneralSecurityException, IOException {
    Arrays.stream(Security.getProviders())
      .flatMap(p -> p.entrySet().stream())
      .map(e -> (String) e.getKey())
      .filter(e -> e.startsWith("KeyStore."))
      .filter(e -> !e.endsWith("ImplementedIn"))
      .map(e -> e.substring("KeyStore.".length()))
      .sorted()
      .forEach(System.out::println);

//    KeyStore keyStore = KeyStore.getInstance("KeychainStore");
//    keyStore.load(null, null);
//    Enumeration<String> aliases = keyStore.aliases();
//    while (aliases.hasMoreElements()) {
//      String alias = (String) aliases.nextElement();
//      if (keyStore.isCertificateEntry(alias)) {
//        System.out.println(alias);
//      }
//    }
  }

}
