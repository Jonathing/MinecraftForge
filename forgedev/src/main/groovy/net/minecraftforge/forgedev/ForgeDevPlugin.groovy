package net.minecraftforge.forgedev

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.ProjectLayout
import org.gradle.api.flow.FlowProviders
import org.gradle.api.flow.FlowScope
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.jetbrains.annotations.Nullable

import javax.inject.Inject

@CompileStatic
abstract class ForgeDevPlugin implements Plugin<ExtensionAware> {
    public static final Logger LOGGER = Logging.getLogger("ForgeDev")

    private final ForgeDevProblems enhancedProblems

    private @Nullable DirectoryProperty globalCaches

    @Inject
    ForgeDevPlugin() {
        this.enhancedProblems = new ForgeDevProblems(this.&getProblems, this.&getProviders)
    }

    @Override
    void apply(ExtensionAware target) {
        this.globalCaches = this.objects.directoryProperty().convention(
            this.objects.directoryProperty().fileValue(this.getGradleUserHomeDir(target)).dir('minecraftforge/forgedev').map(this.enhancedProblems.ensureDirectory())
        )
    }

    DirectoryProperty getGlobalCaches() {
        try {
            Objects.requireNonNull(this.globalCaches)
        } catch (Throwable e) {
            throw new IllegalStateException('ForgeGradle does not have global caches', e)
        }
    }

    @SuppressWarnings('GrDeprecatedAPIUsage') // Intentional deprecation, please use this method
    Provider<File> getTool(Tools tool) {
        tool.get(this.globalCaches, this.providers)
    }

    @CompileDynamic
    private File getGradleUserHomeDir(ExtensionAware target) {
        try {
            target.gradle.startParameter.gradleUserHomeDir
        } catch (Throwable e) {
            throw this.enhancedProblems.illegalPluginTarget(new IllegalArgumentException("Cannot apply ForgeGradle to target: " + target, e))
        }
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
        throw new Exception("Cannot use in current context (this is a ForgeGradle bug, please report it!)")
    }
}
