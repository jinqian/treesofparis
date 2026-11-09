plugins {
    id("trees.android.library")
    id("trees.android.compose")
}

android {
    namespace = "fr.arbresdeparis.core.map.osmdroid"
}

dependencies {
    api(projects.core.map)
    implementation(libs.osmdroid)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
