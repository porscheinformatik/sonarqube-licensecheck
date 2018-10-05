SonarQube License-Check
===================

[![Build Status](https://travis-ci.org/porscheinformatik/sonarqube-licensecheck.png?branch=master)](https://travis-ci.org/porscheinformatik/sonarqube-licensecheck)

This [SonarQube](http://www.sonarqube.org/) plugin ensures that projects in an organization adhere to a set of
standard libraries and versions. This enables the governance of the used libraries and licences.

## License

This software is licensed under the [Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)

## Compatibility

This plugin is compatible:
 
 * 1.x versions with SonarQube >= 5.3 and < 6.
 * 2.x version with SonarQube >= 6.5 and < 7.

## Installation

Put the pre-built jar-file (from release downloads) in the directory `$SONARQUBE_HOME/extensions/plugins` and
restart the server to install the plugin. Activate the rules of this plugin ("License is not allowed", "Dependency has unknown license") in your SonarQube quality profiles - otherwise the plugin is not executed.

## Execution

When a project is analyzed using the `mvn sonar:sonar` in command line the extension is started automatically.

## Configuration

After booting the SonarQube Server with the License-Check Plugin two new options can be found in the tab
<b>Administration</b>.

* Within the general settings the plugin can be manually enabled or disabled. By default it is enabled.
* All other configuration is under License-Check specific settings (Configuration > License Check). 
  * Under "Licenses" you can allow or disallow licenses globally and add/edit the list of known licenses. 
  * Under "Project Licenses" you can allow and disallow licenses for a specific project.
  * Under "Maven Dependencies" you can map the Maven key (groupId:artifactId) to licenses using regular expressions. E.g. `^asm:asm$` to "BSD-3-Clause"
  * Under "Maven Licenses" you can map Maven license texts to licenses using regular expressions, e.g. `.*Apache.*2.*` to "Apache-2.0".

![License
configuration](docs/licensecheck_configuration.jpg)

## Features

### Analysis

The plugin scans for dependencies defined in your project including all transitive dependencies. 

Currently supported formats are:
* Maven POM files - all dependencies with scope "compile" and "runtime" are checked
* NPM package.json files - all dependencies (except "devDependencies") are checked

### Project Dashboard

The plugin contains a project dashboard showing a list of dependencies with version and a list of all used licences. Each table shows the status of the license 
(allowed, not allowed, not found). You can also export the data to Excel.

Example for "Dependencies" table:
<table>
  <tr><th>Name</th><th>Version</th><th>License</th><th>Status</th></tr>
  <tr><td>org.springframework.boot:spring-boot</td><td>1.4.0.RELEASE</td><td>Apache-2.0</td><td>Allowed</td></tr>
  <tr><td>core-js</td><td>2.4.0</td><td>MIT</td><td>Allowed</td></tr>
  <tr><td>dk.brics.automaton:automaton</td><td>1.11-8</td><td>BSD-3-Clause</td><td>Not Allowed</td></tr>
  <tr><td>saxon:saxon</td><td>9.1.0.8j</td><td></td><td>Unknwon</td></tr>
</table>

Example for "Licenses" table:
<table>
  <tr><th>Identifier</th><th>Name</th><th>Allowed</th></tr>
  <tr><td>Apache-2.0</td><td>Apache License 2.0</td><td>true</td></tr>
  <tr><td>MIT</td><td>MIT License</td><td>true</td></tr>
  <tr><td>BSD-3-Clause</td><td>BSD 3-clause New or Revised License</td><td>false</td></tr>
</table>
