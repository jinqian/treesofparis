plugins {
    id("trees.android.application")
    id("trees.android.compose")
    id("trees.android.hilt")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.aboutlibraries)
}

android {
    namespace = "com.treesofparis"
    defaultConfig {
        applicationId = "com.treesofparis"
        versionCode = 1
        versionName = "0.1.0"
    }
    buildFeatures.buildConfig = true
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
