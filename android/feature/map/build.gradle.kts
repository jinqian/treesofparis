plugins {
    id("trees.android.feature")
}

android {
    namespace = "fr.arbresdeparis.feature.map"
}

dependencies {
    // Opens Wikipedia articles in a Custom Tab, so back returns to the tree sheet.
    implementation(libs.androidx.browser)
    // The system location button: the only way to get precise location on Android 17 (ADR 0006).
    implementation(libs.androidx.locationbutton.compose)
    // The photos of the remarkable trees, from the City's server (« Son histoire »).
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
}
