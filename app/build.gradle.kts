import java.util.Properties

plugins {
    id("trees.android.application")
    id("trees.android.compose")
    id("trees.android.hilt")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.aboutlibraries)
}

/**
 * Release signing (upload key for Google Play), from `keystore.properties` at the repository root (git-ignored) or
 * from environment variables in CI. Without them the release build is left unsigned. See RELEASING.md.
 */
val releaseSigning: Map<String, String>? =
    rootProject.file("keystore.properties").takeIf { it.exists() }?.let { file ->
        Properties().apply { file.inputStream().use { load(it) } }.let { props ->
            props.stringPropertyNames().associateWith { props.getProperty(it) }
        }
    } ?: System.getenv("TREES_KEYSTORE_FILE")?.let {
        mapOf(
            "storeFile" to it,
            "storePassword" to System.getenv("TREES_KEYSTORE_PASSWORD").orEmpty(),
            "keyAlias" to System.getenv("TREES_KEY_ALIAS").orEmpty(),
            "keyPassword" to System.getenv("TREES_KEY_PASSWORD").orEmpty(),
        )
    }

android {
    namespace = "com.treesofparis"
    defaultConfig {
        applicationId = "com.treesofparis"
        versionCode = 1
        versionName = "0.1.0"
    }
    buildFeatures.buildConfig = true

    signingConfigs {
        if (releaseSigning != null) {
            create("release") {
                storeFile = rootProject.file(releaseSigning.getValue("storeFile"))
                storePassword = releaseSigning.getValue("storePassword")
                keyAlias = releaseSigning.getValue("keyAlias")
                keyPassword = releaseSigning.getValue("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            // R8: shrink, optimise and obfuscate the code, then drop the resources nothing references.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
    }
}

aboutLibraries {
    // Fail the build if a dependency has no license information.
    export.prettyPrint = true
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.core.map)
    implementation(projects.core.mapOsmdroid)
    implementation(projects.feature.map)
    implementation(projects.feature.favorites)
    implementation(projects.feature.herbier)
    implementation(projects.feature.about)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.json)
}
