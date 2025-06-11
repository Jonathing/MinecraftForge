package net.minecraftforge.forgedev

import groovy.transform.CompileStatic
import org.gradle.api.Action
import org.gradle.api.Transformer
import org.gradle.api.file.Directory
import org.gradle.api.problems.Problem
import org.gradle.api.problems.ProblemGroup
import org.gradle.api.problems.ProblemId
import org.gradle.api.problems.ProblemReporter
import org.gradle.api.problems.ProblemSpec
import org.gradle.api.problems.Problems
import org.gradle.api.problems.Severity
import org.gradle.api.provider.ProviderFactory
import org.jetbrains.annotations.UnknownNullability

import java.nio.file.Files
import java.util.concurrent.Callable
import java.util.function.Predicate

@CompileStatic
class ForgeDevProblems implements Problems {
    private static final ProblemGroup GROUP = ProblemGroup.create('forgedev', 'ForgeDev')
    private static final String HELP_MESSAGE = 'Consult the documentation or ask for help on the Forge Forums, GitHub, or Discord server.'

    private final Problems problems
    private final Predicate<String> properties

    ForgeDevProblems(Problems problems, Predicate<String> properties) {
        this.problems = problems
        this.properties = properties
    }

    @Override
    ProblemReporter getReporter() {
        this.problems.reporter
    }

    ForgeDevProblems(Problems problems, ProviderFactory providers) {
        this(problems, (String property) -> hasProperty(providers, property))
    }

    ForgeDevProblems(Callable<? extends @UnknownNullability Problems> problems, Callable<? extends @UnknownNullability ProviderFactory> providers) {
        this(unwrapProblems(problems), unwrapProperties(providers))
    }

    private static Problems unwrapProblems(Callable<? extends @UnknownNullability Problems> supplier) {
        return Util.tryElse(supplier, EmptyReporter.AS_PROBLEMS)
    }

    private static Predicate<String> unwrapProperties(Callable<? extends @UnknownNullability ProviderFactory> supplier) {
        return Util.tryElse(
            () -> {
                var providers = Objects.requireNonNull(supplier.call())
                return (String property) -> hasProperty(providers, property)
            },
            Boolean.&getBoolean
        )
    }

    private static boolean hasProperty(ProviderFactory providers, String property) {
        return Util.isTrue(providers.gradleProperty(property))
            || Util.isTrue(providers.systemProperty(property))
    }

    private static ProblemId id(String name, String displayName) {
        return ProblemId.create(name, displayName, GROUP)
    }

    //region Utilities
    Transformer<Directory, Directory> ensureDirectory() {
        { Directory dir ->
            try {
                Files.createDirectories(dir.getAsFile().toPath())
                return dir
            } catch (IOException e) {
                throw this.reporter.throwing(e, id('cannot-ensure-directory', 'Failed to create directory'), spec -> spec
                    .details("""
                        Failed to create a directory required for ForgeGradle to function.
                        Directory: ${dir.getAsFile().getAbsolutePath()}""".stripIndent())
                    .severity(Severity.ERROR)
                    .stackLocation()
                    .solution('Ensure that the you have write access to the directory that needs to be created.')
                    .solution(HELP_MESSAGE))
            }
        }
    }
    //endregion

    private static final class EmptyReporter implements ProblemReporter {
        private static final EmptyReporter INSTANCE = new EmptyReporter()
        private static final Problems AS_PROBLEMS = () -> INSTANCE

        @Override
        Problem create(ProblemId problemId, Action<? super ProblemSpec> action) {
            new Problem() { }
        }

        @Override
        void report(ProblemId problemId, Action<? super ProblemSpec> spec) { }

        @Override
        void report(Problem problem) { }

        @Override
        void report(Collection<? extends Problem> problems) { }

        @Override
        RuntimeException throwing(Throwable exception, ProblemId problemId, Action<? super ProblemSpec> spec) {
            toRTE(exception)
        }

        @Override
        RuntimeException throwing(Throwable exception, Problem problem) {
            toRTE(exception)
        }

        @Override
        RuntimeException throwing(Throwable exception, Collection<? extends Problem> problems) {
            toRTE(exception)
        }

        private static RuntimeException toRTE(Throwable exception) {
            return exception instanceof RuntimeException ? exception : new RuntimeException(exception)
        }
    }
}
