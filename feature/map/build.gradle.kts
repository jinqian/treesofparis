plugins {
    id("trees.android.feature")
}

android {
    namespace = "fr.arbresdeparis.feature.map"
}

dependencies {
    // Opens Wikipedia articles in a Custom Tab, so back returns to the tree sheet.
    implementation(libs.androidx.browser)
}
