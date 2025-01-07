import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    java
    `maven-publish`
    id("io.papermc.paperweight.patcher") version "2.0.0-beta.11"
}

val paperMavenPublicUrl = "https://repo.papermc.io/repository/maven-public/"

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    repositories {
        mavenCentral()
        maven(paperMavenPublicUrl)
    }

    dependencies {
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }
    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release = 21
        options.isFork = true
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }
    tasks.withType<Test> {
        testLogging {
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            events(TestLogEvent.STANDARD_OUT)
        }
    }
}

paperweight {
    upstreams.paper {
        ref = providers.gradleProperty("paperRef")

        patchFile {
            path = "paper-server/build.gradle.kts"
            outputFile = file("gale-server/build.gradle.kts") // Gale - build changes
            patchFile = file("gale-server/build.gradle.kts.patch") // Gale - build changes
        }
        patchFile {
            path = "paper-api/build.gradle.kts"
            outputFile = file("gale-api/build.gradle.kts") // Gale - build changes
            patchFile = file("gale-api/build.gradle.kts.patch") // Gale - build changes
        }
        patchDir("paperApi") {
            upstreamPath = "paper-api"
            excludes = setOf("build.gradle.kts")
            patchesDir = file("gale-api/paper-patches") // Gale - build changes
            outputDir = file("paper-api")
        }
        patchDir("paperApiGenerator") {
            upstreamPath = "paper-api-generator"
            patchesDir = file("gale-api-generator/paper-patches") // Gale - build changes
            outputDir = file("paper-api-generator")
        }
    }
}

tasks.register("printMinecraftVersion") {
    doLast {
        println(providers.gradleProperty("mcVersion").get().trim())
    }
}

tasks.register("printGaleVersion") { // Gale - branding changes
    doLast {
        println(project.version)
    }
}

// Gale start - branding changes - package license into jar
// Based on io.papermc.paperweight.taskcontainers.PaperclipTasks
/*
tasks.named("createMojmapBundlerJar") {
    doLast {

        // Based on io.papermc.paperweight.taskcontainers.PaperclipTasks
        val jarName = listOfNotNull(
            project.name,
            "paperclip",
            project.version,
            "mojmap"
        ).joinToString("-") + ".jar"

        // Based on io.papermc.paperweight.taskcontainers.PaperclipTasks
        val zipFile = layout.buildDirectory.file("libs/$jarName").path

        val rootDir = io.papermc.paperweight.util.findOutputDir(zipFile)

        try {
            io.papermc.paperweight.util.unzip(zipFile, rootDir)

            val licenseFileName = "LICENSE.txt"
            project(":gale-server").projectDir.resolve(licenseFileName)
                .copyTo(rootDir.resolve(licenseFileName).toFile())

            io.papermc.paperweight.util.ensureDeleted(zipFile)

            io.papermc.paperweight.util.zip(rootDir, zipFile)
        } finally {
            @OptIn(kotlin.io.path.ExperimentalPathApi::class)
            rootDir.deleteRecursively()
        }

    }
}
 */
// Gale end - branding changes - package license into jar
