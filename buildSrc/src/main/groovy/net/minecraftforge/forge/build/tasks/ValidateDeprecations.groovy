/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.forge.build.tasks

import groovy.transform.CompileStatic
import net.minecraftforge.srgutils.MinecraftVersion
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode

import javax.inject.Inject

@CompileStatic
abstract class ValidateDeprecations extends DefaultTask {
    abstract @InputFile RegularFileProperty getInput()

    abstract @Input Property<String> getMcVersion()

    static TaskProvider<ValidateDeprecations> register(TaskContainer tasks, TaskProvider<? extends Jar> jar, String minecraftVersion) {
        tasks.register("validate${jar.name.capitalize()}Deprecations", ValidateDeprecations, jar, minecraftVersion).tap { task ->
            tasks.named('check') { it.dependsOn(task) }
        }
    }

    @Inject
    ValidateDeprecations(TaskProvider<? extends Jar> jar, String minecraftVersion) {
        this.input.set(jar.flatMap(Jar.&getArchiveFile))
        this.mcVersion.set(minecraftVersion)
    }

    @TaskAction
    protected void exec() {
        var mcVer = MinecraftVersion.from(mcVersion.get())
        List<String> errors = []

        Util.processClassNodes(input.asFile.get()) {
            processNode(mcVer, errors, it)
        }

        if (!errors.isEmpty()) {
            errors.forEach {
                this.logger.error("Deprecated ${it[0]} is marked for removal in ${it[1]} but is not yet removed")
            }
            throw new IllegalStateException("Found deprecated members marked for removal but not yet removed in ${mcVer}; see log for details")
        }
    }

    private static processNode(MinecraftVersion mcVer, List<String> errors, ClassNode node) {
        node.visibleAnnotations?.each { annotation ->
            ValidateDeprecations.processAnnotations(annotation, mcVer, errors) {
                "class ${node.name}"
            }
        }
        node.fields?.each { field ->
            field.visibleAnnotations?.each { annotation ->
                ValidateDeprecations.processAnnotations(annotation, mcVer, errors) {
                    "field ${node.name}#${field.name}"
                }
            }
        }
        node.methods?.each { method ->
            method.visibleAnnotations?.each { annotation ->
                ValidateDeprecations.processAnnotations(annotation, mcVer, errors) {
                    "method ${node.name}#${method.name}${method.desc}"
                }
            }
        }
    }

    private static void processAnnotations(AnnotationNode annotation, MinecraftVersion mcVer, List<String> errors, Closure<String> context) {
        var values = annotation.values
        if (values === null) return

        int forRemoval = values.indexOf('forRemoval')
        int since = values.indexOf('since')
        if (annotation.desc == 'Ljava/lang/Deprecated;' && forRemoval !== -1 && since !== -1 && values.size() >= 4 && values[forRemoval + 1] === true) {
            var oldVersion = MinecraftVersion.from(values[since + 1].toString())
            int[] split = ValidateDeprecations.splitDots(oldVersion.toString())
            if (split.length < 2) return

            var removeVersion = MinecraftVersion.from("${split[0]}.${split[1] + 1}")
            if (removeVersion <= mcVer)
                errors.addAll([context(), removeVersion.toString()])
        }
    }

    private static int[] splitDots(String version) {
        var pts = version.split('\\.')
        var values = new int[pts.length]
        for (int x = 0; x < pts.length; x++)
            values[x] = Integer.parseInt(pts[x])
        return values
    }
}
