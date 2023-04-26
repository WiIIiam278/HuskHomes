![HuskHomes CI](https://jitpack.io/v/WiIIiam278/HuskHomes2.svg)

The HuskHomes API provides methods for getting, editing and updating user homes and warps, as well as functionality for building and executing (cross-server) teleports.

The API is distributed via [JitPack](https://jitpack.io/#net.william278/HuskHomes2). Please note that HuskHomes API v1, v2 and v3 are not compatible.
(Some) javadocs are also available to view on Jitpack [here](https://javadoc.jitpack.io/net/william278/HuskHomes2/latest/javadoc/).

## Table of contents
1. Adding the API to your project
2. Adding HuskHomes as a dependency
3. Next steps

## API Introduction
### 1.1 Setup with Maven
<details>
<summary>Maven setup information</summary>

Add the repository to your `pom.xml` as per below.
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
Add the dependency to your `pom.xml` as per below. Replace `version` with the latest version of HuskHomes (without the v): ![Latest version](https://img.shields.io/github/v/tag/WiIIiam278/HuskHomes2?color=%23282828&label=%20&style=flat-square)
```xml
<dependency>
    <groupId>net.william278</groupId>
    <artifactId>HuskHomes2</artifactId>
    <version>version</version>
    <scope>provided</scope>
</dependency>
```
</details>

### 1.2 Setup with Gradle
<details>
<summary>Gradle setup information</summary>

Add the dependency like so to your `build.gradle`:
```groovy
allprojects {
	repositories {
		maven { url 'https://jitpack.io' }
	}
}
```
Add the dependency as per below. Replace `version` with the latest version of HuskHomes (without the v): ![Latest version](https://img.shields.io/github/v/tag/WiIIiam278/HuskHomes2?color=%23282828&label=%20&style=flat-square)

```groovy
dependencies {
    compileOnly 'net.william278:HuskHomes2:version'
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