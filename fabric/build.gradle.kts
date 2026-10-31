import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

// Shared build script for every Fabric version in fabric/versions/ (see settings.gradle).
// loom-back-compat applies fabric-loom-remap below Minecraft 26 and fabric-loom (unobfuscated) from 26.
plugins {
    id("dev.kikugie.loom-back-compat")
}

val minecraftVersion: String = sc.current.version
val isUnobfuscated: Boolean = loomx.isUnobfuscated
val javaVersion: Int = if (isUnobfuscated) 25 else 21

version = "${rootProject.version}+mc.$minecraftVersion"
base.archivesName = "${rootProject.name}-Fabric"

val commonProject: Project = evaluationDependsOn(":common")

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://libraries.minecraft.net/")
    maven("https://repo.william278.net/releases/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://maven.impactdev.net/repository/development/")
    maven("https://maven.nucleoid.xyz/")
    maven("https://jitpack.io")
    // Hosts net.kyori:event-api:5.0.0-SNAPSHOT, required by Impactor's API on 1.21.1
    maven("https://repo.minebench.de/")
}

loom {
    serverOnlyMinecraftJar()
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraftVersion")
    // Mojang mappings below 26; unobfuscated versions need none
    loomx.applyMojangMappings()
    modImplementation("net.fabricmc:fabric-loader:${property("fabric_loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")

    // Fabric mods and libraries bundled as nested jars
    fun bundledMod(notation: String) {
        modImplementation(notation)
        include(notation)
    }
    bundledMod("net.kyori:adventure-platform-fabric:${property("fabric_adventure_platform_version")}")
    bundledMod("net.kyori:adventure-text-minimessage:${property("fabric_minimessage_version")}")
    bundledMod("me.lucko:fabric-permissions-api:${property("fabric_permissions_api_version")}")
    bundledMod("eu.pb4:placeholder-api:${property("fabric_placeholder_api_version")}")
    bundledMod("net.impactdev.impactor.api:economy:${property("fabric_impactor_api_version")}")
    bundledMod("net.william278.toilet:toilet-fabric:1.1.2+${findProperty("fabric_toilet_minecraft_version") ?: minecraftVersion}")
    if (!isUnobfuscated) {
        // Unobfuscated versions compile the vendored copy in src/unobfuscated instead
        bundledMod("com.github.PokeSkies:FabricPluginMessaging:f621c4fcf6")
    }

    // Plain Java libraries bundled as nested jars
    fun bundledLibrary(notation: Any) {
        implementation(notation)
        include(notation)
    }
    // Manually include config deps due to the way including api deps works
    bundledLibrary("de.exlll:configlib-core:4.8.1")
    bundledLibrary("org.snakeyaml:snakeyaml-engine:2.10")
    bundledLibrary("org.apache.commons:commons-pool2:2.13.1")
    bundledLibrary("redis.clients.authentication:redis-authx-core:0.1.1-beta2")

    // Include driver deps due to no runtime dep loading support
    bundledLibrary("org.xerial:sqlite-jdbc:${property("sqlite_driver_version")}")
    bundledLibrary("com.mysql:mysql-connector-j:${property("mysql_driver_version")}")
    bundledLibrary("org.mariadb.jdbc:mariadb-java-client:${property("mariadb_driver_version")}")
    bundledLibrary("redis.clients:jedis:${property("jedis_version")}")
    bundledLibrary("com.h2database:h2:${property("h2_driver_version")}")

    bundledLibrary(project(":common"))
    commonProject.configurations["api"].dependencies.forEach { dependency ->
        // MiniMessage must match the Adventure API supplied by adventure-platform-fabric.
        // Including the version declared by :common would bypass Gradle's version resolution.
        if (!(dependency.group == "net.kyori" && dependency.name == "adventure-text-minimessage")) {
            include(dependency)
        }
    }

    compileOnly("org.jetbrains:annotations:26.1.0")
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
}

if (isUnobfuscated) {
    sourceSets.main {
        java.srcDir(project(":fabric").file("src/unobfuscated/java"))
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
    withSourcesJar()
    withJavadocJar()
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
        options.release = javaVersion
    }

    javadoc {
        options.encoding = "UTF-8"
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    }

    processResources {
        val tokens = rootProject.extra.properties.mapValues { it.value.toString() }
        inputs.properties(tokens)
        filesMatching(listOf("**/*.json", "**/*.yml")) {
            filter<ReplaceTokens>("beginToken" to "\${", "endToken" to "}", "tokens" to tokens)
        }

        val modJsonProperties = mapOf(
            "version" to project.version.toString(),
            "fabric_loader_version" to project.property("fabric_loader_version").toString(),
            "fabric_minecraft_version" to minecraftVersion,
        )
        inputs.properties(modJsonProperties)
        filesMatching("fabric.mod.json") {
            expand(modJsonProperties)
        }
    }

    jar {
        from(rootProject.file("LICENSE"))
    }

    // The distributable mod jar: remapJar below 26, jar from 26
    loomx.modJar.configure {
        destinationDirectory = rootProject.layout.projectDirectory.dir("target")
        archiveClassifier = ""
    }

    named<Jar>("sourcesJar") {
        destinationDirectory = rootProject.layout.projectDirectory.dir("target")
    }

    named<Jar>("javadocJar") {
        destinationDirectory = rootProject.layout.projectDirectory.dir("target")
    }

    clean {
        delete(rootProject.layout.projectDirectory.dir("target"))
    }
}

// Publications live on the root project, which holds the Maven repositories
rootProject.extensions.configure<PublishingExtension> {
    publications.register<MavenPublication>("mavenJavaFabric_${minecraftVersion.replace('.', '_')}") {
        groupId = "net.william278.huskhomes"
        artifactId = "huskhomes-fabric"
        version = "${rootProject.version}+$minecraftVersion"
        artifact(loomx.modJar)
        artifact(tasks.named("sourcesJar"))
        artifact(tasks.named("javadocJar"))
    }
}
