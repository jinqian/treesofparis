plugins {
    `kotlin-dsl`
}

group = "fr.arbresdeparis.buildlogic"

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
    compileOnly(libs.roborazzi.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("jvmLibrary") {
            id = "trees.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("androidLibrary") {
            id = "trees.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidApplication") {
            id = "trees.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidCompose") {
            id = "trees.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "trees.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("moduleBoundaries") {
            id = "trees.module.boundaries"
            implementationClass = "ModuleBoundariesPlugin"
        }
        register("androidHilt") {
            id = "trees.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }
    }
}
