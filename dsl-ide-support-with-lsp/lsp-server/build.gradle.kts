plugins {
    id("java")
    antlr // https://docs.gradle.org/current/userguide/antlr_plugin.html
    checkstyle // https://docs.gradle.org/current/userguide/checkstyle_plugin.html
    id("com.diffplug.spotless") version "6.25.0" // https://plugins.gradle.org/plugin/com.diffplug.spotless
}

version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.13.1")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:0.22.0")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
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

spotless {
    java {
        targetExclude("build/generated-src/**")
        googleJavaFormat()

        target("**/*.java")
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    dependsOn("spotlessJavaCheck")
    dependsOn("checkstyleMain")
    dependsOn("checkstyleTest")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "lsp.DslLanguageServerLauncher"
    }

    val dependencySigningFilesToExclude = setOf("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")

    from(sourceSets.main.get().output) {
        exclude(dependencySigningFilesToExclude)
    }

    from(configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }) {
        exclude(dependencySigningFilesToExclude)
    }

    doLast {
        val jarFile = archiveFile.get().asFile
        val destinationDir = file("../vscode-extension")
        copy {
            from(jarFile)
            into(destinationDir)
        }
        val destinationDirIntelliJ = file("../intellij-plugin")
        copy {
            from(jarFile)
            into(destinationDirIntelliJ)
        }
    }
}

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    arguments = arguments + listOf("-visitor", "-long-messages", "-package", "antlr_gen")
    outputDirectory =  File("$projectDir/build/generated-src/antlr/main/antlr_gen")
}

tasks.generateTestGrammarSource {
    maxHeapSize = "64m"
    arguments = arguments + listOf("-visitor", "-long-messages", "-package", "antlr_gen")
    outputDirectory = File("$projectDir/build/generated-src/antlr/test/antlr_gen")
}