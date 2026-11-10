import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

/** Modules that feature modules must never depend on; they are wired only in :app. */
private val FORBIDDEN_FOR_FEATURES = setOf(":core:data", ":core:map-maplibre")

class ModuleBoundariesPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val projectPath = path
            if (!projectPath.startsWith(":feature:")) return
            val check =
                tasks.register<CheckModuleBoundariesTask>("checkModuleBoundaries") {
                    modulePath.set(projectPath)
                    violations.set(
                        provider {
                            configurations
                                .flatMap { it.dependencies.withType<ProjectDependency>() }
                                .map { it.path }
                                .filter { it in FORBIDDEN_FOR_FEATURES }
                                .distinct()
                        },
                    )
                }
            tasks.matching { it.name == "check" }.configureEach { dependsOn(check) }
        }
    }
}

abstract class CheckModuleBoundariesTask : DefaultTask() {
    @get:Input
    abstract val modulePath: org.gradle.api.provider.Property<String>

    @get:Input
    abstract val violations: ListProperty<String>

    @TaskAction
    fun check() {
        val found = violations.get()
        if (found.isNotEmpty()) {
            throw GradleException(
                "${modulePath.get()} depends on $found. Feature modules may only depend on " +
                    "core:domain, core:designsystem and core:map; implementations are wired in :app.",
            )
        }
    }
}
