Truststore Maven Plugin [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.marschall/truststore-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.marschall/truststore-maven-plugin) [![Build Status](https://travis-ci.org/marschall/truststore-maven-plugin.svg?branch=master)](https://travis-ci.org/marschall/truststore-maven-plugin)
=======================

The truststore Maven plugin gives you an easy way to manage Java truststores.

The plugins allows you to manage truststores by having a project under source control that contains all your trusted certificates. A Maven build a produces a PKCS12 artifact that can be deployed to a Maven repository. Since the project is under source control a release is also tagged making to easy to know what is deployed in production.

Usage
-----

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
        <version>0.6.0</version>
        <extensions>true</extensions>
        <configuration>
          <password>changeit</password>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>

```

and add your certificates under `src/main/certificates`. The filename minus the extension will be the alias of the certificate. Certificates can be in either PEM or DER format.

The plugin can also generate a truststore in-place to `target/generated-truststores` which is added to the projects JAR file.

```xml

<project>

  <build>
    <plugins>
      <plugin>
        <groupId>com.github.marschall</groupId>
        <artifactId>truststore-maven-plugin</artifactId>
        <version>0.6.0</version>
        <executions>
          <execution>
            <id>generate-truststore</id>
            <goals>
              <goal>generate-pkcs12</goal>
            </goals>
            <configuration>
              <password>changeit</password>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

Documentation
-------------

For more information check out the generated [plugin page](https://marschall.github.io/truststore-maven-plugin/).

Passwordless Truststores
------------------------

Since [JDK 18](https://bugs.openjdk.org/browse/JDK-8274862) passwordless truststores are supported out of the box. [Earlier versions](https://bugs.openjdk.org/browse/JDK-8076190) need the following two system properties

```
-Dkeystore.pkcs12.certProtectionAlgorithm=NONE -Dkeystore.pkcs12.macAlgorithm=NONE
```

Similar Plugins
---------------

Similar plugins are [kaazing/truststore-maven-plugin](https://github.com/kaazing/truststore-maven-plugin) which creates a truststore using trusted sources from Mozilla and Chrome and [automatictester/truststore-maven-plugin](https://github.com/automatictester/truststore-maven-plugin) which is similar but can also download certificates or extract them from existing truststores.

