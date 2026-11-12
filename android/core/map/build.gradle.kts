plugins {
    id("trees.android.library")
    id("trees.android.compose")
}

android {
    namespace = "fr.arbresdeparis.core.map"
}

dependencies {
    api(projects.core.domain)
}
