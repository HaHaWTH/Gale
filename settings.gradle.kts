import java.util.Locale

pluginManagement {
    repositories {
        gradlePluginPortal()
        //maven("https://repo.galemc.org/repository/maven-public/") // Dreeam - site is down, fallback to papermc's default repo
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

if (!file(".git").exists()) {
    // Gale start - build changes
    val errorText = """
        
        =====================[ ERROR ]=====================
         The Gale project directory is not a properly cloned Git repository.
         
         In order to build Gale from source you must clone
         the Gale repository using Git, not download a code
         zip from GitHub.
         
         Built Gale jars are available for download at
         https://github.com/Dreeam-qwq/Gale/actions
         
         See https://github.com/PaperMC/Paper/blob/main/CONTRIBUTING.md
         for further information on building and modifying Paper forks.
        ===================================================
    """.trimIndent()
    // Gale end - build changes
    error(errorText)
}

rootProject.name = "gale" // Gale - build changes

for (name in listOf("gale-api", "gale-server",)) { // Gale - build changes
    val projName = name.lowercase(Locale.ENGLISH)
    include(projName)
    findProject(":$projName")!!.projectDir = file(name)
}

optionalInclude("paper-api-generator")

fun optionalInclude(name: String, op: (ProjectDescriptor.() -> Unit)? = null) {
    val settingsFile = file("$name.settings.gradle.kts")
    if (settingsFile.exists()) {
        apply(from = settingsFile)
        findProject(":$name")?.let { op?.invoke(it) }
    } else {
        settingsFile.writeText(
            """
            // Uncomment to enable the '$name' project
            // include(":$name")

            """.trimIndent()
        )
    }
}
