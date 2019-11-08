# tkit-mp-openapi-plugin

1000kit microprofile openapi plugin

[![License](https://img.shields.io/badge/license-Apache--2.0-green?style=for-the-badge&logo=apache)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.1000kit.maven/tkit-mp-openapi-plugin?logo=java&style=for-the-badge)](https://maven-badges.herokuapp.com/maven-central/org.1000kit.maven/tkit-mp-openapi-plugin)

## Goal: generate

```xml
<plugin>
    <groupId>org.1000kit.mp</groupId>
    <artifactId>tkit-mp-openapi-plugin</artifactId>
    <version>0.1.0-SNAPSHOT</version>
    <executions>
        <execution>
            <id>generate</id>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <verbose>true</verbose>
                <classesDir>${project.build.outputDirectory}</classesDir>
                <configFile>src/main/my.properties</configFile>
                <configFileOrdinal>200</configFileOrdinal>
                <properties>
                    <mp.openapi.scan.exclude.packages>org.tkit.parameters.rs.external.v2</mp.openapi.scan.exclude.packages>
                </properties>
                <propertiesOrdinal>201</propertiesOrdinal>
                <format>YAML</format>
                <outputFile>${project.build.directory}/openapi.yaml</outputFile>
            </configuration>
        </execution>
    </executions>
</plugin>
```

#### Parameters

|  Name | Default  | Values | Description  |
|---|---|---|---|
| verbose  | false | | The verbose flag  |
| classDir | ${project.build.outputDirectory} | | Directory of the classes |
| configFile | | | The micro-profile configuration property file |
| configFileOrdinal | 200 | | The micro-profile configuration property file ordinal number |
| properties | | | The micro-profile configuration properties in the plugin configuration |
| propertiesOrdinal | 201 | | The micro-profile configuration properties ordinal number |
| format | YAML | YAML, JSON | The output format |
| outputFile | ${project.build.directory}/openapi.yaml | | The output openAPI file |

## Release

### Create a release

```bash
mvn semver-release:release-create
```

### Create a patch branch
```bash
mvn semver-release:patch-create -DpatchVersion=x.x.0
```
