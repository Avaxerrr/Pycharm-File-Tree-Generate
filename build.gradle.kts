plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.25"
    id("org.jetbrains.intellij.platform") version "2.3.0"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // Use pycharmCommunity() instead of create("PC") for Community Edition
        pycharmCommunity("2024.2.5")

        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)

        // Add the PythonCore plugin explicitly
        bundledPlugin("PythonCore")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "242"
        }

        changeNotes = """
      Initial version
    """.trimIndent()
    }
}

// Rest of your tasks configuration remains the same
