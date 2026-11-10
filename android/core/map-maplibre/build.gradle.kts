plugins {
    id("trees.android.library")
    id("trees.android.compose")
}

android {
    namespace = "fr.arbresdeparis.core.map.maplibre"
}

dependencies {
    api(projects.core.map)
    implementation(libs.maplibre.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
