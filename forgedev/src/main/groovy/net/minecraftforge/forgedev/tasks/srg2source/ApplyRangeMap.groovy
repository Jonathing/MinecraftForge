package net.minecraftforge.forgedev.tasks.srg2source

import groovy.transform.CompileStatic
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile

import javax.inject.Inject

@CompileStatic
abstract class ApplyRangeMap extends S2SExec {
    abstract @InputFiles ConfigurableFileCollection getSources()
    abstract @OutputFile RegularFileProperty getOutput()
    abstract @InputFiles @Optional ConfigurableFileCollection getExcFiles()
    abstract @InputFiles ConfigurableFileCollection getSrgFiles()

    abstract @InputFile RegularFileProperty getRangeMap()
    abstract @Input @Optional Property<Boolean> getKeepImports()
    abstract @Input @Optional @Deprecated Property<Boolean> getAnnotate()

    abstract @Input Property<Boolean> getSortImports()
    abstract @Input Property<Boolean> getGuessLambdas()
    abstract @Input Property<Boolean> getGuessLocals()

    @Inject
    ApplyRangeMap(Problems problems) {
        super(problems)

        this.output.convention(this.defaultOutputFile)
    }

    @Override
    protected void addArguments() {
        this.args('--apply')

        this.args('--in', this.sources)
        this.args('--out', this.output)
        this.args('--exc', this.excFiles)
        this.args('--srg', this.srgFiles)

        this.args('--range', this.rangeMap)
        this.args('--keepImports', this.keepImports)
        //this.args('--annotate', this.annotate)

        this.argOnlyIf('--sortImports', this.sortImports)
        this.argOnlyIf('--guessLambdas', this.guessLambdas)
        this.argOnlyIf('--guessLocals', this.guessLocals)
    }
}
