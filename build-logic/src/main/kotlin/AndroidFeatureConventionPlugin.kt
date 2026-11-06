import org.gradle.api.Plugin
import org.gradle.api.Project

/** A feature module: Android library + Compose + Hilt, with the shared core modules it may use. */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("trees.android.library")
            pluginManager.apply("trees.android.compose")
            pluginManager.apply("trees.android.hilt")
            dependencies.apply {
                add("implementation", project(":core:domain"))
                add("implementation", project(":core:designsystem"))
                add("implementation", project(":core:map"))
                add("implementation", lib("androidx-hilt-navigation-compose"))
                add("implementation", lib("androidx-lifecycle-runtime-compose"))
                add("implementation", lib("androidx-lifecycle-viewmodel-compose"))
            }
        }
    }
}
