This plugin is **deprecated** please use [smallrye-open-api-maven-plugin](https://github.com/smallrye/smallrye-open-api/tree/main/tools/maven-plugin)

# tkit-mp-openapi-plugin

tkit microprofile openapi plugin 
[![License](https://img.shields.io/badge/license-Apache--2.0-green?style=for-the-badge&logo=apache)](https://www.apache.org/licenses/LICENSE-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.tkit.maven/tkit-mp-openapi-plugin?logo=java&style=for-the-badge)](https://maven-badges.herokuapp.com/maven-central/org.tkit.maven/tkit-mp-openapi-plugin)
[![GitHub Actions Status](<https://img.shields.io/github/workflow/status/1000kit/tkit-mp-openapi-plugin/build?logo=GitHub&style=for-the-badge>)](https://github.com/1000kit/tkit-mp-openapi-plugin/actions/workflows/build.yml)

## What it does

This plugin allows you to generate Openapi schemas from code (JaxRS/Quarkus). Quarkus does support Openapi schema generation out of the box, however with some limitations:  
* Schema generation happens at runtime 
* All API endpoints are merged into single schema 
* No possibility to exclude classes from the scan

This plugin fixes these issues by using the same underlying mechanism for schema generation(Smallrye Openapi) but allowing more granular control.

## Goal: generate

```xml
<plugin>
    <groupId>org.tkit.maven</groupId>
    <artifactId>tkit-mp-openapi-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <id>generate</id>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <verbose>true</verbose>
                <classesDir>${project.build.outputDirectory}</classesDir>
                <!-- Extra MP config file that should be used as config source during the generator run -->
                <configFile>src/main/my.properties</configFile>
                <configFileOrdinal>200</configFileOrdinal>
                <properties>
                    <!-- Use this to control which packages should be included in scan -->
                    <mp.openapi.scan.exclude.packages>org.tkit.parameters.rs.external.v2</mp.openapi.scan.exclude.packages>
                </properties>
                <propertiesOrdinal>201</propertiesOrdinal>
                <format>YAML</format>
                <rootPath>root-rs</rootPath>
                <!-- where do you want to store the output -->
                <outputFile>${project.build.directory}/openapi.yaml</outputFile>
            </configuration>
        </execution>
    </executions>
</plugin>
```

If you want to generate multuiple schema files (for example internal.yaml and public-v1.yaml) then simply add 2 (or multiple) plugin executions with appropriate config.

### Parameters

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
| rootPath | | | When quarkus.http.root-path is set, it is useful to have rootPath also in output file  |

## Release

### Create a release

```bash
mvn semver-release:release-create
```

### Create a patch branch
```bash
mvn semver-release:patch-create -DpatchVersion=x.x.0
```
