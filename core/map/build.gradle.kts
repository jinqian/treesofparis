plugins {
    id("trees.android.library")
    id("trees.android.compose")
}

android {
    namespace = "com.treesofparis.core.map"
}

dependencies {
    api(projects.core.domain)
}
