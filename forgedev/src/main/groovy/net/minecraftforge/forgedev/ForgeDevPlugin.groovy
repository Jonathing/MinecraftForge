package net.minecraftforge.forgedev

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.ProjectLayout
import org.gradle.api.flow.FlowProviders
import org.gradle.api.flow.FlowScope
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.jetbrains.annotations.Nullable
import org.jetbrains.annotations.UnknownNullability

import javax.inject.Inject

@CompileStatic
abstract class ForgeDevPlugin implements Plugin<ExtensionAware> {
    public static final Logger LOGGER = Logging.getLogger('ForgeDev')

    private ExtensionAware target
    private final ForgeDevProblems enhancedProblems

    @Inject
    ForgeDevPlugin() {
        this.enhancedProblems = new ForgeDevProblems(this.&getProblems, this.&getProviders)
    }

    @Override
    void apply(ExtensionAware target) {
        this.target = target

        target.extensions.add(ForgeDevExtension.NAME, new ForgeDevExtension(this))
    }

    @Lazy DirectoryProperty mavenizerRepo = {
        this.getObjects().directoryProperty().value(
            this.getGlobalCaches().dir('repo').map(this.enhancedProblems.ensureFileLocation())
        ).tap {
            disallowChanges()
            finalizeValueOnRead()
        }
    }()

    @Lazy DirectoryProperty globalCaches = {
        try {
            this.getObjects().directoryProperty().convention(
                this.getGradleUserHomeDir().dir('minecraftforge/forgedev').map(this.enhancedProblems.ensureFileLocation())
            )
        } catch (Throwable e) {
            throw new IllegalArgumentException("Failed to get ForgeDev global caches directory for target: ${this.target}", e)
        }
    }()

    @Lazy DirectoryProperty localCaches = {
        try {
            this.getObjects().directoryProperty().convention(
                this.getWorkingProjectBuildDir().dir('minecraftforge/forgedev').map(this.enhancedProblems.ensureFileLocation())
            )
        } catch (Throwable e) {
            throw new IllegalArgumentException("Failed to get ForgeDev local caches directory for target: ${this.target}", e)
        }
    }()

    @SuppressWarnings('GrDeprecatedAPIUsage') // Intentional deprecation, please use this method
    Provider<File> getTool(Tools tool) {
        tool.get(this.globalCaches, this.providers)
    }

    protected @Inject Problems getProblems() throws Exception {
        this.injectFailed()
    }

    protected @Inject FlowScope getFlowScope() throws Exception {
        this.injectFailed()
    }

    protected @Inject FlowProviders getFlowProviders() throws Exception {
        this.injectFailed()
    }

    protected @Inject ObjectFactory getObjects() throws Exception {
        this.injectFailed()
    }

    protected @Inject ProjectLayout getProjectLayout() throws Exception {
        this.injectFailed()
    }

    protected @Inject ProviderFactory getProviders() throws Exception {
        this.injectFailed()
    }

    protected @Inject FileSystemOperations getFileSystemOperations() throws Exception {
        this.injectFailed()
    }

    protected @Inject ArchiveOperations getArchiveOperations() throws Exception {
        this.injectFailed()
    }

    @SuppressWarnings('GrMethodMayBeStatic')
    private <S> S injectFailed() {
        throw new Exception('Cannot use in current context (this is a ForgeGradle bug, please report it!)')
    }

    @CompileDynamic
    private DirectoryProperty getGradleUserHomeDir() {
        final startParameter = (this.target.gradle as Gradle).startParameter
        this.objects.directoryProperty().fileValue(startParameter.gradleUserHomeDir)
    }

    @CompileDynamic
    private DirectoryProperty getWorkingProjectBuildDir() {
        if (this.target instanceof Project)
            return (this.target as Project).layout.buildDirectory

        final startParameter = (this.target.gradle as Gradle).startParameter
        this.objects.directoryProperty().fileValue(new File(startParameter.projectDir ?: startParameter.currentDir, 'build'))
    }
}
