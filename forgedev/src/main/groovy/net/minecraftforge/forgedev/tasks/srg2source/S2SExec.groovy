package net.minecraftforge.forgedev.tasks.srg2source

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import net.minecraftforge.forgedev.Tools
import net.minecraftforge.forgedev.Util
import net.minecraftforge.forgedev.tasks.ToolExec
import org.gradle.api.JavaVersion
import org.gradle.api.problems.Problems

@CompileStatic
@PackageScope abstract class S2SExec extends ToolExec {
    // NOTE: check net.minecraftforge.srg2source.api.SourceVersion for compatible ranges
    private static final int SRC_COMPAT_MIN = 6
    private static final String SRC_COMPAT_MIN_STR = '1.6'
    private static final int SRC_COMPAT_MAX = 23
    private static final String SRC_COMPAT_MAX_STR = '23'

    S2SExec(Problems problems) {
        super(problems, Tools.SRG2SRC)

        this.standardOutput = Util.toLog(this.logger.&info)
    }

    @Override
    void exec() {
        super.exec()
        this.executionResult.get().rethrowFailure().assertNormalExitValue()
    }

    protected final String parseSourceCompatibility(JavaVersion javaVersion) {
        final version = javaVersion.toString()
        final split = version.split('\\.')
        if (split.length > 0 && split[1].toInteger() < SRC_COMPAT_MIN) {
            this.logger.warn('WARNING: {} source compatibility {} is lower than minimum of {}', this.identityPath, version, SRC_COMPAT_MIN_STR)
            return SRC_COMPAT_MIN_STR
        } else if (version.toInteger() > SRC_COMPAT_MAX) {
            this.logger.warn('WARNING: {} source compatibility {} is higher than the maximum of {}', this.identityPath, version, SRC_COMPAT_MAX_STR)
            return SRC_COMPAT_MAX_STR
        } else {
            return version
        }
    }
}
