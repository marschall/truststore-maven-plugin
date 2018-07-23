Trustore Maven Plugin [![Build Status](https://travis-ci.org/marschall/truststore-maven-plugin.svg?branch=master)](https://travis-ci.org/marschall/truststore-maven-plugin)
=====================

The truststore Maven plugin gives you an easy way to manage Java truststores.

The plugins allows you to manage truststores by having a project under source control that contains all your trusted certificates. A Maven build a produces a PKCS12 artifact that can be deployed to a Maven repository.

Simply create a project with packaging `pkcs12`


```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.acme</groupId>
  <artifactId>truststore</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>pkcs12</packaging>

  <build>
    <plugins>
      <plugin>
        <groupId>com.github.marschall</groupId>
        <artifactId>truststore-maven-plugin</artifactId>
        <version>0.1.0</version>
        <extensions>true</extensions>
      </plugin>
    </plugins>
  </build>
</project>

```

and add your certificates under `src/main/certificates`. The filename minus the extension will be the alias of the certificate.


The plugin only sports PKCS12 truststores and does not support legacy JKS truststores.

