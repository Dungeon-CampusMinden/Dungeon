plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.4"
    checkstyle // https://docs.gradle.org/current/userguide/checkstyle_plugin.html
    id("com.diffplug.spotless") version "6.25.0" // https://plugins.gradle.org/plugin/com.diffplug.spotless
}

group = "org.hsbi.dungeon."
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2024.1.4")
    type.set("IC") // Target IDE Platform

    plugins.set(listOf("com.redhat.devtools.lsp4ij:0.3.0"))
}

checkstyle {
    configFile = rootProject.file(".checkstyle.xml")
    // Default version vs. current version? The default version is quite old (9.3 from
    // Jan 30, 2022), so let's go with the current version (10.17).
    // However, this needs to be updated manually as Dependabot won't deal with this!
    toolVersion = "10.17.0"
    isIgnoreFailures = false
    maxWarnings = 0
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    
    runIde {
        dependsOn("spotlessCheck")
        dependsOn("checkstyleMain")
    }
    
    buildPlugin {
        dependsOn("spotlessCheck")
        dependsOn("checkstyleMain")
    }

    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("242.*")
    }

    buildSearchableOptions {
        enabled = false
    }

    prepareSandbox {
        from(".") {
            include("lsp-server*.jar")
            into("${intellij.pluginName.get()}/lib/")
        }
    }
}
