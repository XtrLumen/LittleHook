import java.util.Properties

plugins {
    id("org.jetbrains.kotlin.android")
    id("com.android.application")
}

dependencies {
    compileOnly("io.github.libxposed:api:101.0.0")
    compileOnly(project(":stub"))
    implementation("org.luckypray:dexkit:2.2.0")
}

val properties = Properties()
val localPropertiesFile = rootProject.file("signature.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { stream ->
        properties.load(stream)
    }
}

val verName = providers.exec {
    commandLine("git", "rev-parse", "--verify", "--short", "HEAD")
}.standardOutput.asText.get().trim()
val verCode = providers.exec {
    commandLine("git", "rev-list", "HEAD", "--count")
}.standardOutput.asText.get().trim().toInt()

android {
    namespace = "io.github.xtrlumen.littlehook"
    buildToolsVersion = "36.0.0"
    compileSdk = 36
    defaultConfig {
        minSdk = 36
        targetSdk = 36
        versionCode = verCode
        versionName = "$verCode-$verName"
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("signature.jks")
            storePassword = properties.getProperty("KEYSTORE_PASSWORD")
            keyAlias = properties.getProperty("KEY_ALIAS")
            keyPassword = properties.getProperty("KEY_PASSWORD")
        }
    }

    buildTypes {
        all {
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            versionNameSuffix = "-d"
        }
        release {
            isMinifyEnabled = true
            vcsInfo.include = false
            isShrinkResources = true
            proguardFiles("proguard-rules.pro")
        }
    }

    packaging {
        jniLibs {
            excludes += setOf(
                "lib/armeabi-v7a/libdexkit.so",
                "lib/x86/libdexkit.so",
                "lib/x86_64/libdexkit.so"
            )
        }
        resources {
            excludes += setOf(
                "kotlin-tooling-metadata.json",
                "kotlin/**"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    dependenciesInfo {
        includeInApk = false
    }

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:deprecation")
        options.compilerArgs.add("-Xlint:unchecked")
        options.compilerArgs.add("-Xdiags:verbose")
    }
}