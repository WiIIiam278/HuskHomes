The HuskHomes API provides methods for getting, editing and updating user homes and warps, as well as functionality for building and executing (cross-server) teleports.

The API is distributed on Maven through [repo.william278.net](https://repo.william278.net/#/releases/net/william278/huskhomes/) and can be included in any Maven, Gradle, etc. project. JavaDocs are [available here](https://repo.william278.net/javadoc/releases/net/william278/huskhomes/latest).

## Compatibility
[![Maven](https://repo.william278.net/api/badge/latest/releases/net/william278/huskhomes?color=00fb9a&name=Maven&prefix=v)](https://repo.william278.net/#/releases/net/william278/huskhomes/)

The HuskHomes API shares version numbering with the plugin itself for consistency and convenience. Please note minor and patch plugin releases may make API additions and deprecations, but will not introduce breaking changes without notice.

| API Version |  HuskHomes Versions  | Supported |
|:-----------:|:--------------------:|:---------:|
|    v4.x     | _v4.0&mdash;Current_ |     ✅     |
|    v3.x     | _v3.0&mdash;v3.2.1_  |     ❌     |
|    v2.x     | _v2.0&mdash;v2.11.2_ |     ❌     |
|    v1.x     | _v1.5&mdash;v1.5.11_ |     ❌     |

<details>
<summary>Targeting older versions</summary>

HuskHomes versions prior to `v4.3.1` are distributed on [JitPack](https://jitpack.io/#/net/william278/HuskHomes2), and you will need to use the `https://jitpack.io` repository instead.
</details>

## Table of contents
1. Adding the API to your project
2. Adding HuskHomes as a dependency
3. Next steps

## API Introduction
### 1.1 Setup with Maven
<details>
<summary>Maven setup information</summary>

Add the repository to your `pom.xml` as per below. You can alternatively specify `/snapshots` for the repository containing the latest development builds (not recommended).
```xml
<repositories>
    <repository>
        <id>william278.net</id>
        <url>https://repo.william278.net/releases</url>
    </repository>
</repositories>
```
Add the dependency to your `pom.xml` as per below. Replace `VERSION` with the latest version of HuskHomes (without the v): ![Latest version](https://img.shields.io/github/v/tag/WiIIiam278/HuskHomes2?color=%23282828&label=%20&style=flat-square)
```xml
<dependency>
    <groupId>net.william278</groupId>
    <artifactId>huskhomes</artifactId>
    <version>VERSION</version>
    <scope>provided</scope>
</dependency>
```
</details>

### 1.2 Setup with Gradle
<details>
<summary>Gradle setup information</summary>

Add the dependency as per below to your `build.gradle`. You can alternatively specify `/snapshots` for the repository containing the latest development builds (not recommended).
```groovy
allprojects {
	repositories {
		maven { url 'https://repo.william278.net/releases' }
	}
}
```
Add the dependency as per below. Replace `VERSION` with the latest version of HuskHomes (without the v): ![Latest version](https://img.shields.io/github/v/tag/WiIIiam278/HuskHomes2?color=%23282828&label=%20&style=flat-square)

```groovy
dependencies {
    compileOnly 'net.william278:huskhomes:VERSION'
}
```
</details>

### 2. Adding HuskHomes as a dependency
Add HuskHomes to your `softdepend` (if you want to optionally use HuskHomes) or `depend` (if your plugin relies on HuskHomes) section in `plugin.yml` of your project.

```yaml
name: MyPlugin
version: 1.0
main: net.william278.myplugin.MyPlugin
author: William278
description: 'A plugin that hooks with the HuskHomes API!'
softdepend: # Or, use 'depend' here
  - HuskHomes
```

### 3. Next steps
Now that you've got everything ready, you can start doing stuff with the HuskHomes API!
- [[API Examples]]
- [[API Events]]