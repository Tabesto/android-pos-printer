# Tabesto printer module

## Table of Contents
<!---
ToC generated with https://ecotrust-canada.github.io/markdown-toc/
-->

- [Global architecture](#global-architecture)
- [Installation](#installation)
- [Usage](#usage)
  * [Connect printer](#connect-printer)
  * [Discover printers](#discover-printers)
  * [Print ticket](#print-ticket)
  * [Handle errors](#handle-errors)
  * [...](#)
- [Android architecture](#android-architecture)
- [Continuous integration](#continuous-integration)
  * [Build, tests, lints and sonar](#build--tests--lints-and-sonar)
  * [Build, lints and sonar](#build--lints-and-sonar)
  * [Publish library and prepare release](#publish-library-and-prepare-release)
  * [Publish Javadoc](#publish-javadoc)
- [Contributing](#contributing)
- [License](#license)



## Global architecture

This module allows you to use POS printers easily with your Android application.



![Global architecture](docs/global-architecture-diagram.png?raw=true "Global architecture")



**NB**: For now, only EPSON and its [ePOS SDK](https://download.epson-biz.com/modules/pos/index.php?page=single_soft&cid=6547&scat=61&pcat=52) is supported by the module.



## Installation

```api 'com.tabesto:printer-module:1.0.0-beta4'``` 🚧 TODO: update when first version will be released



## Usage

See `sample` application

🚧 TODO:

### Connect printer

### Discover printers

### Print ticket

### Handle errors

### ...



## Android architecture

🚧 TODO


## Continuous integration
### Build, tests, lints and sonar
A GitHubAction [android-tests](.github/workflows/android-tests.yml) is configured to build `printer` module on:
 - Pull request events : Open and Ready for review
 - Merge on develop branch
 - Demand

It follows these steps:

1. **Build** `printer` module
2. Run **android lint** & **kotlin detekt** for `printer` & `sample` modules
3. Run **instrumentation tests** for `printer` module
4. Run **sonar** analyze (see [SonarCloud Project](https://sonarcloud.io/dashboard?id=Tabesto_pos-printer-module))



### Build, lints and sonar

A GitHubAction [android-sonar](.github/workflows/android-sonar.yml) is configured to build `printer` module on:

 - Pull request events : Open and Ready for review
 - Merge on develop branch
 - Demand

It follows these steps:

1. **Build** `printer` module
2. Run **android lint** & **kotlin detekt** for `printer` & `sample` modules
3. Run **sonar** analyze (see [SonarCloud Project](https://sonarcloud.io/dashboard?id=Tabesto_pos-printer-module))
4. Notify **slack** `mobile-ci` channel anyway


## Contributing

If you want to contribute to the code, please refer to the following [CONTRIBUTING](CONTRIBUTING.md).

If you want to modify diagrams present in this README, you can find all resources in `/docs/resources  ` directory. You can open xml file with [draw.io](https://draw.io/).



## License

🚧 TODO: chose a public license when module will become public.