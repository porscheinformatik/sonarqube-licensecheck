# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
- Create issues on relevant dependency files [#285]
- License and dependency mapping now available for all dependency mechanisms (Groovy, Maven, NPM). Dependency mapping has now an attribute to toggle forced mapping. [#257]
- Support for JavaScript and Groovy projects (without any Java files) [#247, #241, #182]

### Bug Fixes
- Status in license report should be "Allowed" and "Disallowed" not true/false [#262]

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


