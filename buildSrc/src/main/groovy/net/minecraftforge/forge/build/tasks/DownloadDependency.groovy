package net.minecraftforge.forge.build.tasks

import groovy.transform.CompileStatic
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderConvertible
import org.gradle.api.tasks.TaskProvider

@CompileStatic
abstract class DownloadDependency {
    static TaskProvider<Task> register(Project project, String name, Object dependency) {
        final def unpacked
        if (dependency instanceof ProviderConvertible<?>)
            unpacked = dependency.asProvider().get()
        else if (dependency instanceof Provider<?>)
            unpacked = dependency.get()
        else
            unpacked = dependency

        var configuration = project.configurations.detachedConfiguration(
            project.dependencies.create(unpacked instanceof Dependency ? unpacked.copy() : unpacked) { ModuleDependency it ->
                if (it instanceof ModuleDependency) {
                    it.transitive = false
                    if (it instanceof ExternalModuleDependency) {
                        it.changing = false
                    }
                }
            }
        )

        project.tasks.register(name) {
            it.outputs.files(configuration)
        }
    }
}
