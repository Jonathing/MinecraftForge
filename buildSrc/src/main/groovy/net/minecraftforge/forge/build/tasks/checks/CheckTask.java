package net.minecraftforge.forge.build.tasks.checks;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.FromString;
import net.minecraftforge.gradleutils.shared.Closures;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.gradle.api.DefaultTask;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.VerificationTask;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public abstract class CheckTask extends DefaultTask implements VerificationTask {
    public abstract @Input Property<CheckMode> getMode();

    private final Property<Boolean> ignoreFailures = getObjects().property(Boolean.class).convention(false);

    protected abstract @Inject ObjectFactory getObjects();

    @Inject
    public CheckTask() { }

    @Override
    public boolean getIgnoreFailures() {
        return this.ignoreFailures.getOrElse(false);
    }

    @Override
    public void setIgnoreFailures(boolean ignoreFailures) {
        this.ignoreFailures.set(ignoreFailures);
    }

    @TaskAction
    protected void run() {
        var name = getName();
        var logger = getLogger();

        var doFix = getMode().get() == CheckMode.FIX;
        var reporter = getObjects().newInstance(Reporter.class, doFix);
        check(reporter, doFix);

        if (!reporter.messages.isEmpty()) {
            if (!doFix) {
                logger.error("Check task '{}' found errors:\n{}", name, String.join("\n", reporter.messages));
                if (!getIgnoreFailures())
                    throw new IllegalArgumentException(reporter.messages.size() + " errors were found");
            } else {
                if (logger.isEnabled(LogLevel.DEBUG))
                    logger.warn("Check task '{}' found {} errors and fixed {}:\n{}", name, reporter.messages.size(), reporter.fixed.size(), String.join("\n", reporter.fixed));
                else
                    logger.warn("Check task '{}' found {} errors and fixed {}.", name, reporter.messages.size(), reporter.fixed.size());

                if (!reporter.notFixed.isEmpty()) {
                    logger.error("{} errors could not be fixed:\n{}", reporter.notFixed.size(), String.join("\n", reporter.notFixed));
                    if (!getIgnoreFailures())
                        throw new IllegalArgumentException(reporter.notFixed.size() + " errors which cannot be fixed were found!");
                }
            }
        }
    }

    abstract void check(Reporter reporter, boolean fix);

    static abstract class Reporter {
        final boolean trackFixed;

        @Inject
        public Reporter(boolean trackFixed) {
            this.trackFixed = trackFixed;
        }

        public final List<String> messages = new ArrayList<>();

        public final List<String> fixed = new ArrayList<>();
        public final List<String> notFixed = new ArrayList<>();

        public boolean isTrackFixed() {
            return this.trackFixed;
        }

        public void report(String message) {
            report(message, true);
        }

        public void report(String message, boolean canBeFixed) {
            messages.add(message);

            if (trackFixed) {
                if (canBeFixed) {
                    fixed.add(message);
                } else {
                    notFixed.add(message);
                }
            }
        }
    }

    public static <T extends CheckTask> TaskProvider<T> register(
        TaskContainer tasks,
        String taskName,
        Class<T> clazz,
        @DelegatesTo(strategy = Closure.DELEGATE_FIRST)
        @ClosureParams(value = FromString.class, options = "T")
        Closure<?> configuration
    ) {
        taskName = StringGroovyMethods.capitalize(taskName);
        var check = tasks.register("check" + taskName, clazz, task -> {
            Closures.invoke(configuration, task);
            task.getMode().set(CheckMode.CHECK);
            task.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
        });
        tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME, task -> task.dependsOn(check));

        var checkAndFix = tasks.register("checkAndFix" + taskName, clazz, task -> {
            Closures.invoke(configuration, task);
            task.getMode().set(CheckMode.FIX);
            task.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
        });
        tasks.named("checkAndFix", task -> task.dependsOn(checkAndFix));

        return check;
    }
}
