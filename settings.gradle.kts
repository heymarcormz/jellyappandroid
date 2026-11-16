import java.util.Properties

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

val localProperties = Properties().apply {
    val file = rootDir.resolve("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        val qmUsername = localProperties.getProperty("QM_MAVEN_USERNAME").orEmpty().trim()
        val qmPassword = localProperties.getProperty("QM_MAVEN_PASSWORD").orEmpty().trim()
        if (qmUsername.isNotEmpty() && qmPassword.isNotEmpty()) {
            maven {
                name = "QuantumMetric"
                url = uri("https://sdk.quantummetric.com/maven-releases")
                credentials {
                    username = qmUsername
                    password = qmPassword
                }
            }
        }
    }
}

rootProject.name = "JellyApp"
include(":app")
