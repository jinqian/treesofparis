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
}
