SonarQube License-Check
===================

[![Build Status](https://travis-ci.org/porscheinformatik/sonarqube-licensecheck.png?branch=master)](https://travis-ci.org/porscheinformatik/sonarqube-licensecheck)
[![Maintainability](https://api.codeclimate.com/v1/badges/6ac787bb79b43e39c367/maintainability)](https://codeclimate.com/github/porscheinformatik/sonarqube-licensecheck/maintainability)

This [SonarQube](http://www.sonarqube.org/) plugin ensures that projects in an organization adhere to a set of
standard libraries and versions. This enables the governance of the used libraries and licences.

## License

This software is licensed under the [Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)

## Compatibility

This plugin is compatible:

 * 1.x versions with SonarQube >= 5.3 and < 6.
 * 2.x version with SonarQube >= 6.5 and < 7.
 * 3.x version with SonarQube >= 7.9 LTS and < 8.

## Installation

Put the pre-built jar-file (from release downloads) in the directory `$SONARQUBE_HOME/extensions/plugins` and
restart the server to install the plugin. Activate the rules of this plugin ("License is not allowed", "Dependency has unknown license") in your SonarQube quality profiles - otherwise the plugin is not executed.

## Execution

When a project is analyzed using the `mvn sonar:sonar` in command line the extension is started automatically.

Please make sure to have all dependencies installed before launching the SonarQube analysis. So your complete build
should look something like this:

    mvn -B org.jacoco:jacoco-maven-plugin:prepare-agent -Dmaven.test.failure.ignore install
    mvn -B sonar:sonar

## Configuration

After booting the SonarQube Server with the License-Check Plugin two new options can be found in the tab
<b>Administration</b>.

Please do NOT use the page **Administration** -> **General Settings** -> **SonarQube License Check Plugin** for changing the plugin's settings.
Use **Administration** -> **Configuration** > **License check**.

* Within the general settings the plugin can be manually enabled or disabled. By default it is enabled.
* All other configuration is under License-Check specific settings (Configuration > License Check).
  * Under "Licenses" you can allow or disallow licenses globally and add/edit the list of known licenses.
  * Under "Project Licenses" you can allow and disallow licenses for a specific project.
  * Under "Maven Dependencies" you can map the Maven key (groupId:artifactId) to licenses using regular expressions. E.g. `^asm:asm$` to "BSD-3-Clause"
  * Under "Maven Licenses" you can map Maven license texts to licenses using regular expressions, e.g. `.*Apache.*2.*` to "Apache-2.0".

![License
configuration](docs/licensecheck_configuration.jpg)

### Maven

Maven works if your project/module has a `pom.xml` on its root level (running with Maven, Gradle or SonarScanner).

### NPM

NPM works if your project/module has a `package.json` on its root level (running with Maven, Gradle or SonarScanner).

### Gradle

Gradle project should use JK1 plugin https://github.com/jk1/Gradle-License-Report

Note: Please check above link for instructions or follow as mentioned below

**Step1:** Update `build.gradle` file with following code for using JK1 plugin

    import com.github.jk1.license.filter.LicenseBundleNormalizer
    import com.github.jk1.license.render.JsonReportRenderer

    plugins {
      id 'com.github.jk1.dependency-license-report' version '1.13'
    }

    licenseReport {
        allowedLicensesFile = new File("$projectDir/src/main/resources/licenses/allowed-licenses.json")
        renderers = new JsonReportRenderer('license-details.json', false)
        filters = [new LicenseBundleNormalizer()]
    }

**Step 2:** Update `build.gradle` file with following code for using SonarQube plugin

    plugins {
        id 'org.sonarqube' version "3.0"
    }

    jar {
        enabled = true
    }

    sonarqube {
        properties {
            property "sonar.host.url", "http://localhost:9000"
        }
    }

**Step 3:** run following command  to generate your report `license-details.json` in  `build/reports/dependency-license`

    > gradle generateLicenseReport

**Step 4:** run following command for SonarQube

    > gradle sonarqube


## Features

### Analysis

The plugin scans for dependencies defined in your project including all transitive dependencies.

Currently supported formats are:
* Maven POM files - all dependencies with scope "compile" and "runtime" are checked
* NPM package.json files - all dependencies (except "devDependencies") are checked
  * Note that transitive dependencies are _not_ scanned unless `licensecheck.npm.resolvetransitive` is set to `true`.

### Project Dashboard

The plugin contains a project dashboard showing a list of dependencies with version and a list of all used licences. Each table shows the status of the license
(allowed, not allowed, not found). You can also export the data to Excel.

Example for "Dependencies" table:
<table>
  <tr><th>Name</th><th>Version</th><th>License</th><th>Status</th></tr>
  <tr><td>org.springframework.boot:spring-boot</td><td>1.4.0.RELEASE</td><td>Apache-2.0</td><td>Allowed</td></tr>
  <tr><td>core-js</td><td>2.4.0</td><td>MIT</td><td>Allowed</td></tr>
  <tr><td>dk.brics.automaton:automaton</td><td>1.11-8</td><td>BSD-3-Clause</td><td>Not Allowed</td></tr>
  <tr><td>saxon:saxon</td><td>9.1.0.8j</td><td></td><td>Unknown</td></tr>
</table>

Example for "Licenses" table:
<table>
  <tr><th>Identifier</th><th>Name</th><th>Allowed</th></tr>
  <tr><td>Apache-2.0</td><td>Apache License 2.0</td><td>true</td></tr>
  <tr><td>MIT</td><td>MIT License</td><td>true</td></tr>
  <tr><td>BSD-3-Clause</td><td>BSD 3-clause New or Revised License</td><td>false</td></tr>
</table>
