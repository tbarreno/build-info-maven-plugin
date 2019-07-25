# build-info-maven-plugin

This is a simple [Maven](https://maven.apache.org/) plugin that gets
information about the artifacts built and uploaded to the repository so they
could be used in application build and deployment automation (CI/CD).

## Purpose

Some companies have many projects with a common application architecture or a
similar way to be built and deployed. In those scenarios, is a good practice to
define a common pipeline for those projects.

The `build-info` plugin allows DevOps engineers to use a common Maven build
pipeline while getting the binaries list from the build so the artifacts could
be deployed on any server or cloud without specifying the binaries in each
iteration.

## How it works?

The plugin scans the Maven's *Reactor* component getting all projects and
attached binaries, and calculates each upload URL using the distribution
management configuration of the project.

The result is an artifact list that is written to a file in any of those
formats:

 - Comma separated values (CSV)
 - JSON
 - XML
 - YAML
 - Shell script variables

The plugin must be used after the Maven build as attached artifacts are
registered in the *Reactor* component only during the build.

## Usage

Just append the `artifacts` goal of the plugin after the *deploy* stage on the
Maven command line and you will get a CSV file with all the binaries that have
been built.

You can choose the format and output filename with the '`-DoutputFile`'
option. For example:

```
mvn deploy info.bluespot:build-info-maven-plugin:1.0.0:artifacts -DoutputFile=artifacts.json
[INFO] Scanning for projects...
[INFO] 
[INFO] ---------------------------< test:MyLibrary >---------------------------
[INFO] Building MyLibrary 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
...
[INFO] --- build-info-maven-plugin:1.0.0:artifacts (default-cli) @ MyLibrary ---
Writting artifact information to file 'artifacts.json' (format 'json').
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
...
```

The output file ends like this:

```
[ {
  "groupId" : "test",
  "artifactId" : "MyLibrary",
  "version" : "1.0-SNAPSHOT",
  "classifier" : null,
  "type" : "jar",
  "url" : "http://localhost:8081/repository/maven-snapshots/test/MyLibrary/1.0-SNAPSHOT/MyLibrary-1.0-SNAPSHOT.jar"
}, {
  "groupId" : "test",
  "artifactId" : "MyLibrary",
  "version" : "1.0-SNAPSHOT",
  "classifier" : "sources",
  "type" : "java-source",
  "url" : "http://localhost:8081/repository/maven-snapshots/test/MyLibrary/1.0-SNAPSHOT/MyLibrary-1.0-SNAPSHOT-sources.jar"
}, {
  "groupId" : "test",
  "artifactId" : "MyLibrary",
  "version" : "1.0-SNAPSHOT",
  "classifier" : "javadoc",
  "type" : "javadoc",
  "url" : "http://localhost:8081/repository/maven-snapshots/test/MyLibrary/1.0-SNAPSHOT/MyLibrary-1.0-SNAPSHOT-javadoc.jar"
} ]
```

Now you can apply filters for selecting the proper binaries and launch the 
deployment scripts for each one.

## Output examples

For a more complete example, we will use a dummy project that features
some JAR binaries, a WAR web application and an EAR archive.

 - `service-api` (JAR)
 - `service-impl` (JAR)
 - `webapp` (WAR)
 - `enterprise-app` (EAR)

You may want to deploy the WAR webapp only when the target server is an
**Apache Tomcat** and the EAR archive when deploying over **Wildfly** or
a similar JEE server.

So, after the build you can filter the artifacts output file and filter the
binary by the `type` field and run the deployment scripts with the
artifact URL provided.

### CSV

The CSV output features a column description line at the begining of the file:

```
#groupId,artifactId,version,type,classifier,url
test,ComplexProject,3.2.1,pom,null,http://localhost:8081/repository/maven-releases/test/ComplexProject/3.2.1/ComplexProject-3.2.1.pom
test,service-api,3.2.1,jar,null,http://localhost:8081/repository/maven-releases/test/service-api/3.2.1/service-api-3.2.1.jar
test,service-api,3.2.1,java-source,sources,http://localhost:8081/repository/maven-releases/test/service-api/3.2.1/service-api-3.2.1-sources.jar
test,service-api,3.2.1,javadoc,javadoc,http://localhost:8081/repository/maven-releases/test/service-api/3.2.1/service-api-3.2.1-javadoc.jar
test,service-impl,3.2.1,jar,null,http://localhost:8081/repository/maven-releases/test/service-impl/3.2.1/service-impl-3.2.1.jar
test,service-impl,3.2.1,java-source,sources,http://localhost:8081/repository/maven-releases/test/service-impl/3.2.1/service-impl-3.2.1-sources.jar
test,service-impl,3.2.1,javadoc,javadoc,http://localhost:8081/repository/maven-releases/test/service-impl/3.2.1/service-impl-3.2.1-javadoc.jar
test,webapp,3.2.1,war,null,http://localhost:8081/repository/maven-releases/test/webapp/3.2.1/webapp-3.2.1.war
test,enterprise-app,3.2.1,ear,null,http://localhost:8081/repository/maven-releases/test/enterprise-app/3.2.1/enterprise-app-3.2.1.ear
```

The default CSV separator is the comma ('`,`'); it can be modified with
the '`-DcsvSeparator`' parameter.

For example:

```
mvn deploy info.bluespot:build-info-maven-plugin:1.0.0:artifacts -DoutputFile=artifacts.csv -DcsvSeparator=";"
```

Will result in this output file:

```
#groupId;artifactId;version;type;classifier;url
test;ComplexProject;3.2.1;pom;null;http://localhost:8081/repository/maven-releases/test/ComplexProject/3.2.1/ComplexProject-3.2.1.pom
test;service-api;3.2.1;jar;null;http://localhost:8081/repository/maven-releases/test/service-api/3.2.1/service-api-3.2.1.jar
test;service-api;3.2.1;java-source;sources;http://localhost:8081/repository/maven-releases/test/service-api/3.2.1/service-api-3.2.1-sources.jar
test;service-api;3.2.1;javadoc;javadoc;http://localhost:8081/repository/maven-releases/test/service-api/3.2.1/service-api-3.2.1-javadoc.jar
test;service-impl;3.2.1;jar;null;http://localhost:8081/repository/maven-releases/test/service-impl/3.2.1/service-impl-3.2.1.jar
test;service-impl;3.2.1;java-source;sources;http://localhost:8081/repository/maven-releases/test/service-impl/3.2.1/service-impl-3.2.1-sources.jar
test;service-impl;3.2.1;javadoc;javadoc;http://localhost:8081/repository/maven-releases/test/service-impl/3.2.1/service-impl-3.2.1-javadoc.jar
test;webapp;3.2.1;war;null;http://localhost:8081/repository/maven-releases/test/webapp/3.2.1/webapp-3.2.1.war
test;enterprise-app;3.2.1;ear;null;http://localhost:8081/repository/maven-releases/test/enterprise-app/3.2.1/enterprise-app-3.2.1.ear
```


### JSON

The JSON output consists in an array of artifact objects:

```
[ {
  "groupId" : "test",
  "artifactId" : "ComplexProject",
  "version" : "3.2.1",
  "classifier" : null,
  "type" : "pom",
  "url" : "http://localhost:8081/repository/maven-releases/test/ComplexProject/3.2.1/ComplexProject-3.2.1.pom"
}, {
  "groupId" : "test",
  "artifactId" : "service-api",
  "version" : "3.2.1",
  "classifier" : null,
  "type" : "jar",
  "url" : "http://localhost:8081/repository/maven-releases/test/service-api/3.2.1/service-api-3.2.1.jar"
}, {
  "groupId" : "test",
  "artifactId" : "service-api",
  "version" : "3.2.1",
  "classifier" : "sources",
  "type" : "java-source",
  "url" : "http://localhost:8081/repository/maven-releases/test/service-api/3.2.1/service-api-3.2.1-sources.jar"
}, {
  "groupId" : "test",
  "artifactId" : "service-api",
  "version" : "3.2.1",
  "classifier" : "javadoc",
  "type" : "javadoc",
  "url" : "http://localhost:8081/repository/maven-releases/test/service-api/3.2.1/service-api-3.2.1-javadoc.jar"
}, {
  "groupId" : "test",
  "artifactId" : "service-impl",
  "version" : "3.2.1",
  "classifier" : null,
  "type" : "jar",
  "url" : "http://localhost:8081/repository/maven-releases/test/service-impl/3.2.1/service-impl-3.2.1.jar"
}, {
  "groupId" : "test",
  "artifactId" : "service-impl",
  "version" : "3.2.1",
  "classifier" : "sources",
  "type" : "java-source",
  "url" : "http://localhost:8081/repository/maven-releases/test/service-impl/3.2.1/service-impl-3.2.1-sources.jar"
}, {
  "groupId" : "test",
  "artifactId" : "service-impl",
  "version" : "3.2.1",
  "classifier" : "javadoc",
  "type" : "javadoc",
  "url" : "http://localhost:8081/repository/maven-releases/test/service-impl/3.2.1/service-impl-3.2.1-javadoc.jar"
}, {
  "groupId" : "test",
  "artifactId" : "webapp",
  "version" : "3.2.1",
  "classifier" : null,
  "type" : "war",
  "url" : "http://localhost:8081/repository/maven-releases/test/webapp/3.2.1/webapp-3.2.1.war"
}, {
  "groupId" : "test",
  "artifactId" : "enterprise-app",
  "version" : "3.2.1",
  "classifier" : null,
  "type" : "ear",
  "url" : "http://localhost:8081/repository/maven-releases/test/enterprise-app/3.2.1/enterprise-app-3.2.1.ear"
} ]
```

### XML

```
<?xml version="1.0" encoding="UTF-8"?>
<artifacts>
  <artifact>
    <artifactId>ComplexProject</artifactId>
    <groupId>test</groupId>
    <version>3.2.1</version>
    <classifier>null</classifier>
    <type>pom</type>
    <url>http://localhost:8081/repository/maven-releases/test/ComplexProject/3.2.1/ComplexProject-3.2.1.pom</url>
  </artifact>
  <artifact>
    <artifactId>service-api</artifactId>
    <groupId>test</groupId>
    <version>3.2.1</version>
    <classifier>null</classifier>
    <type>jar</type>
    <url>http://localhost:8081/repository/maven-releases/test/service-api/3.2.1/service-api-3.2.1.jar</url>
  </artifact>
  <artifact>
    <artifactId>service-api</artifactId>
    <groupId>test</groupId>
    <version>3.2.1</version>
    <classifier>sources</classifier>
    <type>java-source</type>
    <url>http://localhost:8081/repository/maven-releases/test/service-api/3.2.1/service-api-3.2.1-sources.jar</url>
  </artifact>
  <artifact>
    <artifactId>service-api</artifactId>
    <groupId>test</groupId>
    <version>3.2.1</version>
    <classifier>javadoc</classifier>
    <type>javadoc</type>
    <url>http://localhost:8081/repository/maven-releases/test/service-api/3.2.1/service-api-3.2.1-javadoc.jar</url>
  </artifact>
  <artifact>
    <artifactId>service-impl</artifactId>
    <groupId>test</groupId>
    <version>3.2.1</version>
    <classifier>null</classifier>
    <type>jar</type>
    <url>http://localhost:8081/repository/maven-releases/test/service-impl/3.2.1/service-impl-3.2.1.jar</url>
  </artifact>
  <artifact>
    <artifactId>service-impl</artifactId>
    <groupId>test</groupId>
    <version>3.2.1</version>
    <classifier>sources</classifier>
    <type>java-source</type>
    <url>http://localhost:8081/repository/maven-releases/test/service-impl/3.2.1/service-impl-3.2.1-sources.jar</url>
  </artifact>
  <artifact>
    <artifactId>service-impl</artifactId>
    <groupId>test</groupId>
    <version>3.2.1</version>
    <classifier>javadoc</classifier>
    <type>javadoc</type>
    <url>http://localhost:8081/repository/maven-releases/test/service-impl/3.2.1/service-impl-3.2.1-javadoc.jar</url>
  </artifact>
  <artifact>
    <artifactId>webapp</artifactId>
    <groupId>test</groupId>
    <version>3.2.1</version>
    <classifier>null</classifier>
    <type>war</type>
    <url>http://localhost:8081/repository/maven-releases/test/webapp/3.2.1/webapp-3.2.1.war</url>
  </artifact>
  <artifact>
    <artifactId>enterprise-app</artifactId>
    <groupId>test</groupId>
    <version>3.2.1</version>
    <classifier>null</classifier>
    <type>ear</type>
    <url>http://localhost:8081/repository/maven-releases/test/enterprise-app/3.2.1/enterprise-app-3.2.1.ear</url>
  </artifact>
</artifacts>
```

### YAML

```
artifacts:
 - artifactId: 'ComplexProject'
   groupId: 'test'
   version: '3.2.1'
   classifier: 'null'
   type: 'pom'
   url: 'http://localhost:8081/repository/maven-releases/test/ComplexProject/3.2.1/ComplexProject-3.2.1.pom'
 - artifactId: 'service-api'
   groupId: 'test'
   version: '3.2.1'
   classifier: 'null'
   type: 'jar'
   url: 'http://localhost:8081/repository/maven-releases/test/service-api/3.2.1/service-api-3.2.1.jar'
 - artifactId: 'service-api'
   groupId: 'test'
   version: '3.2.1'
   classifier: 'sources'
   type: 'java-source'
   url: 'http://localhost:8081/repository/maven-releases/test/service-api/3.2.1/service-api-3.2.1-sources.jar'
 - artifactId: 'service-api'
   groupId: 'test'
   version: '3.2.1'
   classifier: 'javadoc'
   type: 'javadoc'
   url: 'http://localhost:8081/repository/maven-releases/test/service-api/3.2.1/service-api-3.2.1-javadoc.jar'
 - artifactId: 'service-impl'
   groupId: 'test'
   version: '3.2.1'
   classifier: 'null'
   type: 'jar'
   url: 'http://localhost:8081/repository/maven-releases/test/service-impl/3.2.1/service-impl-3.2.1.jar'
 - artifactId: 'service-impl'
   groupId: 'test'
   version: '3.2.1'
   classifier: 'sources'
   type: 'java-source'
   url: 'http://localhost:8081/repository/maven-releases/test/service-impl/3.2.1/service-impl-3.2.1-sources.jar'
 - artifactId: 'service-impl'
   groupId: 'test'
   version: '3.2.1'
   classifier: 'javadoc'
   type: 'javadoc'
   url: 'http://localhost:8081/repository/maven-releases/test/service-impl/3.2.1/service-impl-3.2.1-javadoc.jar'
 - artifactId: 'webapp'
   groupId: 'test'
   version: '3.2.1'
   classifier: 'null'
   type: 'war'
   url: 'http://localhost:8081/repository/maven-releases/test/webapp/3.2.1/webapp-3.2.1.war'
 - artifactId: 'enterprise-app'
   groupId: 'test'
   version: '3.2.1'
   classifier: 'null'
   type: 'ear'
   url: 'http://localhost:8081/repository/maven-releases/test/enterprise-app/3.2.1/enterprise-app-3.2.1.ear'
```

## LICENSE

This plugin is released under the [Apache Licence v2](https://www.apache.org/licenses/LICENSE-2.0).
