plugins {
    id("trees.android.feature")
}

android {
    namespace = "fr.arbresdeparis.feature.about"
}

dependencies {
    implementation(libs.aboutlibraries.compose.core)
}
