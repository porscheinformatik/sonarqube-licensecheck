# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [7.0.3](https://github.com/porscheinformatik/sonarqube-licensecheck/compare/v7.0.2...v7.0.3) - 2025-07-09

### Bug Fixes

- fix: skip artifacts with "data" classifier (#456, #457)  
  Some Maven dependencies have an additional artifact with "data" classifiers which leads to the issue that in some cases the license cannot be found correctly.

### Other Changes

- chore: fix dependencies for echoes-react and bump minor versions in package.json (#455)

## [7.0.2](https://github.com/porscheinformatik/sonarqube-licensecheck/compare/v7.0.1...v7.0.2) - 2025-06-23

### Bug Fixes

- Also check maven.repo.local is set via MAVEN_OPTS in License finding (#451)

## [7.0.1](https://github.com/porscheinformatik/sonarqube-licensecheck/compare/v7.0.0...v7.0.1) - 2025-06-23

### Bug Fixes

- When maven.repo.local is set via MAVEN_OPTS it is not considered (#449)

## [7.0.0](https://github.com/porscheinformatik/sonarqube-licensecheck/compare/v6.0.1...v7.0.0) - 2025-05-15

### BREAKING CHANGES

This version is not compatible with SonarQube < 2025.0 / 25.x

### Features

- Update to SonarQube 2025.x/25.x in (#435)
- Switch UI from Vue to React (#435)

### Bug Fixes

- Fix #409 - Switched back to baseDir by @tgwbean in #416

### Other Changes

- Update dependencies

## [6.0.1](https://github.com/porscheinformatik/sonarqube-licensecheck/compare/v6.0.0...v6.0.1) - 2024-01-26

### Bug Fixes

- Dependency mapping "overwrite" should default to true (#413)

## [6.0.0](https://github.com/porscheinformatik/sonarqube-licensecheck/compare/v5.1.1...v6.0.0) - 2023-12-15

### BREAKING CHANGES

This version is not compatible with SonarQube < 9.5

### Features

- Compatibility with Sonar 10.x (#375)
- Support for Scala (#352)
- Feature to import SPDX license list (fa68e04422bdd12d05e78c72ee0a49224f6b8741)
- Resolve node_modules relative to package.json (#380)
- Make report path configurable in Gradle scanner (#397)

Other Changes:

- Use Prettier for code formatting #399

## [5.1.1](https://github.com/porscheinformatik/sonarqube-licensecheck/compare/v5.1.0...v5.1.1) - 2022-11-04

### Bug Fixes

- Fix Kotlin seems to not be supported on 5.1.0 (#350, #351)
- Improving NPM scan resilience (#347)

### Other Changes

- Fix some critical and major code smells (#353)
- New and shiny README (#343)

## [5.1.0](https://github.com/porscheinformatik/sonarqube-licensecheck/compare/v5.0.1...v5.1.0) - 2022-08-25

### Bug Fixes

- Rule repository for TypeScript was not registered (#315)
- Show correct measures for branches and pull requests (#325)

### Other Changes

- Add defined order to settings (#342)

## [5.0.1](https://github.com/porscheinformatik/sonarqube-licensecheck/compare/v5.0.0..v5.0.1) - 2022-02-24

### Bug Fixes

- Fix: rule repository for Kotlin was not registered (#311, #310)
- Fix: project licenses page call (#291, #296)

## [5.0.0](https://github.com/porscheinformatik/sonarqube-licensecheck/compare/v4.0.2..v5.0.0) - 2021-12-20

### BREAKING CHANGES

- This version uses a different format for storing the settings. The old settings will be migrated (and deleted) on the first start of SonarQube with the new version. You cannot go back to a previous version! With this change you can edit the settings via the default SonarQube UI or via the custom License Check settings. This change was necessary to remove the dependency on the internal SonarQube API. [#240, #244]

### Features

- Create issues on relevant dependency files (#285)
- License and dependency mapping now available for all dependency mechanisms (Groovy, Maven, NPM). Dependency mapping has now an attribute to toggle forced mapping. [#257]
- Support for JavaScript and Groovy projects (without any Java files) (#247, #241, #182)

### Bug Fixes

- Status in license report should be "Allowed" and "Disallowed" not true/false (#262)

### Other Changes

- Dependency updates and increase test coverage

## [4.0.2](https://github.com/porscheinformatik/sonarqube-licensecheck/compare/v4.0.1..v4.0.2) - 2021-05-04

### Bug Fixes

- Create license with status disallowed (#230), fixes #209

### Other Changes

- Update SonarQube API to 8.8 (#229)

## [4.0.1](https://github.com/porscheinformatik/sonarqube-licensecheck/compare/v4.0.0..v4.0.1) - 2021-05-04

### Bug Fixes

- Icons in UI are missing #217
