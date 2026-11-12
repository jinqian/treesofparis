import java.util.Properties

plugins {
    id("trees.android.application")
    id("trees.android.compose")
    id("trees.android.hilt")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.aboutlibraries)
}

// Firebase Crashlytics (ADR 0007). `google-services.json` is git-ignored: copy it from the Firebase console for local
// builds; CI writes it from the GOOGLE_SERVICES_JSON secret. Without it the app builds and runs, and sends nothing.
if (file("google-services.json").exists()) {
    apply(
        plugin =
            libs.plugins.google.services
                .get()
                .pluginId,
    )
    apply(
        plugin =
            libs.plugins.firebase.crashlytics
                .get()
                .pluginId,
    )
}

/**
 * The app version, in SemVer (see RELEASING.md). Change only this: the versionCode follows from it, so it goes up
 * whenever the version does.
 */
val appVersionName = "0.1.0"

/**
 * `major.minor.patch` → `major × 10,000 + minor × 100 + patch` (0.1.0 → 100, 1.2.3 → 10203). Minor and patch stay
 * below 100 so that a higher version always gives a higher code. The release workflow repeats this formula for the
 * earlier tags; keep both in sync.
 */
fun versionCodeOf(name: String): Int {
    val parts =
        Regex("""(\d+)\.(\d+)\.(\d+)""")
            .matchEntire(name)
            ?.destructured
            ?.toList()
            ?.map(String::toInt)
    requireNotNull(parts) { "versionName must be major.minor.patch, got \"$name\"" }
    val (major, minor, patch) = parts
    require(minor < 100 && patch < 100) { "minor and patch must be below 100, got $name" }
    require(major < 2100) { "major must be below 2100 (Google Play's versionCode limit), got $name" }
    return major * 10_000 + minor * 100 + patch
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
    // Packages and the application ID follow the app's domain, arbresdeparis.fr; class names stay in English.
    namespace = "fr.arbresdeparis"
    defaultConfig {
        applicationId = "fr.arbresdeparis"
        versionCode = versionCodeOf(appVersionName)
        versionName = appVersionName
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
        // Debug installs next to the release build: its own application ID, plus its own icon and name
        // (app/src/debug/res).
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            // R8: shrink, optimise and obfuscate the code, then drop the resources nothing references.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
    }
}

// Used by the release workflow to find the release note (fastlane/metadata/.../changelogs/<versionCode>.txt).
tasks.register("printVersionCode") {
    val code = versionCodeOf(appVersionName)
    doLast { println(code) }
}

aboutLibraries {
    // Fail the build if a dependency has no license information.
    export.prettyPrint = true
}

dependencies {
    implementation(libs.compose.runtime.retain)
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.designsystem)
    implementation(projects.core.map)
    implementation(projects.core.mapMaplibre)
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
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
}
