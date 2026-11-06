plugins {
    id("trees.android.application")
    id("trees.android.compose")
    id("trees.android.hilt")
}

android {
    namespace = "com.treesofparis"
    defaultConfig {
        applicationId = "com.treesofparis"
        versionCode = 1
        versionName = "0.1.0"
    }
}
