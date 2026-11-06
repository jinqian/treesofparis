plugins {
    id("trees.android.feature")
}

android {
    namespace = "com.treesofparis.feature.about"
}

dependencies {
    implementation(libs.aboutlibraries.compose.core)
}
