# Contributing

If you want to contribute to the code, please follow the following rules.

## Table of contents

<!---
ToC generated with https://ecotrust-canada.github.io/markdown-toc/
-->

- [💻 Android Studio](#---android-studio)
  * [Recommended plugins](#recommended-plugins)
- [✏️ Code](#---code)
  * [Clean code](#clean-code)
  * [Code Style](#code-style)
  * [Naming conventions](#naming-conventions)
  * [Lint & Sonar](#lint---sonar)
  * [Unit tests](#unit-tests)
  * [UI](#ui)
- [🛠 Tasks](#---tasks)
  * [Git](#git)
    + [Messages](#messages)
    + [Branches](#branches)
  * [App Versioning](#app-versioning)
  * [Release a version](#release-a-version)
  * [Markdown](#markdown)
  

## 💻 Android Studio

### Recommended plugins

- [Adb Idea](https://plugins.jetbrains.com/plugin/7380-adb-idea) : Use ADB commands to your connected device directly from Android Studio.
- [Kotlin Fill Class](https://plugins.jetbrains.com/plugin/10942-kotlin-fill-class) : Pre-fill all constructor parameters for a class. Essential for data classes.
- [Key Promoter](https://plugins.jetbrains.com/plugin/9792-key-promoter-x) : Learn shortcuts for all actions done in Android Studio.
- [String Manipulation](https://plugins.jetbrains.com/plugin/2162-string-manipulation) : Reformat strings (case switching, sorting, filtering, incrementing, aligning, encoding ...)
- [Rainbow Brackets](https://plugins.jetbrains.com/plugin/10080-rainbow-brackets) : Increase readability for nested blocks.


## ✏️ Code

### Clean code

Follow as much as possible the [Clean Code tenets](https://moderatemisbehaviour.github.io/clean-code-smells-and-heuristics/). TL;DR [Here is a summary](https://gist.github.com/wojteklu/73c6914cc446146b8b533c0988cf8d29).

### Code Style

Follow this [Kotlin Code Conventions](https://kotlinlang.org/docs/reference/coding-conventions.html).

To format the code, we follow the [DoistCodeStyle](https://github.com/Tabesto/java-kotlin-code-styles). In Android Studio go to `preferences` > `Editor` > `Code Style`, import [this code style scheme file](https://github.com/Tabesto/java-kotlin-code-styles/blob/master/configs/DoistStyle.xml) or simply check `Enable EditorConfig support`. 
Do not forget to reformat code before you commit:

- by clicking on `code` > ` reformat code` or typing `CMD` + `SHIFT` + `L` shortcut in each file.
- if you use Android Studio git cli, setup auto reformat code on git commit.

### Naming conventions

For Kotlin classes, follow the [official Kotlin naming rules](https://kotlinlang.org/docs/reference/coding-conventions.html#naming-rules).

For Android resources, follow [these naming conventions](https://jeroenmols.com/blog/2016/03/07/resourcenaming/).

According to the Clean Code, follow these generic naming rules :

- Choose descriptive and unambiguous names.
- Make meaningful distinction.
- Use pronounceable names.
- Use searchable names.
- Replace magic numbers with named constants.
- Avoid encodings. Don't append prefixes or type information.

### Lint & Sonar

_Before_ you commit, make sure that you have not introduced Lint or [Sonar](https://www.sonarlint.org/intellij/) warnings with Android Studio and its plugins.

_After_ you commit, verify analysis report on [SonarCloud Project](https://sonarcloud.io/dashboard?id=Tabesto_pos-printer-module) or directly in PR check if configured.
Make sure you have not increased issues or broken quality gates.

💡 To avoid `NewLineAtEndOfFile` warnings: In Android Studio go to `preferences` > `Editor` > `General`, check `Ensure line feed at file end on Save`. 

### Unit tests

To write unit tests, follow the [Given-When-Then](https://solidsoft.wordpress.com/2017/05/16/importance-of-given-when-then-in-unit-tests-and-tdd/) pattern.

_Before_ you commit, make sure that you :

- have covered new code
- do not have broken existing tests

by running unit tests locally.

_After_ you commit, verify if tests are running on CI:

- in Github Action CI job if triggered

- in [SonarCloud Project](https://sonarcloud.io/dashboard?id=Tabesto_pos-printer-module) if triggered

  

💡 To be sure to not break **lint** & **detekt** builds on CI, run locally this command before each push:


    ./gradlew clean lintDebug detekt



💡 To be sure to not break **test** builds on CI and check **code coverage**, run locally this command before each push:


    ./gradlew clean createDebugCoverageReport
    
and open **jacoco report** generated in :

    /printer/build/reports/coverage/debug/index.html


### UI

Follow as much as possible the [Design System](https://uxdesign.cc/everything-you-need-to-know-about-design-systems-54b109851969) principles:

- Create and re-use ui components with custom views
- Use as much as possible `ConstrainLayout` to build responsive screens.
- Create `styles`, `colors` and `dimens` instead of adding attributes in each views in layouts.


## 🛠 Tasks

### Git

#### Messages

Follow the [Commit Message Conventions][conventional-commits]

TL;DR Follow the [Angular Commit Message Guidelines](https://github.com/angular/angular/blob/22b96b9/CONTRIBUTING.md#-commit-message-guidelines):

    ^(?:(?P<type>build|ci|docs|feat|fix|perf|refactor|style|test)(?:\((?P<scope>.+)\)|): (?P<description>.+)|Merge branch .+|Merge remote-tracking branch .+|Revert .+)$

- `build`: Changes that affect the build system or external dependencies (example scopes: gulp, broccoli, npm)
- `ci`: Changes to our CI configuration files and scripts (example scopes: Travis, Circle, BrowserStack, SauceLabs)
- `docs`: Documentation only changes
- `feat`: A new feature
- `fix`: A bug fix
- `perf`: A code change that improves performance
- `refactor`: A code change that neither fixes a bug nor adds a feature
- `style`: Changes that do not affect the meaning of the code (white-space, formatting, missing semi-colons, etc)
- `test`: Adding missing tests or correcting existing tests

ℹ️ To be sure avoiding bad commit messages, you need to activate `commit-msg` hook by executing this command:

```
cp .githooks/commit-msg .git/hooks/commit-msg
```

#### Branches

Follow the [GitHub Flow][github-flow]

 🚧 TODO update this.

    ^(?:master|main|develop|feature\/(?P<feature_name>[A-Za-z][A-Za-z0-9\-\/]*)|(?:release|hotfix)\/(?P<major>0|[1-9]\d*)\.(?P<minor>0|[1-9]\d*)\.(?P<patch>0|[1-9]\d*)(?:(?P<pre_release>(?:0|[1-9]\d*|\d*[A-Za-z-][A-Za-z0-9-]*)(?:\.(?:0|[1-9]\d*|\d*[A-Za-z-][A-Za-z0-9-]*))*))?(?:\+(?P<build_metadata>[A-Za-z0-9-]+(?:\.[A-Za-z0-9-]+)*))?)$


### App Versioning

Follow the [SemVer Conventions][semver]

### Release a version

 🚧 TODO explain release process with GitHub.

### Markdown

Contribute to the [README](README.md) and [CONTRIBUTING](CONTRIBUTING.md) files:

If you want to enrich those files, please Follow the [Markdown Syntax](https://www.markdownguide.org/basic-syntax/).

If you add or update titles, please generate a new Table of Contents by using [markdown-toc](https://ecotrust-canada.github.io/markdown-toc/).






[semver]: http://semver.org
[conventional-commits]: https://www.conventionalcommits.org
[github-flow]: https://guides.github.com/introduction/flow/
[standard-version]: https://github.com/conventional-changelog/standard-version
