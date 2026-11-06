plugins {
    id("trees.android.library")
    id("trees.android.compose")
}

android {
    namespace = "com.treesofparis.core.designsystem"
}

dependencies {
    implementation(libs.compose.material.icons)
}
