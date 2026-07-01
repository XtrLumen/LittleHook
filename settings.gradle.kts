pluginManagement {
    repositories {
        google()
        mavenCentral()
    }
    plugins {
        id("org.jetbrains.kotlin.android") version "2.0.0"
        id("com.android.application") version "8.13.0"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

include(":app")