plugins {
    id("trees.android.feature")
    // Small-screen screenshots of the map controls and sheets (SmallScreenScreenshotTest).
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "fr.arbresdeparis.feature.map"
}

dependencies {
    // Opens Wikipedia articles in a Custom Tab, so back returns to the tree sheet.
    implementation(libs.androidx.browser)
    // The system location button: the only way to get precise location on Android 17 (ADR 0006).
    implementation(libs.androidx.locationbutton.compose)

    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
}

roborazzi {
    // Reference images live with the tests, so a layout change shows up in the pull request.
    outputDir.set(file("src/test/screenshots"))
}
