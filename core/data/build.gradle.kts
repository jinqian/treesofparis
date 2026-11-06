plugins {
    id("trees.android.library")
    id("trees.android.hilt")
}

android {
    namespace = "com.treesofparis.core.data"
}

dependencies {
    implementation(projects.core.domain)
}
