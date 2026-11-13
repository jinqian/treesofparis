import io.github.takahirom.roborazzi.RoborazziExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * A feature module: Android library + Compose + Hilt, with the shared core modules it may use, and Roborazzi for the
 * screenshot tests of its screens (reference images in `src/test/screenshots/`, compared in CI).
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("trees.android.library")
            pluginManager.apply("trees.android.compose")
            pluginManager.apply("trees.android.hilt")
            pluginManager.apply("io.github.takahirom.roborazzi")
            dependencies.apply {
                add("implementation", project(":core:domain"))
                add("implementation", project(":core:designsystem"))
                add("implementation", project(":core:map"))
                add("implementation", lib("androidx-hilt-lifecycle-viewmodel-compose"))
                add("implementation", lib("androidx-lifecycle-runtime-compose"))
                add("implementation", lib("androidx-lifecycle-viewmodel-compose"))
                add("implementation", lib("androidx-activity-compose"))
                add("testImplementation", lib("roborazzi"))
                add("testImplementation", lib("roborazzi-compose"))
            }
            // Reference images live with the tests, so a visual change shows up in the pull request.
            extensions.configure<RoborazziExtension> { outputDir.set(file("src/test/screenshots")) }
        }
    }
}
